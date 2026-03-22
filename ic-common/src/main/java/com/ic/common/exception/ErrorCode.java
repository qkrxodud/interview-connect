package com.ic.common.exception;

/**
 * 애플리케이션에서 사용하는 에러 코드 정의
 * 각 도메인별로 분류하여 관리한다
 */
public enum ErrorCode {

    // Auth (인증/인가 관련)
    INVALID_CREDENTIALS(401, "A001", "이메일 또는 비밀번호가 올바르지 않습니다"),
    EXPIRED_TOKEN(401, "A002", "토큰이 만료되었습니다"),
    UNAUTHORIZED(401, "A003", "로그인이 필요합니다"),
    FORBIDDEN(403, "A004", "권한이 없습니다"),
    INVALID_TOKEN(401, "A005", "유효하지 않은 토큰입니다"),

    // Member (회원 관련)
    DUPLICATE_EMAIL(409, "M001", "이미 가입된 이메일입니다"),
    MEMBER_NOT_FOUND(404, "M002", "회원을 찾을 수 없습니다"),
    DUPLICATE_NICKNAME(409, "M003", "이미 사용 중인 닉네임입니다"),
    INVALID_PASSWORD(400, "M004", "비밀번호는 8자 이상이어야 합니다"),

    // Review (후기 관련)
    REVIEW_NOT_FOUND(404, "R001", "후기를 찾을 수 없습니다"),
    REVIEW_PERMISSION_DENIED(403, "R002", "인증 회원만 후기를 작성할 수 있습니다"),
    REVIEW_AUTHOR_MISMATCH(403, "R003", "본인이 작성한 후기만 수정/삭제할 수 있습니다"),
    INVALID_DIFFICULTY_RANGE(400, "R004", "난이도는 1~5 사이의 값이어야 합니다"),
    INVALID_ATMOSPHERE_RANGE(400, "R005", "분위기는 1~5 사이의 값이어야 합니다"),

    // Company (회사 관련)
    COMPANY_NOT_FOUND(404, "C001", "회사를 찾을 수 없습니다"),
    DUPLICATE_COMPANY_NAME(409, "C002", "이미 등록된 회사명입니다"),

    // QA (질문/답변 관련)
    QUESTION_NOT_FOUND(404, "Q001", "질문을 찾을 수 없습니다"),
    ANSWER_PERMISSION_DENIED(403, "Q002", "인증 회원만 답변할 수 있습니다"),
    ANSWER_NOT_FOUND(404, "Q003", "답변을 찾을 수 없습니다"),
    QUESTION_AUTHOR_MISMATCH(403, "Q004", "본인이 작성한 질문만 수정/삭제할 수 있습니다"),
    ANSWER_AUTHOR_MISMATCH(403, "Q005", "본인이 작성한 답변만 수정/삭제할 수 있습니다"),

    // Notification (알림 관련)
    NOTIFICATION_NOT_FOUND(404, "N001", "알림을 찾을 수 없습니다"),
    NOTIFICATION_PERMISSION_DENIED(403, "N002", "본인의 알림만 조회할 수 있습니다"),

    // Common (공통)
    INVALID_INPUT(400, "G001", "입력값이 올바르지 않습니다"),
    INTERNAL_ERROR(500, "G002", "서버 내부 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(405, "G003", "허용되지 않은 HTTP 메서드입니다"),
    RESOURCE_NOT_FOUND(404, "G004", "요청한 리소스를 찾을 수 없습니다"),
    INVALID_REQUEST_FORMAT(400, "G005", "요청 형식이 올바르지 않습니다"),
    MISSING_REQUIRED_PARAMETER(400, "G006", "필수 파라미터가 누락되었습니다"),
    INVALID_PAGE_PARAMETER(400, "G007", "페이지 파라미터가 올바르지 않습니다");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}