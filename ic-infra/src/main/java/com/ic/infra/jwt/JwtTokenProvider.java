package com.ic.infra.jwt;

import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.domain.member.MemberRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Long memberId, MemberRole role) {
        final Date now = new Date();
        final Date expiration = new Date(now.getTime() + jwtProperties.getAccessTokenExpireTime());

        return Jwts.builder()
                .setSubject(memberId.toString())
                .claim("role", role.name())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Long memberId) {
        final Date now = new Date();
        final Date expiration = new Date(now.getTime() + jwtProperties.getRefreshTokenExpireTime());

        return Jwts.builder()
                .setSubject(memberId.toString())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            final Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);

            // 만료시간 확인
            final Date expiration = claimsJws.getBody().getExpiration();
            return expiration.after(new Date());

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (SecurityException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 회원 ID 추출
     */
    public Long getMemberIdFromToken(String token) {
        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            final String subject = claims.getSubject();
            return Long.valueOf(subject);

        } catch (ExpiredJwtException e) {
            throw BusinessException.from(ErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            log.warn("Failed to extract member ID from token: {}", e.getMessage());
            throw BusinessException.from(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰에서 회원 역할 추출
     */
    public MemberRole getMemberRoleFromToken(String token) {
        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            final String roleString = claims.get("role", String.class);
            return MemberRole.valueOf(roleString);

        } catch (ExpiredJwtException e) {
            throw BusinessException.from(ErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            log.warn("Failed to extract member role from token: {}", e.getMessage());
            throw BusinessException.from(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰의 남은 만료 시간 조회 (밀리초)
     */
    public long getTokenRemainingTime(String token) {
        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            final Date expiration = claims.getExpiration();
            final Date now = new Date();

            return Math.max(0, expiration.getTime() - now.getTime());

        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Secret Key 생성
     */
    private SecretKey getSecretKey() {
        final byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}