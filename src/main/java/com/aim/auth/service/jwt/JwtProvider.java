package com.aim.auth.service.jwt;


import com.aim.auth.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component // 스프링 빈으로 등록
@RequiredArgsConstructor
@Slf4j
public class JwtProvider { // 설정값을 이용해 JWT 문자열을 생성
    private final JwtProperties jwtProperties;

    public String createAccessToken(User user) {
        long accessTokenExpirationMills = jwtProperties.getAccessTokenExpirationMills();
        return createToken(user, accessTokenExpirationMills, "accessToken");

    }

    public String createToken(User user, long expiration, String tokenType){
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(user.getEmail())             // 토큰의 주체(subject) User's Email
                .claim("role", user.getRole().name()) // 학생인지 회사인지
                .setIssuedAt(now)                        // 발급 시간
                .setExpiration(expirationDate)           // 만료 시간
                .claim("type", tokenType)             // type에 액세스인지 refresh인지
                .signWith(jwtProperties.getSigningKey(), SignatureAlgorithm.HS256) //HMAC-SHA256 알고리즘
                .compact();
    }
}

//    public String createRefreshToken(User user) {
//        long refreshTokenExpirationMills = jwtProperties.getRefreshTokenExpirationMills();
//        return createToken(user, refreshTokenExpirationMills, "refreshToken");
//    }

