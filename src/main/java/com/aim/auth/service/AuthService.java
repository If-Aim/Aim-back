package com.aim.auth.service;


import com.aim.auth.domain.User;
import com.aim.auth.dto.request.EmailLoginRequest;
import com.aim.auth.dto.GoogleUserInfo;
import com.aim.auth.repository.UserRepository;
import com.aim.auth.service.jwt.JwtProvider;
import com.aim.auth.service.jwt.JwtResolver;
import com.aim.global.exception.AimException;
import com.aim.global.exception.ExceptionCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtResolver jwtResolver;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${CLIENT_ID}")
    private String googleClientId;

    @Value("${CLIENT_SECRET}")
    private String googleClientSecret;

    @Transactional
    public void loginOrRegisterUser(EmailLoginRequest request, HttpServletResponse response) {
        String email = request.getEmail();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null){
            user = registerEmailUser(request);
        } else {
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new AimException(ExceptionCode.AUTH_BAD_CREDENTIALS); // 적절한 코드 사용
            }
        }
        sendTokens(user, response);
    }

    private User registerEmailUser(EmailLoginRequest request){
        String email = request.getEmail();
        User.Role role = request.getRole();
        String encodedPw = passwordEncoder.encode(request.getPassword());
        String name = "테스트용 이름"; // 구글 연결하면 나중에 바꿀 예정

        User user = User.builder()
                .email(email)
                .password(encodedPw)
                .name(name)
                .role(role)
                .build();
        return userRepository.save(user);
    }

    private void sendTokens(User user, HttpServletResponse response){
        String accessToken = jwtProvider.createAccessToken(user);
        response.setHeader("Authorization", "Bearer " + accessToken);
    }


    // 구글 OAuth2 code로 JWT 토큰 발급 (포스트맨용)
    public String googleOAuth2Login(String code, HttpServletResponse response) {
        try {
            // 1. 인가코드로 구글 사용자 정보 가져오기
            User user = getGoogleUserInfo(code);

            // 2. JWT 토큰 생성 (기존 JWT 함수 사용)
            String jwtToken = jwtProvider.createAccessToken(user);

            // 3. 헤더에 JWT 토큰 설정
            response.setHeader("Authorization", "Bearer " + jwtToken);

            log.info("구글 OAuth2 로그인 성공: {}", user.getEmail());

            return jwtToken;

        } catch (Exception e) {
            log.error("구글 OAuth2 로그인 실패: {}", e.getMessage());
            throw new AimException(ExceptionCode.AUTH_BAD_CREDENTIALS);
        }
    }
    
    // 구글 OAuth2 code로 사용자 정보 가져오기
    private User getGoogleUserInfo(String code) {
        try {
            // 1. Authorization code로 access token 요청
            String accessToken = getGoogleAccessToken(code);
            
            // 2. Access token으로 사용자 정보 요청
            GoogleUserInfo userInfo = getGoogleUserProfile(accessToken);
            
            // 3. 사용자 정보로 User 엔티티 생성 또는 업데이트
            return createOrUpdateUser(userInfo);
            
        } catch (Exception e) {
            log.error("구글 사용자 정보 가져오기 실패: {}", e.getMessage());
            throw new AimException(ExceptionCode.AUTH_BAD_CREDENTIALS);
        }
    }
    
    // 구글 OAuth2 authorization code로 access token 요청
    private String getGoogleAccessToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", "https://aim-internie-app.kro.kr/login/oauth2/code/google");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            
            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            } else {
                throw new RuntimeException("구글 access token 요청 실패");
            }
        } catch (Exception e) {
            log.error("구글 access token 요청 실패: {}", e.getMessage());
            throw new RuntimeException("구글 access token 요청 실패", e);
        }
    }
    
    // 구글 access token으로 사용자 프로필 정보 요청
    private GoogleUserInfo getGoogleUserProfile(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                userInfoUrl, 
                HttpMethod.GET, 
                request, 
                GoogleUserInfo.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("구글 사용자 프로필 요청 실패: {}", e.getMessage());
            throw new RuntimeException("구글 사용자 프로필 요청 실패", e);
        }
    }
    
    // 구글 사용자 정보로 User 엔티티 생성 또는 업데이트
    private User createOrUpdateUser(GoogleUserInfo userInfo) {
        String email = userInfo.getEmail();
        
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(userInfo.getName())
                            .imageUrl(userInfo.getPicture())
                            .role(User.Role.ROLE_STUDENT)
                            .provider("google")
                            .providerId(userInfo.getId())
                            .password("")
                            .build();
                    return userRepository.save(newUser);
                });
        
        // 기존 사용자의 경우 provider 정보 업데이트
        if (user.getProvider() == null || user.getProviderId() == null) {
            user.setProvider("google");
            user.setProviderId(userInfo.getId());
            userRepository.save(user);
        }
        
        return user;
    }
    
    private jakarta.servlet.http.Cookie createTokenCookie(String name, String value, int maxAge) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS 환경에서는 true로 설정
        return cookie;
    }

}