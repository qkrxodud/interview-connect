package com.ic.common.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 서비스 레이어 테스트를 위한 베이스 클래스
 * - Mockito 기반 (~5ms)
 * - @Mock, @InjectMocks 사용
 * - 비즈니스 로직 검증에 집중
 */
@ExtendWith(MockitoExtension.class)
public abstract class ServiceTestBase {

    @BeforeEach
    void serviceTestSetUp() {
        // 서비스 테스트용 공통 설정
    }
}