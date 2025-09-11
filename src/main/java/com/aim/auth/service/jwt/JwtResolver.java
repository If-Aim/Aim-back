package com.aim.auth.service.jwt;


import com.aim.global.exception.AimException;
import com.aim.global.exception.CustomAuthenticationException;
import com.aim.global.exception.ExceptionCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtResolver {
    private final JwtProperties jwtProperties;

    public String resolveAccessToken(String token) {
        return resolveTokenByType(token, "accessToken");
    }

    public String resolveTokenByType(String token, String type) {
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtProperties.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            validateTokenByType(claims, type);

            return claims.getSubject();
        }catch (ExpiredJwtException exception) {
            throw new CustomAuthenticationException(ExceptionCode.AUTH_TOKEN_EXPIRED, ExceptionCode.AUTH_TOKEN_EXPIRED.getMessage(), exception);
        } catch (JwtException exception) {
            throw new CustomAuthenticationException(ExceptionCode.AUTH_TOKEN_INVALID, ExceptionCode.AUTH_TOKEN_INVALID.getMessage(), exception);
        }
    }

    public void validateTokenByType(Claims claims, String type){
        String extractTokenType = claims.get("type", String.class);
        if (extractTokenType == null || !extractTokenType.equals(type)) {
            throw new AimException(ExceptionCode.AUTH_TOKEN_MISMATCH);
        }
    }
}

