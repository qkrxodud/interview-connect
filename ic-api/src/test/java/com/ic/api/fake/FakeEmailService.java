package com.ic.api.fake;

import com.ic.domain.member.EmailService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 테스트용 Fake EmailService 구현체
 * - 실제 이메일 전송 없이 상태를 추적
 * - 전송된 이메일 기록을 확인할 수 있음
 * - 인증 코드 생성과 검증 로직을 시뮬레이션
 */
public class FakeEmailService implements EmailService {

    private final List<SentEmail> sentEmails = new ArrayList<>();
    private final Map<String, String> verificationCodes = new HashMap<>();
    private boolean shouldFailSending = false;
    private int codeSequence = 0;

    @Override
    public void sendVerificationEmail(String to, String code) {
        if (shouldFailSending) {
            throw new RuntimeException("이메일 전송 실패 (테스트용)");
        }

        verificationCodes.put(to, code);
        sentEmails.add(new SentEmail(to, "이메일 인증 코드",
            "인증 코드: " + code, EmailType.VERIFICATION));
    }

    @Override
    public String generateVerificationCode() {
        return String.format("%06d", ++codeSequence);
    }

    // === 테스트 헬퍼 메서드들 ===

    /**
     * 전송된 모든 이메일 기록 초기화
     */
    public void clear() {
        sentEmails.clear();
        verificationCodes.clear();
        shouldFailSending = false;
    }

    /**
     * 전송된 이메일 개수 확인
     */
    public int getSentEmailCount() {
        return sentEmails.size();
    }

    /**
     * 특정 이메일 주소로 전송된 이메일 개수 확인
     */
    public int getSentEmailCountTo(String to) {
        return (int) sentEmails.stream()
            .filter(email -> email.to.equals(to))
            .count();
    }

    /**
     * 특정 타입의 이메일이 전송되었는지 확인
     */
    public boolean hasEmailOfType(String to, EmailType type) {
        return sentEmails.stream()
            .anyMatch(email -> email.to.equals(to) && email.type == type);
    }

    /**
     * 전송된 모든 이메일 기록 조회
     */
    public List<SentEmail> getSentEmails() {
        return new ArrayList<>(sentEmails);
    }

    /**
     * 특정 이메일 주소의 인증 코드 조회
     */
    public String getVerificationCode(String email) {
        return verificationCodes.get(email);
    }

    /**
     * 이메일 전송 실패 시뮬레이션 활성화/비활성화
     */
    public void setFailureMode(boolean shouldFail) {
        this.shouldFailSending = shouldFail;
    }

    /**
     * 특정 이메일의 가장 최근 이메일 조회
     */
    public SentEmail getLatestEmailTo(String to) {
        return sentEmails.stream()
            .filter(email -> email.to.equals(to))
            .reduce((first, second) -> second)
            .orElse(null);
    }

    // === 내부 클래스들 ===

    /**
     * 전송된 이메일 정보를 담는 클래스
     */
    public static class SentEmail {
        public final String to;
        public final String subject;
        public final String content;
        public final EmailType type;

        public SentEmail(String to, String subject, String content, EmailType type) {
            this.to = to;
            this.subject = subject;
            this.content = content;
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("SentEmail{to='%s', subject='%s', type=%s}", to, subject, type);
        }
    }

    /**
     * 이메일 타입 구분
     */
    public enum EmailType {
        VERIFICATION,
        PASSWORD_RESET,
        WELCOME
    }
}