package com.ic.infra.persistence;

import com.ic.domain.company.Company;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.InterviewReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * 간단한 JpaInterviewReviewRepository 통합 테스트
 */
@SpringBootTest
@Transactional
@DisplayName("면접 후기 Repository 간단 테스트")
class SimpleJpaInterviewReviewRepositoryTest {

    @Autowired
    private InterviewReviewRepository interviewReviewRepository;

    @Test
    @DisplayName("면접 후기를 저장하고 조회할 수 있다")
    void 면접후기를_저장하고_조회할_수_있다() {
        // given
        Member 회원 = Member.builder()
                .email("test@example.com")
                .password("password123")
                .nickname("테스터")
                .role(MemberRole.VERIFIED)
                .build();

        Company 회사 = Company.builder()
                .name("테스트 회사")
                .industry("IT")
                .build();

        InterviewReview 면접후기 = InterviewReview.create(
                회원, 회사, LocalDate.of(2024, 1, 15),
                "백엔드 개발자", Arrays.asList("기술 면접"), Arrays.asList("질문1"),
                3, 4, InterviewResult.PASS, "좋은 면접이었습니다."
        );

        // when
        InterviewReview 저장된후기 = interviewReviewRepository.save(면접후기);

        // then
        assertThat(저장된후기.getId()).isNotNull();
        assertThat(저장된후기.getPosition()).isEqualTo("백엔드 개발자");
        assertThat(저장된후기.getDifficulty()).isEqualTo(3);
        assertThat(저장된후기.getAtmosphere()).isEqualTo(4);
        assertThat(저장된후기.getResult()).isEqualTo(InterviewResult.PASS);
    }

    @Test
    @DisplayName("저장된 면접 후기의 개수를 확인할 수 있다")
    void 저장된_면접후기의_개수를_확인할_수_있다() {
        // given
        Member 회원 = Member.builder()
                .email("test@example.com")
                .password("password123")
                .nickname("테스터")
                .role(MemberRole.VERIFIED)
                .build();

        Company 회사 = Company.builder()
                .name("테스트 회사")
                .industry("IT")
                .build();

        InterviewReview 면접후기1 = InterviewReview.create(
                회원, 회사, LocalDate.of(2024, 1, 15),
                "백엔드 개발자", Arrays.asList("기술 면접"), Arrays.asList("질문1"),
                3, 4, InterviewResult.PASS, "첫 번째 후기"
        );

        InterviewReview 면접후기2 = InterviewReview.create(
                회원, 회사, LocalDate.of(2024, 1, 16),
                "프론트엔드 개발자", Arrays.asList("인성 면접"), Arrays.asList("질문2"),
                4, 5, InterviewResult.FAIL, "두 번째 후기"
        );

        // when
        interviewReviewRepository.save(면접후기1);
        interviewReviewRepository.save(면접후기2);
        long 총개수 = interviewReviewRepository.count();

        // then
        assertThat(총개수).isGreaterThanOrEqualTo(2);
    }
}