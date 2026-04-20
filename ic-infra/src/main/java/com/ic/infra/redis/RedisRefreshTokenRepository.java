package com.ic.infra.redis;

import com.ic.infra.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 Refresh Token 저장소 구현체
 * - redis.enabled=false 설정 시 비활성화 (테스트용)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    /**
     * Refresh Token 저장
     */
    @Override
    public void save(Long memberId, String refreshToken) {
        if (Objects.isNull(memberId) || Objects.isNull(refreshToken)) {
            throw new IllegalArgumentException("MemberId and refreshToken cannot be null");
        }

        final String key = generateKey(memberId);
        final long ttlInSeconds = jwtProperties.getRefreshTokenExpireTime() / 1000;

        redisTemplate.opsForValue().set(key, refreshToken, ttlInSeconds, TimeUnit.SECONDS);

        log.debug("Refresh token saved: memberId={}, ttl={}s", memberId, ttlInSeconds);
    }

    /**
     * Refresh Token 조회
     */
    @Override
    public Optional<String> findByMemberId(Long memberId) {
        if (Objects.isNull(memberId)) {
            return Optional.empty();
        }

        final String key = generateKey(memberId);
        final String refreshToken = redisTemplate.opsForValue().get(key);

        return Optional.ofNullable(refreshToken);
    }

    /**
     * Refresh Token 존재 여부 확인
     */
    @Override
    public boolean existsByMemberId(Long memberId) {
        if (Objects.isNull(memberId)) {
            return false;
        }

        final String key = generateKey(memberId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Refresh Token 삭제 (로그아웃)
     */
    @Override
    public void deleteByMemberId(Long memberId) {
        if (Objects.isNull(memberId)) {
            return;
        }

        final String key = generateKey(memberId);
        final Boolean deleted = redisTemplate.delete(key);

        log.debug("Refresh token deleted: memberId={}, deleted={}", memberId, deleted);
    }

    /**
     * Refresh Token 검증 및 갱신
     */
    @Override
    public boolean validateAndRefresh(Long memberId, String providedRefreshToken, String newRefreshToken) {
        if (Objects.isNull(memberId) || Objects.isNull(providedRefreshToken) || Objects.isNull(newRefreshToken)) {
            return false;
        }

        final Optional<String> storedTokenOpt = findByMemberId(memberId);

        if (storedTokenOpt.isEmpty()) {
            log.warn("No stored refresh token found for memberId: {}", memberId);
            return false;
        }

        final String storedToken = storedTokenOpt.get();

        if (!storedToken.equals(providedRefreshToken)) {
            log.warn("Refresh token mismatch for memberId: {}", memberId);
            return false;
        }

        // 기존 토큰 검증이 완료되면 새 토큰으로 교체
        save(memberId, newRefreshToken);
        return true;
    }

    /**
     * 특정 회원의 모든 세션 무효화 (보안용)
     */
    @Override
    public void deleteAllByMemberId(Long memberId) {
        deleteByMemberId(memberId);
        log.info("All refresh tokens deleted for memberId: {}", memberId);
    }

    /**
     * Refresh Token의 남은 TTL 조회 (초 단위)
     */
    @Override
    public long getTtl(Long memberId) {
        if (Objects.isNull(memberId)) {
            return -1;
        }

        final String key = generateKey(memberId);
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Redis 키 생성
     */
    private String generateKey(Long memberId) {
        return KEY_PREFIX + memberId;
    }
}