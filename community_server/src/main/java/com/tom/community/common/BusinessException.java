package com.tom.community.common;

import lombok.Getter;

/**
 * 业务异常 — 由 GlobalExceptionHandler 统一拦截转为 Result
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
