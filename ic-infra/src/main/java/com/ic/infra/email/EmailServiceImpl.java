package com.ic.infra.email;

import com.ic.domain.member.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * 이메일 발송 서비스 구현체
 * - JavaMailSender가 없으면 로그로 대체 (로컬 개발 환경 대응)
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 6;
    private static final String FROM_EMAIL = "qkrxodud00@gmail.com";
    private static final String FROM_NAME = "Interview Connect";

    // JavaMailSender가 없는 환경(메일 설정 미완료)에서도 기동되도록 optional 주입
    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Override
    public void sendVerificationEmail(final String toEmail, final String verificationCode) {
        // 메일 서버 미설정 시 로그로 대체
        if (javaMailSender == null) {
            log.warn("=== [EmailService] JavaMailSender not configured — printing code to log ===");
            log.warn("To: {}", toEmail);
            log.warn("Verification Code: {}", verificationCode);
            log.warn("=======================================================================");
            return;
        }

        try {
            final MimeMessage message = javaMailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            // 발신자 설정
            helper.setFrom(FROM_EMAIL, FROM_NAME);

            // 수신자 설정
            helper.setTo(toEmail);

            // 제목 설정
            helper.setSubject("[Interview Connect] 이메일 인증 코드");

            // HTML 내용 설정
            final String htmlContent = loadEmailTemplate(verificationCode);
            helper.setText(htmlContent, true);

            // 이메일 발송
            javaMailSender.send(message);

            log.info("Successfully sent verification email to: {}", toEmail);
            log.info("Verification code: {}", verificationCode);

        } catch (final MessagingException | IOException e) {
            log.error("Failed to send verification email to: {}", toEmail, e);

            // 이메일 발송 실패 시 로그로 대체 (로컬 개발용)
            log.warn("=== 이메일 발송 실패 - 로그로 대체 ===");
            log.warn("수신자: {}", toEmail);
            log.warn("인증 코드: {}", verificationCode);
            log.warn("===============================");

            // 실제 운영 환경에서는 예외를 던지거나 다른 처리 방식 사용
        }
    }

    @Override
    public String generateVerificationCode() {
        final SecureRandom random = new SecureRandom();
        final StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            final int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }

    /**
     * 이메일 템플릿을 로드하고 인증 코드를 삽입합니다.
     */
    private String loadEmailTemplate(final String verificationCode) throws IOException {
        final ClassPathResource resource = new ClassPathResource("templates/email/verification-email.html");
        final String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        return template.replace("{{verificationCode}}", verificationCode);
    }
}