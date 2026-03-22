package com.ic.infra.redis.fake;

import com.ic.infra.redis.RefreshTokenRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * RefreshTokenRepository의 메모리 기반 Fake 구현체
 * - Redis 없이 테스트 가능
 * - 간단한 메모리 저장소 사용
 * - 빠른 테스트 실행
 */
public class FakeRefreshTokenRepository implements RefreshTokenRepository {

    private final Map<Long, String> tokenStore = new HashMap<>();

    @Override
    public void save(final Long memberId, final String refreshToken) {
        tokenStore.put(memberId, refreshToken);
    }

    @Override
    public Optional<String> findByMemberId(final Long memberId) {
        return Optional.ofNullable(tokenStore.get(memberId));
    }

    @Override
    public boolean existsByMemberId(final Long memberId) {
        return tokenStore.containsKey(memberId);
    }

    @Override
    public void deleteByMemberId(final Long memberId) {
        tokenStore.remove(memberId);
    }

    @Override
    public boolean validateAndRefresh(final Long memberId, final String providedRefreshToken, final String newRefreshToken) {
        final Optional<String> storedToken = findByMemberId(memberId);
        if (storedToken.isEmpty()) {
            return false;
        }

        if (!storedToken.get().equals(providedRefreshToken)) {
            return false;
        }

        // 새 토큰으로 교체
        save(memberId, newRefreshToken);
        return true;
    }

    @Override
    public void deleteAllByMemberId(final Long memberId) {
        deleteByMemberId(memberId);
    }

    @Override
    public long getTtl(final Long memberId) {
        return hasToken(memberId) ? 3600L : -1L; // 1시간으로 고정 (테스트용)
    }

    // === 테스트 헬퍼 메서드 ===

    /**
     * 토큰 저장소 초기화
     */
    public void clear() {
        tokenStore.clear();
    }

    /**
     * 저장된 토큰 수 조회
     */
    public int size() {
        return tokenStore.size();
    }

    /**
     * 특정 회원의 토큰 존재 여부 확인
     */
    public boolean hasToken(final Long memberId) {
        return tokenStore.containsKey(memberId);
    }

    /**
     * 모든 토큰 조회 (테스트용)
     */
    public Map<Long, String> findAll() {
        return new HashMap<>(tokenStore);
    }
}