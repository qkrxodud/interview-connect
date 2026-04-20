package com.ic.api.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ic.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BaseApiWebClientTest의 TypeReference fromJson 메서드 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BaseApiWebClientTypeReferenceTest extends BaseApiWebClientTest {

    @Test
    @DisplayName("TypeReference로 제네릭 타입 JSON 파싱 성공")
    void shouldParseJsonWithTypeReference() {
        // given
        final String json = """
            {
                "success": true,
                "data": {"name": "test", "value": 123},
                "error": null
            }
            """;

        // when
        final ApiResponse<Object> result = fromJson(json, new TypeReference<ApiResponse<Object>>() {});

        // then
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isNotNull();
        assertThat(result.error()).isNull();
    }

    @Test
    @DisplayName("Class 타입으로도 여전히 정상 동작")
    void shouldStillWorkWithClassType() {
        // given
        final String json = """
            {
                "success": false,
                "data": null,
                "error": {"code": "ERROR", "message": "Test error"}
            }
            """;

        // when
        @SuppressWarnings("unchecked")
        final ApiResponse<Object> result = (ApiResponse<Object>) fromJson(json, ApiResponse.class);

        // then
        assertThat(result.success()).isFalse();
        assertThat(result.data()).isNull();
        assertThat(result.error()).isNotNull();
    }

    @Test
    @DisplayName("잘못된 JSON 형식일 때 예외 발생")
    void shouldThrowExceptionForInvalidJson() {
        // given
        final String invalidJson = "{ invalid json }";

        // when & then
        assertThatThrownBy(() -> fromJson(invalidJson, new TypeReference<ApiResponse<Object>>() {}))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("JSON 파싱 실패");
    }

    @Test
    @DisplayName("null JSON일 때 예외 발생")
    void shouldThrowExceptionForNullJson() {
        // when & then
        assertThatThrownBy(() -> fromJson(null, new TypeReference<ApiResponse<Object>>() {}))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("JSON 파싱 실패");
    }
}