package com.ic.domain.review;

import com.ic.domain.fixture.CompanyFixture;
import com.ic.domain.fixture.InterviewReviewFixture;
import com.ic.domain.fixture.MemberFixture;
import com.ic.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * 리팩토링된 InterviewReview 엔티티 테스트
 */
@DisplayName("면접 후기 엔티티 (리팩토링)")
class RefactoredInterviewReviewTest {

    @Nested
    @DisplayName("면접 후기 생성")
    class CreateTest {

        @Test
        @DisplayName("픽스처를 사용한 후기 생성이 정상 동작한다")
        void 픽스처를_사용한_후기_생성이_정상_동작한다() {
            // given & when
            InterviewReview 백엔드후기 = InterviewReviewFixture.백엔드_합격_후기();
            InterviewReview 프론트후기 = InterviewReviewFixture.프론트엔드_불합격_후기();
            InterviewReview 대기후기 = InterviewReviewFixture.대기중_후기();

            // then
            assertThat(백엔드후기.getPosition()).isEqualTo("백엔드 개발자");
            assertThat(백엔드후기.getResult()).isEqualTo(InterviewResult.PASS);

            assertThat(프론트후기.getPosition()).isEqualTo("프론트엔드 개발자");
            assertThat(프론트후기.getResult()).isEqualTo(InterviewResult.FAIL);

            assertThat(대기후기.getResult()).isEqualTo(InterviewResult.PENDING);
        }

        @Test
        @DisplayName("유효성 검증이 올바르게 동작한다")
        void 유효성_검증이_올바르게_동작한다() {
            // given
            Member 회원 = MemberFixture.인증회원();
            var 회사 = CompanyFixture.카카오();

            // when & then - 난이도 검증
            assertThatThrownBy(() ->
                    InterviewReview.create(회원, 회사, LocalDate.now(), "개발자",
                            Arrays.asList(), Arrays.asList(), 0, 3, InterviewResult.PASS, "내용")
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("난이도는 1~5 사이의 값이어야 합니다");

            // 분위기 검증
            assertThatThrownBy(() ->
                    InterviewReview.create(회원, 회사, LocalDate.now(), "개발자",
                            Arrays.asList(), Arrays.asList(), 3, 6, InterviewResult.PASS, "내용")
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("분위기는 1~5 사이의 값이어야 합니다");

            // 포지션 검증
            assertThatThrownBy(() ->
                    InterviewReview.create(회원, 회사, LocalDate.now(), null,
                            Arrays.asList(), Arrays.asList(), 3, 4, InterviewResult.PASS, "내용")
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("포지션은 필수입니다");
        }
    }

    @Nested
    @DisplayName("비즈니스 로직")
    class BusinessLogicTest {

        @Test
        @DisplayName("후기 내용 변경이 정상 동작한다")
        void 후기_내용_변경이_정상_동작한다() {
            // given
            InterviewReview 후기 = InterviewReviewFixture.백엔드_합격_후기();
            String 새내용 = "변경된 후기 내용입니다.";

            // when
            후기.changeContent(새내용);

            // then
            assertThat(후기.getContent()).isEqualTo(새내용);
        }

        @Test
        @DisplayName("조회수 증가가 정상 동작한다")
        void 조회수_증가가_정상_동작한다() {
            // given
            InterviewReview 후기 = InterviewReviewFixture.백엔드_합격_후기();
            long 초기조회수 = 후기.getViewCount();

            // when
            후기.increaseViewCount();

            // then
            assertThat(후기.getViewCount()).isEqualTo(초기조회수 + 1);
        }

        @Test
        @DisplayName("작성자 확인이 정상 동작한다")
        void 작성자_확인이_정상_동작한다() {
            // given
            Member 회원 = MemberFixture.ID가_있는_인증회원(1L);
            InterviewReview 후기 = InterviewReviewFixture.후기_생성(
                    회원, CompanyFixture.카카오(), "개발자", 3, 4, InterviewResult.PASS
            );

            // when & then
            assertThat(후기.isWrittenBy(1L)).isTrue();
            assertThat(후기.isWrittenBy(999L)).isFalse();
        }

        @Test
        @DisplayName("컨텐츠 및 질문 존재 여부 확인이 정상 동작한다")
        void 컨텐츠_및_질문_존재_여부_확인이_정상_동작한다() {
            // given
            InterviewReview 내용있는후기 = InterviewReviewFixture.백엔드_합격_후기();
            InterviewReview 내용없는후기 = InterviewReviewFixture.상세_후기_생성(
                    MemberFixture.인증회원(), CompanyFixture.카카오(), LocalDate.now(),
                    "개발자", Arrays.asList("면접"), null, 3, 4, InterviewResult.PASS, null
            );

            // when & then
            assertThat(내용있는후기.hasContent()).isTrue();
            assertThat(내용있는후기.hasQuestions()).isTrue();

            assertThat(내용없는후기.hasContent()).isFalse();
            assertThat(내용없는후기.hasQuestions()).isFalse();
        }
    }
}