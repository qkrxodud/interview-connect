package com.ic.infra.jwt;

import com.ic.domain.member.MemberRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * JWT 인증 필터
 * 요청의 Authorization 헤더에서 JWT 토큰을 추출하고 검증한다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 요청에서 JWT 토큰 추출
        final String token = extractTokenFromRequest(request);

        // 토큰이 유효한 경우 SecurityContext에 인증 정보 설정
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            try {
                final Long memberId = jwtTokenProvider.getMemberIdFromToken(token);
                final MemberRole memberRole = jwtTokenProvider.getMemberRoleFromToken(token);

                // Authentication 객체 생성
                final Authentication authentication = createAuthentication(memberId, memberRole);

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authentication successful: memberId={}, role={}", memberId, memberRole);

            } catch (Exception e) {
                log.warn("JWT authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        final String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Authentication 객체 생성
     */
    private Authentication createAuthentication(Long memberId, MemberRole memberRole) {
        // Principal: 회원 ID
        final String principal = memberId.toString();

        // Credentials: 필요 없음 (이미 인증됨)
        final Object credentials = null;

        // Authorities: 회원 역할 기반 권한 생성
        final List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + memberRole.name())
        );

        return new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
    }
}