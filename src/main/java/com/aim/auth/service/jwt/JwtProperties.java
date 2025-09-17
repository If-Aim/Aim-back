package com.aim.auth.service.jwt;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Getter
public class JwtProperties { //JWT 발급/검증에 필요한 설정값(시크릿 키, 만료시간 등) 보관
    private final String secretKey;
    private final SecretKey hmacSecretKey;
    private final long accessTokenExpirationMills = 1000 * 60 * 60; // 1시간으로 설정, 액세스 토큰 유효기간
    //private final long refreshTokenExpirationMills = 1000 * 60 * 60 * 24 * 14; // 2주

    public JwtProperties(@Value("${jwt.secret.key}") String secretKey) {
        this.secretKey = secretKey;
        // 문자열 형태의 시크릿을 HMAC용 Secret 객체로 바꾼다. 같은 문자열이라도 바이트가 달라지는 일을 막기 위해 항상 UTF-8로 고정
        this.hmacSecretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // secretKey -> HMAC-SHA 서명에 사용 가능한 SecretKey 객체로 변환
    public SecretKey getSigningKey() {
        return hmacSecretKey;
    }

}

