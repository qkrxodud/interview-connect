package com.ic.common.exception;

/**
 * 비즈니스 로직에서 발생하는 예외 클래스
 * ErrorCode를 포함하여 일관된 에러 처리를 지원한다
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    private BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    private BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode로 예외 생성
     */
    public static BusinessException from(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    /**
     * ErrorCode와 커스텀 메시지로 예외 생성
     */
    public static BusinessException of(ErrorCode errorCode, String message) {
        return new BusinessException(errorCode, message);
    }

    /**
     * ErrorCode, 커스텀 메시지, 원인 예외로 예외 생성
     */
    public static BusinessException of(ErrorCode errorCode, String message, Throwable cause) {
        return new BusinessException(errorCode, message, cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getStatus() {
        return errorCode.getStatus();
    }

    public String getCode() {
        return errorCode.getCode();
    }
}