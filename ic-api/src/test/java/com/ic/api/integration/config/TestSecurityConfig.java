package com.ic.api.integration.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 테스트용 Spring Security 설정
 * - JWT 필터 없이 기본 인증만 사용
 * - Fake 구현체들과 호환되도록 단순화
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (테스트용)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 세션 관리 - Stateless (테스트 간소화)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 인증/인가 설정 - 테스트 환경 규칙
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // 비로그인 허용 웹 페이지
                .requestMatchers(HttpMethod.GET, "/", "/reviews", "/reviews/**").permitAll()

                // 비로그인 허용 API (모든 GET 요청 허용 - 404 응답이 Security가 아닌 DispatcherServlet에서 반환되도록)
                .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()

                // 인증 관련 API
                .requestMatchers("/api/v1/auth/**").permitAll()

                // H2 콘솔
                .requestMatchers("/h2-console/**").permitAll()

                // 헬스체크
                .requestMatchers("/actuator/health").permitAll()

                // POST/PUT/DELETE는 인증 필요 (401 반환)
                .anyRequest().authenticated()
            )

            // 미인증 요청 시 401 응답 (기본값 403 대신)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"AUTH_001\",\"message\":\"인증이 필요합니다\",\"status\":401}}");
                })
            )

            // HTTP Basic 인증 비활성화 (401 응답용)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)

            // H2 콘솔을 위한 설정
            .headers(headers ->
                headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // PasswordEncoder는 IntegrationTestFakesConfig에서 FakePasswordEncoder로 등록
}