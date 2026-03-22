package com.ic.common.response;

import com.ic.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 통일된 API 응답 래퍼 클래스
 * 모든 API 응답은 이 형태로 반환된다
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorResponse error
) {

    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    /**
     * 실패 응답 생성 (ErrorCode와 메시지 포함)
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        final ErrorResponse errorResponse = ErrorResponse.of(errorCode, message);
        return new ApiResponse<>(false, null, errorResponse);
    }

    /**
     * 실패 응답 생성 (ErrorCode만 포함)
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        final ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        return new ApiResponse<>(false, null, errorResponse);
    }
}