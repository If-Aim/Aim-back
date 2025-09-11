package com.aim.global.config;


import com.aim.global.exception.CustomAuthenticationException;
import com.aim.global.exception.ExceptionCode;
import com.aim.global.exception.dto.ExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper; // JSON 변환용 Jackson

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 1) 기본 예외 코드 설정
        ExceptionCode exceptionCode = ExceptionCode.AUTH_TOKEN_INVALID;

        // 2) 예외가 CustomAuthenticationException이면, 내부의 ExceptionCode를 꺼냄
        if (authException instanceof CustomAuthenticationException customEx){
            exceptionCode = customEx.getExceptionCode();
        }

        // 3) 예외 응답 바디 객체 생성
        ExceptionResponse responseBody = new ExceptionResponse(
                HttpStatus.UNAUTHORIZED.value(),    // 401
                exceptionCode.name(),               // 예: AUTH_TOKEN_INVALID
                exceptionCode.getMessage(),         // 예: "토큰이 유효하지 않습니다."
                request.getRequestURI(),            // 요청 URI
                ZonedDateTime.now()                 // 발생 시각
        );

        // 4) HTTP 응답 헤더/상태 코드 설정
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("application/json;charset=UTF-8");

        // 5) JSON으로 변환해 응답 바디에 씀
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}


