package com.ic.infra.persistence;

import com.ic.domain.company.Company;
import com.ic.domain.member.Member;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.InterviewReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * JpaInterviewReviewRepository 통합 테스트
 */
@DataJpaTest
@EnableJpaAuditing
@EntityScan(basePackages = {"com.ic.domain", "com.ic.infra"})
@EnableJpaRepositories(basePackages = {"com.ic.domain", "com.ic.infra"})
@DisplayName("면접 후기 Repository 통합 테스트")
class JpaInterviewReviewRepositoryTest {

    @Autowired
    private InterviewReviewRepository interviewReviewRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Member 테스트회원1;
    private Member 테스트회원2;
    private Company 테스트회사1;
    private Company 테스트회사2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        테스트회원1 = Member.builder()
                .email("user1@example.com")
                .password("password123")
                .nickname("테스터1")
                .build();
        entityManager.persist(테스트회원1);

        테스트회원2 = Member.builder()
                .email("user2@example.com")
                .password("password123")
                .nickname("테스터2")
                .build();
        entityManager.persist(테스트회원2);

        테스트회사1 = Company.builder()
                .name("테스트 회사1")
                .industry("IT")
                .build();
        entityManager.persist(테스트회사1);

        테스트회사2 = Company.builder()
                .name("테스트 회사2")
                .industry("금융")
                .build();
        entityManager.persist(테스트회사2);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("기본 CRUD")
    class BasicCrudTest {

        @Test
        @DisplayName("면접 후기를 저장할 수 있다")
        void 면접후기를_저장할_수_있다() {
            // given
            InterviewReview 면접후기 = InterviewReview.create(
                    테스트회원1, 테스트회사1, LocalDate.of(2024, 1, 15),
                    "백엔드 개발자", Arrays.asList("기술 면접"), Arrays.asList("질문1"),
                    3, 4, InterviewResult.PASS, "좋은 면접이었습니다."
            );

            // when
            InterviewReview 저장된후기 = interviewReviewRepository.save(면접후기);

            // then
            assertThat(저장된후기.getId()).isNotNull();
            assertThat(저장된후기.getPosition()).isEqualTo("백엔드 개발자");
            assertThat(저장된후기.getMember().getId()).isEqualTo(테스트회원1.getId());
        }

        @Test
        @DisplayName("ID로 면접 후기를 조회할 수 있다")
        void ID로_면접후기를_조회할_수_있다() {
            // given
            InterviewReview 면접후기 = 면접후기_픽스처(테스트회원1, 테스트회사1, "백엔드 개발자", 3);
            InterviewReview 저장된후기 = interviewReviewRepository.save(면접후기);

            // when
            Optional<InterviewReview> 조회된후기 = interviewReviewRepository.findById(저장된후기.getId());

            // then
            assertThat(조회된후기).isPresent();
            assertThat(조회된후기.get().getPosition()).isEqualTo("백엔드 개발자");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void 존재하지_않는_ID로_조회하면_빈_Optional을_반환한다() {
            // when
            Optional<InterviewReview> 조회된후기 = interviewReviewRepository.findById(999L);

            // then
            assertThat(조회된후기).isEmpty();
        }

        @Test
        @DisplayName("면접 후기를 삭제할 수 있다")
        void 면접후기를_삭제할_수_있다() {
            // given
            InterviewReview 면접후기 = 면접후기_픽스처(테스트회원1, 테스트회사1, "백엔드 개발자", 3);
            InterviewReview 저장된후기 = interviewReviewRepository.save(면접후기);

            // when
            interviewReviewRepository.deleteById(저장된후기.getId());

            // then
            Optional<InterviewReview> 조회된후기 = interviewReviewRepository.findById(저장된후기.getId());
            assertThat(조회된후기).isEmpty();
        }
    }

    @Nested
    @DisplayName("단일 조건 필터링")
    class SingleFilterTest {

        @BeforeEach
        void setUp() {
            // 테스트 데이터 생성
            InterviewReview 후기1 = 면접후기_픽스처(테스트회원1, 테스트회사1, "백엔드 개발자", 3);
            InterviewReview 후기2 = 면접후기_픽스처(테스트회원1, 테스트회사1, "프론트엔드 개발자", 4);
            InterviewReview 후기3 = 면접후기_픽스처(테스트회원2, 테스트회사2, "백엔드 개발자", 5);

            interviewReviewRepository.save(후기1);
            interviewReviewRepository.save(후기2);
            interviewReviewRepository.save(후기3);
        }

        @Test
        @DisplayName("회사 ID로 필터링하여 조회할 수 있다")
        void 회사_ID로_필터링하여_조회할_수_있다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<InterviewReview> 결과 = interviewReviewRepository.findByCompanyId(테스트회사1.getId(), pageable);

            // then
            assertThat(결과.getContent()).hasSize(2);
            assertThat(결과.getContent())
                    .allMatch(review -> review.getCompany().getId().equals(테스트회사1.getId()));
        }

        @Test
        @DisplayName("포지션으로 필터링하여 조회할 수 있다")
        void 포지션으로_필터링하여_조회할_수_있다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<InterviewReview> 결과 = interviewReviewRepository.findByPosition("백엔드 개발자", pageable);

            // then
            assertThat(결과.getContent()).hasSize(2);
            assertThat(결과.getContent())
                    .allMatch(review -> review.getPosition().equals("백엔드 개발자"));
        }

        @Test
        @DisplayName("난이도로 필터링하여 조회할 수 있다")
        void 난이도로_필터링하여_조회할_수_있다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<InterviewReview> 결과 = interviewReviewRepository.findByDifficulty(3, pageable);

            // then
            assertThat(결과.getContent()).hasSize(1);
            assertThat(결과.getContent().get(0).getDifficulty()).isEqualTo(3);
        }

        @Test
        @DisplayName("면접 결과로 필터링하여 조회할 수 있다")
        void 면접_결과로_필터링하여_조회할_수_있다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<InterviewReview> 결과 = interviewReviewRepository.findByResult(InterviewResult.PASS, pageable);

            // then
            assertThat(결과.getContent()).hasSize(3);
            assertThat(결과.getContent())
                    .allMatch(review -> review.getResult() == InterviewResult.PASS);
        }
    }

    @Nested
    @DisplayName("복합 조건 필터링")
    class MultipleFilterTest {

        @BeforeEach
        void setUp() {
            // 다양한 조건의 테스트 데이터 생성
            InterviewReview 후기1 = 면접후기_픽스처(테스트회원1, 테스트회사1, "백엔드 개발자", 3);
            InterviewReview 후기2 = 면접후기_픽스처(테스트회원1, 테스트회사1, "프론트엔드 개발자", 3);
            InterviewReview 후기3 = 면접후기_픽스처(테스트회원2, 테스트회사1, "백엔드 개발자", 4);
            InterviewReview 후기4 = 면접후기_픽스처(테스트회원2, 테스트회사2, "백엔드 개발자", 3);

            interviewReviewRepository.save(후기1);
            interviewReviewRepository.save(후기2);
            interviewReviewRepository.save(후기3);
            interviewReviewRepository.save(후기4);
        }

        @Test
        @DisplayName("회사 ID와 포지션으로 필터링하여 조회할 수 있다")
        void 회사_ID와_포지션으로_필터링하여_조회할_수_있다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<InterviewReview> 결과 = interviewReviewRepository
                    .findByCompanyIdAndPosition(테스트회사1.getId(), "백엔드 개발자", pageable);

            // then
            assertThat(결과.getContent()).hasSize(2);
            assertThat(결과.getContent()).allMatch(review ->
                    review.getCompany().getId().equals(테스트회사1.getId()) &&
                            review.getPosition().equals("백엔드 개발자")
            );
        }

        @Test
        @DisplayName("회사 ID와 난이도로 필터링하여 조회할 수 있다")
        void 회사_ID와_난이도로_필터링하여_조회할_수_있다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<InterviewReview> 결과 = interviewReviewRepository
                    .findByCompanyIdAndDifficulty(테스트회사1.getId(), 3, pageable);

            // then
            assertThat(결과.getContent()).hasSize(2);
            assertThat(결과.getContent()).allMatch(review ->
                    review.getCompany().getId().equals(테스트회사1.getId()) &&
                            review.getDifficulty() == 3
            );
        }

        @Test
        @DisplayName("포지션과 난이도로 필터링하여 조회할 수 있다")
        void 포지션과_난이도로_필터링하여_조회할_수_있다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<InterviewReview> 결과 = interviewReviewRepository
                    .findByPositionAndDifficulty("백엔드 개발자", 3, pageable);

            // then
            assertThat(결과.getContent()).hasSize(2);
            assertThat(결과.getContent()).allMatch(review ->
                    review.getPosition().equals("백엔드 개발자") &&
                            review.getDifficulty() == 3
            );
        }

        @Test
        @DisplayName("모든 필터 조건으로 조회할 수 있다")
        void 모든_필터_조건으로_조회할_수_있다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<InterviewReview> 결과 = interviewReviewRepository
                    .findByAllFilters(테스트회사1.getId(), "백엔드 개발자", 3, InterviewResult.PASS, pageable);

            // then
            assertThat(결과.getContent()).hasSize(1);
            assertThat(결과.getContent()).allMatch(review ->
                    review.getCompany().getId().equals(테스트회사1.getId()) &&
                            review.getPosition().equals("백엔드 개발자") &&
                            review.getDifficulty() == 3 &&
                            review.getResult() == InterviewResult.PASS
            );
        }

        @Test
        @DisplayName("모든 필터 조건이 null이면 전체 조회한다")
        void 모든_필터_조건이_null이면_전체_조회한다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<InterviewReview> 결과 = interviewReviewRepository
                    .findByAllFilters(null, null, null, null, pageable);

            // then
            assertThat(결과.getContent()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("회원별 조회")
    class MemberFilterTest {

        @Test
        @DisplayName("특정 회원이 작성한 면접 후기를 모두 조회할 수 있다")
        void 특정_회원이_작성한_면접후기를_모두_조회할_수_있다() {
            // given
            InterviewReview 후기1 = 면접후기_픽스처(테스트회원1, 테스트회사1, "백엔드 개발자", 3);
            InterviewReview 후기2 = 면접후기_픽스처(테스트회원1, 테스트회사2, "프론트엔드 개발자", 4);
            InterviewReview 후기3 = 면접후기_픽스처(테스트회원2, 테스트회사1, "백엔드 개발자", 5);

            interviewReviewRepository.save(후기1);
            interviewReviewRepository.save(후기2);
            interviewReviewRepository.save(후기3);

            // when
            List<InterviewReview> 결과 = interviewReviewRepository.findByMemberId(테스트회원1.getId());

            // then
            assertThat(결과).hasSize(2);
            assertThat(결과).allMatch(review -> review.getMember().getId().equals(테스트회원1.getId()));
        }

        @Test
        @DisplayName("면접 후기를 작성하지 않은 회원은 빈 리스트를 반환한다")
        void 면접후기를_작성하지_않은_회원은_빈_리스트를_반환한다() {
            // when
            List<InterviewReview> 결과 = interviewReviewRepository.findByMemberId(테스트회원1.getId());

            // then
            assertThat(결과).isEmpty();
        }
    }

    @Nested
    @DisplayName("페이징 처리")
    class PagingTest {

        @BeforeEach
        void setUp() {
            // 페이징 테스트를 위한 다수의 데이터 생성
            for (int i = 0; i < 15; i++) {
                InterviewReview 후기 = 면접후기_픽스처(테스트회원1, 테스트회사1, "포지션" + i, 3);
                interviewReviewRepository.save(후기);
            }
        }

        @Test
        @DisplayName("페이징 조회가 올바르게 동작한다")
        void 페이징_조회가_올바르게_동작한다() {
            // given
            Pageable 첫번째페이지 = PageRequest.of(0, 5);
            Pageable 두번째페이지 = PageRequest.of(1, 5);

            // when
            Page<InterviewReview> 첫번째결과 = interviewReviewRepository.findAll(첫번째페이지);
            Page<InterviewReview> 두번째결과 = interviewReviewRepository.findAll(두번째페이지);

            // then
            assertThat(첫번째결과.getContent()).hasSize(5);
            assertThat(두번째결과.getContent()).hasSize(5);
            assertThat(첫번째결과.getTotalElements()).isEqualTo(15);
            assertThat(첫번째결과.getTotalPages()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("집계 메서드")
    class AggregateMethodTest {

        @Test
        @DisplayName("전체 면접 후기 수를 조회할 수 있다")
        void 전체_면접후기_수를_조회할_수_있다() {
            // given
            InterviewReview 후기1 = 면접후기_픽스처(테스트회원1, 테스트회사1, "백엔드 개발자", 3);
            InterviewReview 후기2 = 면접후기_픽스처(테스트회원2, 테스트회사2, "프론트엔드 개발자", 4);

            interviewReviewRepository.save(후기1);
            interviewReviewRepository.save(후기2);

            // when
            long 총개수 = interviewReviewRepository.count();

            // then
            assertThat(총개수).isEqualTo(2);
        }

        @Test
        @DisplayName("면접 후기 존재 여부를 확인할 수 있다")
        void 면접후기_존재_여부를_확인할_수_있다() {
            // given
            InterviewReview 후기 = 면접후기_픽스처(테스트회원1, 테스트회사1, "백엔드 개발자", 3);
            InterviewReview 저장된후기 = interviewReviewRepository.save(후기);

            // when & then
            assertThat(interviewReviewRepository.existsById(저장된후기.getId())).isTrue();
            assertThat(interviewReviewRepository.existsById(999L)).isFalse();
        }
    }

    // 테스트 픽스처 메서드
    private InterviewReview 면접후기_픽스처(Member 회원, Company 회사, String 포지션, int 난이도) {
        return InterviewReview.create(
                회원, 회사, LocalDate.of(2024, 1, 15), 포지션,
                Arrays.asList("기술 면접"), Arrays.asList("질문1"), 난이도, 4,
                InterviewResult.PASS, "후기 내용"
        );
    }
}