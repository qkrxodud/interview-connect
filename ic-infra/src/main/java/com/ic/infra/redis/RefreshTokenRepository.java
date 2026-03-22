package com.ic.infra.redis;

import java.util.Optional;

/**
 * Refresh Token 저장소 인터페이스
 */
public interface RefreshTokenRepository {

    /**
     * Refresh Token 저장
     */
    void save(Long memberId, String refreshToken);

    /**
     * Refresh Token 조회
     */
    Optional<String> findByMemberId(Long memberId);

    /**
     * Refresh Token 존재 여부 확인
     */
    boolean existsByMemberId(Long memberId);

    /**
     * Refresh Token 삭제 (로그아웃)
     */
    void deleteByMemberId(Long memberId);

    /**
     * Refresh Token 검증 및 갱신
     */
    boolean validateAndRefresh(Long memberId, String providedRefreshToken, String newRefreshToken);

    /**
     * 특정 회원의 모든 세션 무효화 (보안용)
     */
    void deleteAllByMemberId(Long memberId);

    /**
     * Refresh Token의 남은 TTL 조회 (초 단위)
     */
    long getTtl(Long memberId);
}