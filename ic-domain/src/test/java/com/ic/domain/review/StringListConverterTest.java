package com.ic.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * StringListConverter JSON 변환 로직 테스트
 */
@DisplayName("문자열 리스트 컨버터")
class StringListConverterTest {

    private final StringListConverter converter = new StringListConverter();

    @Nested
    @DisplayName("데이터베이스 컬럼으로 변환")
    class ConvertToDatabaseColumnTest {

        @Test
        @DisplayName("문자열 리스트를 JSON 배열로 변환한다")
        void 문자열_리스트를_JSON_배열로_변환한다() {
            // given
            List<String> 문자열목록 = Arrays.asList("기술 면접", "인성 면접", "과제 발표");

            // when
            String json결과 = converter.convertToDatabaseColumn(문자열목록);

            // then
            assertThat(json결과).isEqualTo("[\"기술 면접\",\"인성 면접\",\"과제 발표\"]");
        }

        @Test
        @DisplayName("빈 리스트는 빈 JSON 배열로 변환한다")
        void 빈_리스트는_빈_JSON_배열로_변환한다() {
            // given
            List<String> 빈목록 = Collections.emptyList();

            // when
            String json결과 = converter.convertToDatabaseColumn(빈목록);

            // then
            assertThat(json결과).isEqualTo("[]");
        }

        @Test
        @DisplayName("null 리스트는 빈 JSON 배열로 변환한다")
        void null_리스트는_빈_JSON_배열로_변환한다() {
            // given
            List<String> null목록 = null;

            // when
            String json결과 = converter.convertToDatabaseColumn(null목록);

            // then
            assertThat(json결과).isEqualTo("[]");
        }

        @Test
        @DisplayName("단일 요소 리스트를 JSON 배열로 변환한다")
        void 단일_요소_리스트를_JSON_배열로_변환한다() {
            // given
            List<String> 단일목록 = Arrays.asList("기술 면접");

            // when
            String json결과 = converter.convertToDatabaseColumn(단일목록);

            // then
            assertThat(json결과).isEqualTo("[\"기술 면접\"]");
        }

        @Test
        @DisplayName("특수문자가 포함된 문자열을 올바르게 변환한다")
        void 특수문자가_포함된_문자열을_올바르게_변환한다() {
            // given
            List<String> 특수문자목록 = Arrays.asList("\"따옴표\"", "역슬래시\\", "줄바꿈\n");

            // when
            String json결과 = converter.convertToDatabaseColumn(특수문자목록);

            // then
            assertThat(json결과).contains("\\\"따옴표\\\"");
            assertThat(json결과).contains("역슬래시\\\\");
            assertThat(json결과).contains("줄바꿈\\n");
        }
    }

    @Nested
    @DisplayName("엔티티 속성으로 변환")
    class ConvertToEntityAttributeTest {

        @Test
        @DisplayName("JSON 배열을 문자열 리스트로 변환한다")
        void JSON_배열을_문자열_리스트로_변환한다() {
            // given
            String json데이터 = "[\"기술 면접\",\"인성 면접\",\"과제 발표\"]";

            // when
            List<String> 결과목록 = converter.convertToEntityAttribute(json데이터);

            // then
            assertThat(결과목록).hasSize(3);
            assertThat(결과목록).containsExactly("기술 면접", "인성 면접", "과제 발표");
        }

        @Test
        @DisplayName("빈 JSON 배열을 빈 리스트로 변환한다")
        void 빈_JSON_배열을_빈_리스트로_변환한다() {
            // given
            String 빈json = "[]";

            // when
            List<String> 결과목록 = converter.convertToEntityAttribute(빈json);

            // then
            assertThat(결과목록).isEmpty();
        }

        @Test
        @DisplayName("null 문자열은 빈 리스트로 변환한다")
        void null_문자열은_빈_리스트로_변환한다() {
            // given
            String null데이터 = null;

            // when
            List<String> 결과목록 = converter.convertToEntityAttribute(null데이터);

            // then
            assertThat(결과목록).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열은 빈 리스트로 변환한다")
        void 빈_문자열은_빈_리스트로_변환한다() {
            // given
            String 빈문자열 = "";

            // when
            List<String> 결과목록 = converter.convertToEntityAttribute(빈문자열);

            // then
            assertThat(결과목록).isEmpty();
        }

        @Test
        @DisplayName("공백만 있는 문자열은 빈 리스트로 변환한다")
        void 공백만_있는_문자열은_빈_리스트로_변환한다() {
            // given
            String 공백문자열 = "   ";

            // when
            List<String> 결과목록 = converter.convertToEntityAttribute(공백문자열);

            // then
            assertThat(결과목록).isEmpty();
        }

        @Test
        @DisplayName("단일 요소 JSON 배열을 리스트로 변환한다")
        void 단일_요소_JSON_배열을_리스트로_변환한다() {
            // given
            String 단일json = "[\"기술 면접\"]";

            // when
            List<String> 결과목록 = converter.convertToEntityAttribute(단일json);

            // then
            assertThat(결과목록).hasSize(1);
            assertThat(결과목록).containsExactly("기술 면접");
        }

        @Test
        @DisplayName("특수문자가 포함된 JSON을 올바르게 변환한다")
        void 특수문자가_포함된_JSON을_올바르게_변환한다() {
            // given
            String 특수문자json = "[\"\\\"따옴표\\\"\",\"역슬래시\\\\\",\"줄바꿈\\n\"]";

            // when
            List<String> 결과목록 = converter.convertToEntityAttribute(특수문자json);

            // then
            assertThat(결과목록).hasSize(3);
            assertThat(결과목록.get(0)).isEqualTo("\"따옴표\"");
            assertThat(결과목록.get(1)).isEqualTo("역슬래시\\");
            assertThat(결과목록.get(2)).isEqualTo("줄바꿈\n");
        }

        @Test
        @DisplayName("잘못된 JSON 형식이면 예외가 발생한다")
        void 잘못된_JSON_형식이면_예외가_발생한다() {
            // given
            String 잘못된json = "[기술 면접, 인성 면접]"; // 따옴표 없음

            // when & then
            assertThatThrownBy(() -> converter.convertToEntityAttribute(잘못된json))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("JSON을 문자열 목록으로 변환하는 중 오류가 발생했습니다");
        }

        @Test
        @DisplayName("JSON 배열이 아닌 형식이면 예외가 발생한다")
        void JSON_배열이_아닌_형식이면_예외가_발생한다() {
            // given
            String 잘못된형식 = "\"단일 문자열\"";

            // when & then
            assertThatThrownBy(() -> converter.convertToEntityAttribute(잘못된형식))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("JSON을 문자열 목록으로 변환하는 중 오류가 발생했습니다");
        }
    }

    @Nested
    @DisplayName("양방향 변환 검증")
    class BidirectionalConversionTest {

        @Test
        @DisplayName("리스트를 JSON으로 변환 후 다시 리스트로 변환하면 원본과 같다")
        void 리스트를_JSON으로_변환_후_다시_리스트로_변환하면_원본과_같다() {
            // given
            List<String> 원본목록 = Arrays.asList("기술 면접", "인성 면접", "과제 발표", "임원 면접");

            // when
            String json = converter.convertToDatabaseColumn(원본목록);
            List<String> 변환된목록 = converter.convertToEntityAttribute(json);

            // then
            assertThat(변환된목록).isEqualTo(원본목록);
        }

        @Test
        @DisplayName("빈 리스트의 양방향 변환이 올바르게 동작한다")
        void 빈_리스트의_양방향_변환이_올바르게_동작한다() {
            // given
            List<String> 빈목록 = Collections.emptyList();

            // when
            String json = converter.convertToDatabaseColumn(빈목록);
            List<String> 변환된목록 = converter.convertToEntityAttribute(json);

            // then
            assertThat(변환된목록).isEmpty();
        }
    }
}