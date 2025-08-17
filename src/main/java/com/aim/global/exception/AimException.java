package com.aim.global.exception;

import org.springframework.http.HttpStatusCode;

public class AimException extends RuntimeException {
    private final ExceptionCode exceptionCode;
    public AimException(ExceptionCode exceptionCode){this.exceptionCode=exceptionCode;}

    @Override
    public String getMessage() {return exceptionCode.getMessage();}
    public HttpStatusCode getHttpStatusCode() {return exceptionCode.getHttpStatus();}
    public String getExceptionCodeName() {return exceptionCode.getClientExceptionCode().name();}
}
