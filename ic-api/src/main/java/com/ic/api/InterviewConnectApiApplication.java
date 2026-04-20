package com.ic.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.ic")
@EntityScan(basePackages = {"com.ic.domain.member", "com.ic.domain.company", "com.ic.domain.review", "com.ic.domain.qa", "com.ic.domain.notification", "com.ic.domain.comment"})
@EnableJpaRepositories(basePackages = {"com.ic.domain.member", "com.ic.domain.company", "com.ic.domain.review", "com.ic.domain.qa", "com.ic.domain.notification", "com.ic.domain.comment"})
@EnableJpaAuditing
public class InterviewConnectApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewConnectApiApplication.class, args);
    }
}