package com.ic.infra.email;

import com.ic.domain.member.EmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EmailService 빈 등록 설정
 * - JavaMailSender가 없어 EmailServiceImpl이 등록되지 않을 경우
 *   FallbackEmailService를 대신 등록
 */
@Configuration
public class EmailServiceConfig {

    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService fallbackEmailService() {
        return new FallbackEmailService();
    }
}
