package com.aim.auth.service.jwt;


import com.aim.auth.domain.User;
import com.aim.global.config.CustomAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtResolver jwtResolver; // 토큰 검증/파싱 유틸
    private final UserDetailsService userDetailsService; // 유저 로드
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1) Authorization 헤더 추출
            String bearerToken = request.getHeader("Authorization");

            // 2) Bearer 토큰 형식인지 확인
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                // "Bearer " 제거 → 실제 JWT 값만 추출
                bearerToken = bearerToken.substring(7);

                // 3) 블랙리스트 체크 (로그아웃된 토큰인지)
//                if (jwtResolver.isBlacklisted(bearerToken)) {
//                    throw new CustomAuthenticationException(
//                            ExceptionCode.ALREADY_LOGGED_TOKEN,
//                            ExceptionCode.ALREADY_LOGGED_TOKEN.getMessage(),
//                            null
//                    );
//                }

                // 4) 토큰에서 userId 추출
                String email = jwtResolver.resolveAccessToken(bearerToken);

                // 5) DB에서 해당 유저 정보(UserDetails) 로드
                CustomUserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                // 6) Spring Security의 Authentication 객체 생성
                User user = userDetails.getUser();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user, null, userDetails.getAuthorities()
                        );

                // 7) 요청 정보(WebAuthenticationDetails) 세팅
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 8) SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // 9) 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        } catch (AuthenticationException exception) {
            // 10) 인증 예외 발생 시 EntryPoint 호출 → 401 응답
            authenticationEntryPoint.commence(request, response, exception);
        }
    }
}

