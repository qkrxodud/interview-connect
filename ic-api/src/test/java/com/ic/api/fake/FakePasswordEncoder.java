package com.ic.api.fake;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PasswordEncoder의 메모리 기반 Fake 구현체
 * - BCrypt 없이 빠른 테스트 실행
 * - 예측 가능한 인코딩 결과
 * - 실제 암호화 로직 없이 상태 기반 검증
 * - 테스트에서 쉽게 검증 가능한 구조
 */
public class FakePasswordEncoder implements PasswordEncoder {

    // 인코딩된 비밀번호 저장소 (검증 목적)
    private final Map<String, String> encodedPasswordStore = new ConcurrentHashMap<>();

    // 인코딩 접두사
    private static final String ENCODED_PREFIX = "FAKE_ENCODED_";

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Raw password cannot be null");
        }

        final String raw = rawPassword.toString();
        final String encoded = ENCODED_PREFIX + raw;

        // 인코딩된 비밀번호 저장 (검증 목적)
        encodedPasswordStore.put(raw, encoded);

        return encoded;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        final String raw = rawPassword.toString();
        final String expectedEncoded = ENCODED_PREFIX + raw;

        return expectedEncoded.equals(encodedPassword);
    }

    /**
     * 인코딩된 비밀번호인지 확인 (테스트용)
     */
    public boolean isEncoded(String password) {
        return password != null && password.startsWith(ENCODED_PREFIX);
    }

    /**
     * 원본 비밀번호 추출 (테스트용)
     */
    public String extractRawPassword(String encodedPassword) {
        if (encodedPassword == null || !encodedPassword.startsWith(ENCODED_PREFIX)) {
            throw new IllegalArgumentException("Invalid encoded password format");
        }
        return encodedPassword.substring(ENCODED_PREFIX.length());
    }

    /**
     * 특정 비밀번호가 인코딩되었는지 확인 (테스트 헬퍼)
     */
    public boolean wasEncoded(String rawPassword) {
        return encodedPasswordStore.containsKey(rawPassword);
    }

    /**
     * 인코딩 히스토리 조회 (테스트 헬퍼)
     */
    public Map<String, String> getEncodingHistory() {
        return new ConcurrentHashMap<>(encodedPasswordStore);
    }

    /**
     * 인코딩 히스토리 초기화 (테스트 헬퍼)
     */
    public void clearEncodingHistory() {
        encodedPasswordStore.clear();
    }

    /**
     * 인코딩된 비밀번호의 수 조회 (테스트 헬퍼)
     */
    public int getEncodedPasswordCount() {
        return encodedPasswordStore.size();
    }

    // === Deprecated 메서드 (Spring Security 5.x 호환성) ===

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        // Fake 구현체는 항상 최신 인코딩으로 간주
        return false;
    }
}