package com.ic.domain.qa.dto;

import com.ic.domain.qa.ReviewAnswer;
import com.ic.domain.qa.ReviewQuestion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Q&A 관련 DTO 정의
 */
public class QaDto {

    /**
     * Q&A 질문 생성 요청
     */
    public record CreateQuestionRequest(
            Long reviewId,
            String content
    ) {
        public CreateQuestionRequest {
            if (Objects.isNull(reviewId)) {
                throw new IllegalArgumentException("후기 ID는 필수입니다");
            }
            if (Objects.isNull(content) || content.trim().isEmpty()) {
                throw new IllegalArgumentException("질문 내용은 필수입니다");
            }
        }
    }

    /**
     * Q&A 답변 생성 요청
     */
    public record CreateAnswerRequest(
            Long questionId,
            String content
    ) {
        public CreateAnswerRequest {
            if (Objects.isNull(questionId)) {
                throw new IllegalArgumentException("질문 ID는 필수입니다");
            }
            if (Objects.isNull(content) || content.trim().isEmpty()) {
                throw new IllegalArgumentException("답변 내용은 필수입니다");
            }
        }
    }

    /**
     * Q&A 답변 수정 요청
     */
    public record UpdateAnswerRequest(
            Long answerId,
            String content
    ) {
        public UpdateAnswerRequest {
            if (Objects.isNull(answerId)) {
                throw new IllegalArgumentException("답변 ID는 필수입니다");
            }
            if (Objects.isNull(content) || content.trim().isEmpty()) {
                throw new IllegalArgumentException("답변 내용은 필수입니다");
            }
        }
    }

    /**
     * Q&A 질문 응답
     */
    public record QuestionResponse(
            Long id,
            String content,
            String questionerNickname,
            LocalDateTime createdAt,
            List<AnswerResponse> answers
    ) {
        public static QuestionResponse from(final ReviewQuestion question) {
            final List<AnswerResponse> answers = question.getAnswers().stream()
                    .map(AnswerResponse::from)
                    .toList();

            return new QuestionResponse(
                    question.getId(),
                    question.getContent(),
                    question.getQuestioner().getNickname(),
                    question.getCreatedAt(),
                    answers
            );
        }

        public static QuestionResponse fromWithBlurredAnswers(final ReviewQuestion question) {
            final List<AnswerResponse> blurredAnswers = question.getAnswers().stream()
                    .map(AnswerResponse::fromBlurred)
                    .toList();

            return new QuestionResponse(
                    question.getId(),
                    question.getContent(),
                    question.getQuestioner().getNickname(),
                    question.getCreatedAt(),
                    blurredAnswers
            );
        }
    }

    /**
     * Q&A 답변 응답
     */
    public record AnswerResponse(
            Long id,
            String content,
            boolean blurred,
            String preview,
            String answererNickname,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static AnswerResponse from(final ReviewAnswer answer) {
            return new AnswerResponse(
                    answer.getId(),
                    answer.getContent(),
                    false,
                    null,
                    answer.getAnswerer().getNickname(),
                    answer.getCreatedAt(),
                    answer.getUpdatedAt()
            );
        }

        public static AnswerResponse fromBlurred(final ReviewAnswer answer) {
            final String preview = createPreview(answer.getContent());

            return new AnswerResponse(
                    answer.getId(),
                    null, // 비로그인 사용자에게는 내용 숨김
                    true,
                    preview,
                    answer.getAnswerer().getNickname(),
                    answer.getCreatedAt(),
                    answer.getUpdatedAt()
            );
        }

        private static String createPreview(final String content) {
            if (Objects.isNull(content) || content.isEmpty()) {
                return "";
            }
            if (content.length() <= 10) {
                return content;
            }
            return content.substring(0, 10) + "...";
        }
    }

    /**
     * Q&A 목록 응답
     */
    public record QaListResponse(
            List<QuestionResponse> questions,
            long totalQuestions
    ) {
        public static QaListResponse from(final List<ReviewQuestion> questions) {
            final List<QuestionResponse> questionResponses = questions.stream()
                    .map(QuestionResponse::from)
                    .toList();

            return new QaListResponse(questionResponses, questionResponses.size());
        }

        public static QaListResponse fromWithBlurredAnswers(final List<ReviewQuestion> questions) {
            final List<QuestionResponse> questionResponses = questions.stream()
                    .map(QuestionResponse::fromWithBlurredAnswers)
                    .toList();

            return new QaListResponse(questionResponses, questionResponses.size());
        }
    }
}