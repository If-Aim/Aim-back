package com.aim.global.handler;


import com.aim.global.exception.AimException;
import com.aim.global.exception.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(AimException.class)
    public ResponseEntity<ExceptionResponse> handleAimException(AimException e, HttpServletRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                e.getHttpStatusCode().value(),
                e.getExceptionCodeName(),
                e.getMessage(),
                request.getRequestURI(), ZonedDateTime.now()
        );
        return ResponseEntity.status(e.getHttpStatusCode())
                .body(response);
    }
}

