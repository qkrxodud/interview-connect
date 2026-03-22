package com.ic.infra.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 관련 설정 프로퍼티
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 서명에 사용할 시크릿 키
     */
    private String secret;

    /**
     * Access Token 만료 시간 (밀리초)
     */
    private long accessTokenExpireTime;

    /**
     * Refresh Token 만료 시간 (밀리초)
     */
    private long refreshTokenExpireTime;
}