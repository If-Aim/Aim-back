package com.aim.global.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class CustomAuthenticationException extends AuthenticationException {
    private final ExceptionCode exceptionCode;

    public CustomAuthenticationException(ExceptionCode code, String message, Throwable cause) {
        super(message, cause);
        this.exceptionCode = code;
    }
}

