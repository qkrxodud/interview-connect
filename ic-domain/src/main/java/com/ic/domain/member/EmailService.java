package com.ic.domain.member;

/**
 * 이메일 발송 서비스 인터페이스
 */
public interface EmailService {

    /**
     * 이메일 인증 코드 발송
     */
    void sendVerificationEmail(String toEmail, String verificationCode);

    /**
     * 인증 코드 생성
     */
    String generateVerificationCode();
}