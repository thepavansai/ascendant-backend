package com.ascendant.initiative.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public AppException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static AppException notFound(String message) {
        return new AppException("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    public static AppException conflict(String message) {
        return new AppException("CONFLICT", message, HttpStatus.CONFLICT);
    }

    public static AppException forbidden(String message) {
        return new AppException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public static AppException badRequest(String message) {
        return new AppException("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }

    public static AppException unauthorized(String message) {
        return new AppException("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }
}
