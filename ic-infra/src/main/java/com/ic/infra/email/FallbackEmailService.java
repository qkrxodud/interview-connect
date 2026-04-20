package com.ic.infra.email;

import com.ic.domain.member.EmailService;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

/**
 * 메일 서버 미설정 시 사용되는 폴백 이메일 서비스
 * - JavaMailSender 빈이 없을 때 활성화 (EmailServiceImpl 대신 등록)
 * - 실제 이메일 발송 대신 로그로 출력 (로컬 개발, 테스트 환경)
 * - EmailServiceConfig에서 @ConditionalOnMissingBean으로 등록
 */
@Slf4j
public class FallbackEmailService implements EmailService {

    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 6;

    @Override
    public void sendVerificationEmail(final String toEmail, final String verificationCode) {
        log.warn("=== [FallbackEmailService] 이메일 발송 생략 (메일 서버 미설정) ===");
        log.warn("수신자: {}", toEmail);
        log.warn("인증 코드: {}", verificationCode);
        log.warn("=======================================================");
    }

    @Override
    public String generateVerificationCode() {
        final SecureRandom random = new SecureRandom();
        final StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }
}
