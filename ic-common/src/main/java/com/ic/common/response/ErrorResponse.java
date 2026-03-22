package com.ic.common.response;

import com.ic.common.exception.ErrorCode;

/**
 * 에러 응답 정보를 담는 클래스
 */
public record ErrorResponse(
        String code,
        String message,
        int status
) {

    /**
     * ErrorCode와 커스텀 메시지로 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                errorCode.getCode(),
                message,
                errorCode.getStatus()
        );
    }

    /**
     * ErrorCode의 기본 메시지로 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getStatus()
        );
    }
}