package com.bank.assets.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;

    public AppException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = status;
    }

    public static AppException notFound(ErrorCode code) {
        return new AppException(code, HttpStatus.NOT_FOUND);
    }

    public static AppException badRequest(ErrorCode code) {
        return new AppException(code, HttpStatus.BAD_REQUEST);
    }

    public static AppException conflict(ErrorCode code) {
        return new AppException(code, HttpStatus.CONFLICT);
    }

    public static AppException forbidden(ErrorCode code) {
        return new AppException(code, HttpStatus.FORBIDDEN);
    }

    public static AppException unauthorized(ErrorCode code) {
        return new AppException(code, HttpStatus.UNAUTHORIZED);
    }
}
