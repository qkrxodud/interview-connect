package com.ic.common.test;

import org.junit.jupiter.api.BeforeEach;

/**
 * 순수한 도메인 로직 단위 테스트를 위한 베이스 클래스
 * - Spring Context 없음 (~1ms)
 * - 빠른 실행 속도
 * - 도메인 객체의 비즈니스 로직 검증에 집중
 */
public abstract class UnitTestBase {

    @BeforeEach
    void unitTestSetUp() {
        // 단위 테스트용 공통 설정
    }
}