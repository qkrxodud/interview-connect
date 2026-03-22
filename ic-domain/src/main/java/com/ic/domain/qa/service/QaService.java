package com.ic.domain.qa.service;

import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import com.ic.domain.qa.ReviewAnswer;
import com.ic.domain.qa.ReviewAnswerRepository;
import com.ic.domain.qa.ReviewQuestion;
import com.ic.domain.qa.ReviewQuestionRepository;
import com.ic.domain.qa.dto.QaDto;
import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.InterviewReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Q&A 비즈니스 로직 처리 서비스
 *
 * 핵심 기능:
 * 1. Q&A 목록 조회 (비로그인/로그인 구분)
 * 2. 질문 작성 (로그인 필수)
 * 3. 답변 작성 (로그인 필수, 본인 질문 답변 불가)
 * 4. 답변 수정 (본인 답변만 가능)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QaService {
    private final ReviewQuestionRepository reviewQuestionRepository;
    private final ReviewAnswerRepository reviewAnswerRepository;
    private final InterviewReviewRepository interviewReviewRepository;
    private final MemberRepository memberRepository;

    /**
     * 특정 후기의 Q&A 목록 조회 (비로그인 사용자)
     * - 답변 내용 블러 처리 (content=null, blurred=true, preview=앞10자)
     */
    public QaDto.QaListResponse getQaListForGuest(final Long reviewId) {
        validateReviewExists(reviewId);

        final List<ReviewQuestion> questions = reviewQuestionRepository
                .findByInterviewReviewIdWithQuestioner(reviewId);

        // 각 질문의 답변들을 fetch
        questions.forEach(this::loadAnswers);

        return QaDto.QaListResponse.fromWithBlurredAnswers(questions);
    }

    /**
     * 특정 후기의 Q&A 목록 조회 (로그인 사용자)
     * - 전체 내용 공개
     */
    public QaDto.QaListResponse getQaListForMember(final Long reviewId) {
        validateReviewExists(reviewId);

        final List<ReviewQuestion> questions = reviewQuestionRepository
                .findByInterviewReviewIdWithQuestioner(reviewId);

        // 각 질문의 답변들을 fetch
        questions.forEach(this::loadAnswers);

        return QaDto.QaListResponse.from(questions);
    }

    /**
     * 질문 작성 (로그인 필수)
     */
    @Transactional
    public QaDto.QuestionResponse createQuestion(final QaDto.CreateQuestionRequest request,
                                                final Long questionerId) {
        final InterviewReview review = findReviewById(request.reviewId());
        final Member questioner = findMemberById(questionerId);

        validateCanAskQuestion(questioner);

        final ReviewQuestion question = ReviewQuestion.create(review, questioner, request.content());
        final ReviewQuestion savedQuestion = reviewQuestionRepository.save(question);

        return QaDto.QuestionResponse.from(savedQuestion);
    }

    /**
     * 답변 작성 (로그인 필수, 본인 질문 답변 불가)
     */
    @Transactional
    public QaDto.AnswerResponse createAnswer(final QaDto.CreateAnswerRequest request,
                                            final Long answererId) {
        final ReviewQuestion question = findQuestionById(request.questionId());
        final Member answerer = findMemberById(answererId);

        validateCanAnswerQuestion(answerer);
        validateNotSelfAnswer(question, answererId);
        validateNoDuplicateAnswer(request.questionId(), answererId);

        final ReviewAnswer answer = ReviewAnswer.create(question, answerer, request.content());
        final ReviewAnswer savedAnswer = reviewAnswerRepository.save(answer);

        // 양방향 연관관계 설정
        question.addAnswer(savedAnswer);

        return QaDto.AnswerResponse.from(savedAnswer);
    }

    /**
     * 답변 수정 (본인 답변만 가능)
     */
    @Transactional
    public QaDto.AnswerResponse updateAnswer(final QaDto.UpdateAnswerRequest request,
                                            final Long requesterId) {
        final ReviewAnswer answer = findAnswerById(request.answerId());

        validateAnswerOwner(answer, requesterId);

        answer.updateContent(request.content());
        final ReviewAnswer updatedAnswer = reviewAnswerRepository.save(answer);

        return QaDto.AnswerResponse.from(updatedAnswer);
    }

    // === 비공개 헬퍼 메서드 ===

    private void validateReviewExists(final Long reviewId) {
        if (!interviewReviewRepository.existsById(reviewId)) {
            throw BusinessException.of(ErrorCode.REVIEW_NOT_FOUND, "후기를 찾을 수 없습니다");
        }
    }

    private InterviewReview findReviewById(final Long reviewId) {
        return interviewReviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.REVIEW_NOT_FOUND, "후기를 찾을 수 없습니다"));
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.MEMBER_NOT_FOUND, "회원을 찾을 수 없습니다"));
    }

    private ReviewQuestion findQuestionById(final Long questionId) {
        return reviewQuestionRepository.findById(questionId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.QUESTION_NOT_FOUND, "질문을 찾을 수 없습니다"));
    }

    private ReviewAnswer findAnswerById(final Long answerId) {
        return reviewAnswerRepository.findById(answerId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ANSWER_NOT_FOUND, "답변을 찾을 수 없습니다"));
    }

    private void validateCanAskQuestion(final Member member) {
        if (!member.canAskQuestion()) {
            throw BusinessException.of(ErrorCode.FORBIDDEN, "질문 작성 권한이 없습니다");
        }
    }

    private void validateCanAnswerQuestion(final Member member) {
        if (!member.canAnswerQuestion()) {
            throw BusinessException.of(ErrorCode.ANSWER_PERMISSION_DENIED, "답변 작성 권한이 없습니다");
        }
    }

    private void validateNotSelfAnswer(final ReviewQuestion question, final Long answererId) {
        if (Objects.equals(question.getQuestioner().getId(), answererId)) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT, "자신이 작성한 질문에는 답변할 수 없습니다");
        }
    }

    private void validateNoDuplicateAnswer(final Long questionId, final Long answererId) {
        if (reviewAnswerRepository.existsByReviewQuestionIdAndAnswererId(questionId, answererId)) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT, "이미 답변을 작성했습니다");
        }
    }

    private void validateAnswerOwner(final ReviewAnswer answer, final Long requesterId) {
        if (!answer.isAnsweredBy(requesterId)) {
            throw BusinessException.of(ErrorCode.ANSWER_AUTHOR_MISMATCH, "본인이 작성한 답변만 수정할 수 있습니다");
        }
    }

    private void loadAnswers(final ReviewQuestion question) {
        final List<ReviewAnswer> answers = reviewAnswerRepository
                .findByReviewQuestionIdWithAnswerer(question.getId());

        // 기존 답변 목록 초기화 후 다시 추가 (중복 방지)
        question.getAnswers().clear();
        answers.forEach(question::addAnswer);
    }
}