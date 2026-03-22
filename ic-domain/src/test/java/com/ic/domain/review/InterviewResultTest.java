package com.ic.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * InterviewResult enum 테스트
 */
@DisplayName("면접 결과")
class InterviewResultTest {

    @Nested
    @DisplayName("결과 설명")
    class DescriptionTest {

        @Test
        @DisplayName("PASS는 합격 설명을 반환한다")
        void PASS는_합격_설명을_반환한다() {
            // when & then
            assertThat(InterviewResult.PASS.getDescription()).isEqualTo("합격");
        }

        @Test
        @DisplayName("FAIL은 불합격 설명을 반환한다")
        void FAIL은_불합격_설명을_반환한다() {
            // when & then
            assertThat(InterviewResult.FAIL.getDescription()).isEqualTo("불합격");
        }

        @Test
        @DisplayName("PENDING은 결과대기 설명을 반환한다")
        void PENDING은_결과대기_설명을_반환한다() {
            // when & then
            assertThat(InterviewResult.PENDING.getDescription()).isEqualTo("결과 대기");
        }
    }

    @Nested
    @DisplayName("상태 체크 메서드")
    class StatusCheckTest {

        @Test
        @DisplayName("PASS는 합격 상태를 올바르게 체크한다")
        void PASS는_합격_상태를_올바르게_체크한다() {
            // when & then
            assertThat(InterviewResult.PASS.isPass()).isTrue();
            assertThat(InterviewResult.PASS.isFail()).isFalse();
            assertThat(InterviewResult.PASS.isPending()).isFalse();
        }

        @Test
        @DisplayName("FAIL은 불합격 상태를 올바르게 체크한다")
        void FAIL은_불합격_상태를_올바르게_체크한다() {
            // when & then
            assertThat(InterviewResult.FAIL.isFail()).isTrue();
            assertThat(InterviewResult.FAIL.isPass()).isFalse();
            assertThat(InterviewResult.FAIL.isPending()).isFalse();
        }

        @Test
        @DisplayName("PENDING은 대기 상태를 올바르게 체크한다")
        void PENDING은_대기_상태를_올바르게_체크한다() {
            // when & then
            assertThat(InterviewResult.PENDING.isPending()).isTrue();
            assertThat(InterviewResult.PENDING.isPass()).isFalse();
            assertThat(InterviewResult.PENDING.isFail()).isFalse();
        }
    }

    @Nested
    @DisplayName("enum 값 검증")
    class EnumValueTest {

        @Test
        @DisplayName("모든 enum 값이 정의되어 있다")
        void 모든_enum_값이_정의되어_있다() {
            // when
            InterviewResult[] 모든값 = InterviewResult.values();

            // then
            assertThat(모든값).hasSize(3);
            assertThat(모든값).containsExactly(
                    InterviewResult.PASS,
                    InterviewResult.FAIL,
                    InterviewResult.PENDING
            );
        }

        @Test
        @DisplayName("문자열로 enum 값을 찾을 수 있다")
        void 문자열로_enum_값을_찾을_수_있다() {
            // when & then
            assertThat(InterviewResult.valueOf("PASS")).isEqualTo(InterviewResult.PASS);
            assertThat(InterviewResult.valueOf("FAIL")).isEqualTo(InterviewResult.FAIL);
            assertThat(InterviewResult.valueOf("PENDING")).isEqualTo(InterviewResult.PENDING);
        }

        @Test
        @DisplayName("존재하지 않는 문자열로 enum 값을 찾으면 예외가 발생한다")
        void 존재하지_않는_문자열로_enum_값을_찾으면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> InterviewResult.valueOf("UNKNOWN"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}