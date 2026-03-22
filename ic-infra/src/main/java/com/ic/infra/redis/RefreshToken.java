package com.ic.infra.redis;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * Redis에 저장될 Refresh Token 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RedisHash(value = "refresh_token")
public class RefreshToken {

    @Id
    private String memberId;  // refresh:{memberId} 키 형태로 저장됨

    private String refreshToken;

    @TimeToLive
    private long ttl;  // TTL (초 단위)

    /**
     * RefreshToken 생성 정적 팩터리 메서드
     */
    public static RefreshToken of(Long memberId, String refreshToken, long ttlInSeconds) {
        return new RefreshToken(
                memberId.toString(),
                refreshToken,
                ttlInSeconds
        );
    }

    /**
     * 토큰 업데이트
     */
    public RefreshToken updateToken(String newRefreshToken, long newTtlInSeconds) {
        return new RefreshToken(this.memberId, newRefreshToken, newTtlInSeconds);
    }
}