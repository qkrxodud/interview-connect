package com.ic.common.exception;

import com.ic.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;

/**
 * 전역 예외 처리 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 일관된 형태로 처리한다
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business Exception: {}", e.getMessage());

        final ApiResponse<Void> response = ApiResponse.error(e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(response);
    }

    /**
     * Bean Validation 예외 처리 (@Valid 검증 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());

        final BindingResult bindingResult = e.getBindingResult();
        final FieldError fieldError = bindingResult.getFieldError();
        final String errorMessage = Objects.nonNull(fieldError)
            ? fieldError.getDefaultMessage()
            : ErrorCode.INVALID_INPUT.getMessage();

        final ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_INPUT, errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 필수 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.warn("Missing Parameter Exception: {}", e.getMessage());

        final String message = String.format("필수 파라미터 '%s'가 누락되었습니다", e.getParameterName());
        final ApiResponse<Void> response = ApiResponse.error(ErrorCode.MISSING_REQUIRED_PARAMETER, message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * HTTP 메서드 지원하지 않음 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.warn("Method Not Allowed Exception: {}", e.getMessage());

        final ApiResponse<Void> response = ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 핸들러를 찾을 수 없음 예외 처리 (404 Not Found)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("No Handler Found Exception: {}", e.getMessage());

        final ApiResponse<Void> response = ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 요청 형식 오류 예외 처리 (JSON 파싱 실패 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.warn("Message Not Readable Exception: {}", e.getMessage());

        final ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_REQUEST_FORMAT);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 타입 변환 예외 처리 (PathVariable, RequestParam 타입 불일치)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("Argument Type Mismatch Exception: {}", e.getMessage());

        final String message = String.format("파라미터 '%s'의 타입이 올바르지 않습니다", e.getName());
        final ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_INPUT, message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 일반 예외 처리 (예상하지 못한 모든 예외)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);

        final ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}