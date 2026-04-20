package com.ic.domain.qa.service;

import com.ic.common.exception.BusinessException;
import com.ic.domain.company.Company;
import com.ic.domain.fake.*;
import com.ic.domain.fixture.CompanyFixture;
import com.ic.domain.fixture.MemberFixture;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;
import com.ic.domain.notification.NotificationService;
import com.ic.domain.qa.dto.QaDto;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

class QaServiceTest {

    private FakeMemberRepository memberRepository;
    private FakeInterviewReviewRepository reviewRepository;
    private FakeReviewQuestionRepository questionRepository;
    private FakeReviewAnswerRepository answerRepository;
    private FakeNotificationRepository notificationRepository;
    private NotificationService notificationService;
    private QaService qaService;

    private Member 후기_작성자;
    private Member 질문자_일반회원;
    private Member 답변자_인증회원;
    private InterviewReview 면접_후기;

    @BeforeEach
    void setUp() {
        memberRepository = new FakeMemberRepository();
        reviewRepository = new FakeInterviewReviewRepository();
        questionRepository = new FakeReviewQuestionRepository();
        answerRepository = new FakeReviewAnswerRepository();
        notificationRepository = new FakeNotificationRepository();
        notificationService = new NotificationService(notificationRepository);

        qaService = new QaService(
                questionRepository,
                answerRepository,
                reviewRepository,
                memberRepository,
                notificationService
        );

        // 기본 테스트 데이터 설정
        후기_작성자 = memberRepository.save(
                Member.builder()
                        .email("author@example.com")
                        .password("password123!")
                        .nickname("후기작성자")
                        .role(MemberRole.VERIFIED)
                        .emailVerified(true)
                        .build()
        );

        질문자_일반회원 = memberRepository.save(
                Member.builder()
                        .email("questioner@example.com")
                        .password("password123!")
                        .nickname("질문자")
                        .role(MemberRole.GENERAL)
                        .emailVerified(true)
                        .build()
        );

        답변자_인증회원 = memberRepository.save(
                Member.builder()
                        .email("answerer@example.com")
                        .password("password123!")
                        .nickname("답변자")
                        .role(MemberRole.VERIFIED)
                        .emailVerified(true)
                        .build()
        );

        final Company 회사 = CompanyFixture.카카오();

        면접_후기 = reviewRepository.save(
                InterviewReview.create(
                        후기_작성자,
                        회사,
                        LocalDate.of(2024, 1, 15),
                        "백엔드 개발자",
                        Arrays.asList("기술 면접"),
                        Arrays.asList("자기소개를 해주세요"),
                        3,
                        4,
                        InterviewResult.PASS,
                        "좋은 면접이었습니다."
                )
        );
    }

    // ======= Q&A 목록 조회 =======

    @Test
    @DisplayName("비로그인 사용자가 Q&A 목록 조회 시 답변 내용이 블러 처리된다")
    void 비로그인_QA_조회_시_답변_블러_처리() {
        // given
        final QaDto.CreateQuestionRequest 질문_요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "면접 준비는 어떻게 하셨나요?"
        );
        qaService.createQuestion(질문_요청, 질문자_일반회원.getId());

        // 답변이 있는 질문에 답변 추가
        final Long 질문_ID = questionRepository.findByInterviewReviewIdWithQuestioner(면접_후기.getId())
                .get(0).getId();
        final QaDto.CreateAnswerRequest 답변_요청 = new QaDto.CreateAnswerRequest(
                질문_ID, "코딩 테스트를 집중적으로 준비했습니다."
        );
        qaService.createAnswer(답변_요청, 답변자_인증회원.getId());

        // when
        final QaDto.QaListResponse 응답 = qaService.getQaListForGuest(면접_후기.getId());

        // then
        assertThat(응답.questions()).hasSize(1);
        assertThat(응답.totalQuestions()).isEqualTo(1);

        final QaDto.AnswerResponse 답변 = 응답.questions().get(0).answers().get(0);
        assertThat(답변.blurred()).isTrue();
        assertThat(답변.content()).isNull();
        assertThat(답변.preview()).isNotNull();
        assertThat(답변.preview()).hasSize(10 + 3); // "코딩 테스트를 집" (10자) + "..."
    }

    @Test
    @DisplayName("로그인 사용자가 Q&A 목록 조회 시 답변 전체 내용이 공개된다")
    void 로그인_QA_조회_시_답변_전체_공개() {
        // given
        final QaDto.CreateQuestionRequest 질문_요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "면접 준비는 어떻게 하셨나요?"
        );
        qaService.createQuestion(질문_요청, 질문자_일반회원.getId());

        final Long 질문_ID = questionRepository.findByInterviewReviewIdWithQuestioner(면접_후기.getId())
                .get(0).getId();
        final QaDto.CreateAnswerRequest 답변_요청 = new QaDto.CreateAnswerRequest(
                질문_ID, "코딩 테스트를 집중적으로 준비했습니다."
        );
        qaService.createAnswer(답변_요청, 답변자_인증회원.getId());

        // when
        final QaDto.QaListResponse 응답 = qaService.getQaListForMember(면접_후기.getId());

        // then
        assertThat(응답.questions()).hasSize(1);

        final QaDto.AnswerResponse 답변 = 응답.questions().get(0).answers().get(0);
        assertThat(답변.blurred()).isFalse();
        assertThat(답변.content()).isEqualTo("코딩 테스트를 집중적으로 준비했습니다.");
        assertThat(답변.preview()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 후기 조회 시 BusinessException이 발생한다")
    void 존재하지_않는_후기_QA_조회_시_예외_발생() {
        // given
        final Long 존재하지않는_후기_ID = 9999L;

        // when & then
        assertThatThrownBy(() -> qaService.getQaListForGuest(존재하지않는_후기_ID))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> qaService.getQaListForMember(존재하지않는_후기_ID))
                .isInstanceOf(BusinessException.class);
    }

    // ======= 질문 작성 =======

    @Test
    @DisplayName("일반 회원이 후기에 질문을 작성하면 질문이 저장된다")
    void 일반_회원이_질문_작성_성공() {
        // given
        final QaDto.CreateQuestionRequest 요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "면접 분위기가 어땠나요?"
        );

        // when
        final QaDto.QuestionResponse 응답 = qaService.createQuestion(요청, 질문자_일반회원.getId());

        // then
        assertThat(응답.id()).isNotNull();
        assertThat(응답.content()).isEqualTo("면접 분위기가 어땠나요?");
        assertThat(응답.questionerNickname()).isEqualTo("질문자");
        assertThat(questionRepository.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("질문 작성 시 후기 작성자에게 알림이 발송된다")
    void 질문_작성_시_후기_작성자에게_알림_발송() {
        // given
        final QaDto.CreateQuestionRequest 요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "기술 면접에서 어떤 질문이 나왔나요?"
        );

        // when
        qaService.createQuestion(요청, 질문자_일반회원.getId());

        // then
        assertThat(notificationRepository.findAllForRecipient(후기_작성자.getId())).hasSize(1);
    }

    @Test
    @DisplayName("후기 작성자 본인이 질문 작성 시 자신에게 알림이 발송되지 않는다")
    void 후기_작성자_본인_질문_시_알림_미발송() {
        // given
        final QaDto.CreateQuestionRequest 요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "본인이 작성한 질문입니다."
        );

        // when
        qaService.createQuestion(요청, 후기_작성자.getId());

        // then
        assertThat(notificationRepository.findAllForRecipient(후기_작성자.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 후기에 질문 작성 시 BusinessException이 발생한다")
    void 존재하지_않는_후기에_질문_작성_시_예외_발생() {
        // given
        final QaDto.CreateQuestionRequest 요청 = new QaDto.CreateQuestionRequest(
                9999L, "존재하지 않는 후기 질문"
        );

        // when & then
        assertThatThrownBy(() -> qaService.createQuestion(요청, 질문자_일반회원.getId()))
                .isInstanceOf(BusinessException.class);
    }

    // ======= 답변 작성 =======

    @Test
    @DisplayName("인증 회원이 다른 회원의 질문에 답변을 작성하면 답변이 저장된다")
    void 인증_회원이_답변_작성_성공() {
        // given
        final QaDto.CreateQuestionRequest 질문_요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "면접 준비 기간은 얼마나 되나요?"
        );
        qaService.createQuestion(질문_요청, 질문자_일반회원.getId());

        final Long 질문_ID = questionRepository.findByInterviewReviewIdWithQuestioner(면접_후기.getId())
                .get(0).getId();
        final QaDto.CreateAnswerRequest 답변_요청 = new QaDto.CreateAnswerRequest(
                질문_ID, "약 3개월 준비했습니다."
        );

        // when
        final QaDto.AnswerResponse 응답 = qaService.createAnswer(답변_요청, 답변자_인증회원.getId());

        // then
        assertThat(응답.id()).isNotNull();
        assertThat(응답.content()).isEqualTo("약 3개월 준비했습니다.");
        assertThat(응답.answererNickname()).isEqualTo("답변자");
        assertThat(answerRepository.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("답변 작성 시 질문 작성자에게 알림이 발송된다")
    void 답변_작성_시_질문자에게_알림_발송() {
        // given
        final QaDto.CreateQuestionRequest 질문_요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "합격 후 어떻게 됐나요?"
        );
        qaService.createQuestion(질문_요청, 질문자_일반회원.getId());

        final Long 질문_ID = questionRepository.findByInterviewReviewIdWithQuestioner(면접_후기.getId())
                .get(0).getId();
        final QaDto.CreateAnswerRequest 답변_요청 = new QaDto.CreateAnswerRequest(
                질문_ID, "입사 후 만족하며 다니고 있습니다."
        );
        notificationRepository.clear(); // 질문 작성 시 발생한 알림 초기화

        // when
        qaService.createAnswer(답변_요청, 답변자_인증회원.getId());

        // then
        assertThat(notificationRepository.findAllForRecipient(질문자_일반회원.getId())).hasSize(1);
    }

    @Test
    @DisplayName("자신이 작성한 질문에는 답변할 수 없다")
    void 자신의_질문에_답변_시_예외_발생() {
        // given
        final QaDto.CreateQuestionRequest 질문_요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "자신이 작성한 질문입니다."
        );
        qaService.createQuestion(질문_요청, 질문자_일반회원.getId());

        final Long 질문_ID = questionRepository.findByInterviewReviewIdWithQuestioner(면접_후기.getId())
                .get(0).getId();
        final QaDto.CreateAnswerRequest 답변_요청 = new QaDto.CreateAnswerRequest(
                질문_ID, "자기 자신의 답변 시도"
        );

        // when & then
        assertThatThrownBy(() -> qaService.createAnswer(답변_요청, 질문자_일반회원.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("동일 질문에 중복 답변 작성 시 BusinessException이 발생한다")
    void 중복_답변_작성_시_예외_발생() {
        // given
        final QaDto.CreateQuestionRequest 질문_요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "중복 답변 테스트 질문"
        );
        qaService.createQuestion(질문_요청, 질문자_일반회원.getId());

        final Long 질문_ID = questionRepository.findByInterviewReviewIdWithQuestioner(면접_후기.getId())
                .get(0).getId();
        final QaDto.CreateAnswerRequest 첫번째_답변 = new QaDto.CreateAnswerRequest(질문_ID, "첫 번째 답변");
        qaService.createAnswer(첫번째_답변, 답변자_인증회원.getId());

        final QaDto.CreateAnswerRequest 두번째_답변 = new QaDto.CreateAnswerRequest(질문_ID, "두 번째 답변 시도");

        // when & then
        assertThatThrownBy(() -> qaService.createAnswer(두번째_답변, 답변자_인증회원.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("일반 회원은 답변을 작성할 수 없다")
    void 일반_회원은_답변_작성_불가() {
        // given
        final Member 다른_질문자 = memberRepository.save(
                Member.builder()
                        .email("another@example.com")
                        .password("password123!")
                        .nickname("다른질문자")
                        .role(MemberRole.GENERAL)
                        .emailVerified(true)
                        .build()
        );

        final QaDto.CreateQuestionRequest 질문_요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "일반 회원 답변 권한 테스트"
        );
        qaService.createQuestion(질문_요청, 다른_질문자.getId());

        final Long 질문_ID = questionRepository.findByInterviewReviewIdWithQuestioner(면접_후기.getId())
                .get(0).getId();
        final QaDto.CreateAnswerRequest 답변_요청 = new QaDto.CreateAnswerRequest(질문_ID, "일반 회원 답변 시도");

        // when & then
        assertThatThrownBy(() -> qaService.createAnswer(답변_요청, 질문자_일반회원.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("존재하지 않는 질문에 답변 작성 시 BusinessException이 발생한다")
    void 존재하지_않는_질문에_답변_작성_시_예외_발생() {
        // given
        final QaDto.CreateAnswerRequest 답변_요청 = new QaDto.CreateAnswerRequest(9999L, "존재하지 않는 질문 답변");

        // when & then
        assertThatThrownBy(() -> qaService.createAnswer(답변_요청, 답변자_인증회원.getId()))
                .isInstanceOf(BusinessException.class);
    }

    // ======= 답변 수정 =======

    @Test
    @DisplayName("본인이 작성한 답변을 수정할 수 있다")
    void 본인_답변_수정_성공() {
        // given
        final QaDto.CreateQuestionRequest 질문_요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "수정 테스트 질문"
        );
        qaService.createQuestion(질문_요청, 질문자_일반회원.getId());

        final Long 질문_ID = questionRepository.findByInterviewReviewIdWithQuestioner(면접_후기.getId())
                .get(0).getId();
        final QaDto.AnswerResponse 원본_답변 = qaService.createAnswer(
                new QaDto.CreateAnswerRequest(질문_ID, "원본 답변 내용"),
                답변자_인증회원.getId()
        );

        final QaDto.UpdateAnswerRequest 수정_요청 = new QaDto.UpdateAnswerRequest(
                원본_답변.id(), "수정된 답변 내용"
        );

        // when
        final QaDto.AnswerResponse 수정된_답변 = qaService.updateAnswer(수정_요청, 답변자_인증회원.getId());

        // then
        assertThat(수정된_답변.id()).isEqualTo(원본_답변.id());
        assertThat(수정된_답변.content()).isEqualTo("수정된 답변 내용");
    }

    @Test
    @DisplayName("타인의 답변을 수정하려 하면 BusinessException이 발생한다")
    void 타인_답변_수정_시_예외_발생() {
        // given
        final QaDto.CreateQuestionRequest 질문_요청 = new QaDto.CreateQuestionRequest(
                면접_후기.getId(), "권한 테스트 질문"
        );
        qaService.createQuestion(질문_요청, 질문자_일반회원.getId());

        final Long 질문_ID = questionRepository.findByInterviewReviewIdWithQuestioner(면접_후기.getId())
                .get(0).getId();
        final QaDto.AnswerResponse 답변 = qaService.createAnswer(
                new QaDto.CreateAnswerRequest(질문_ID, "작성된 답변"),
                답변자_인증회원.getId()
        );

        final Member 다른_인증회원 = memberRepository.save(
                Member.builder()
                        .email("other@example.com")
                        .password("password123!")
                        .nickname("다른인증회원")
                        .role(MemberRole.VERIFIED)
                        .emailVerified(true)
                        .build()
        );
        final QaDto.UpdateAnswerRequest 수정_요청 = new QaDto.UpdateAnswerRequest(답변.id(), "무단 수정 시도");

        // when & then
        assertThatThrownBy(() -> qaService.updateAnswer(수정_요청, 다른_인증회원.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("존재하지 않는 답변 수정 시 BusinessException이 발생한다")
    void 존재하지_않는_답변_수정_시_예외_발생() {
        // given
        final QaDto.UpdateAnswerRequest 수정_요청 = new QaDto.UpdateAnswerRequest(9999L, "없는 답변 수정");

        // when & then
        assertThatThrownBy(() -> qaService.updateAnswer(수정_요청, 답변자_인증회원.getId()))
                .isInstanceOf(BusinessException.class);
    }
}
