package com.ic.api.qa;

import com.ic.api.config.security.AuthMember;
import com.ic.api.config.security.CustomUserDetails;
import com.ic.common.response.ApiResponse;
import com.ic.domain.qa.dto.QaDto;
import com.ic.domain.qa.service.QaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Q&A API 컨트롤러
 *
 * 핵심 비즈니스 룰:
 * 1. 비로그인 사용자: Q&A 조회 시 답변 블러 처리
 * 2. 로그인 사용자: Q&A 전체 공개 + 질문 작성 가능
 * 3. 인증 회원: 답변 작성 가능
 */
@RestController
@RequestMapping("/api/v1")
public class QaController {

    private final QaService qaService;

    public QaController(final QaService qaService) {
        this.qaService = qaService;
    }

    /**
     * 특정 후기의 Q&A 목록 조회
     * GET /api/v1/reviews/{reviewId}/qa
     *
     * 비로그인: 답변 블러 처리 (content=null, blurred=true, preview=앞10자)
     * 로그인: 전체 공개
     */
    @GetMapping("/reviews/{reviewId}/qa")
    public ResponseEntity<ApiResponse<QaDto.QaListResponse>> getQaList(
            @PathVariable final Long reviewId,
            @AuthMember(required = false) CustomUserDetails userDetails) {

        final QaDto.QaListResponse response;

        if (Objects.nonNull(userDetails)) {
            // 로그인 사용자: 전체 공개
            response = qaService.getQaListForMember(reviewId);
        } else {
            // 비로그인 사용자: 답변 블러 처리
            response = qaService.getQaListForGuest(reviewId);
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 질문 작성
     * POST /api/v1/reviews/{reviewId}/questions
     *
     * 권한: 로그인 필수 (일반 회원 이상)
     */
    @PostMapping("/reviews/{reviewId}/questions")
    public ResponseEntity<ApiResponse<QaDto.QuestionResponse>> createQuestion(
            @PathVariable final Long reviewId,
            @Valid @RequestBody final QaDto.CreateQuestionRequest request,
            @AuthMember CustomUserDetails userDetails) {

        // reviewId 검증 (request의 reviewId와 path variable 일치 확인)
        if (!Objects.equals(request.reviewId(), reviewId)) {
            throw new IllegalArgumentException("경로의 후기 ID와 요청의 후기 ID가 일치하지 않습니다");
        }

        final QaDto.QuestionResponse response = qaService.createQuestion(request, userDetails.getMemberId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 답변 작성
     * POST /api/v1/questions/{questionId}/answers
     *
     * 권한: 인증 회원 이상 (VERIFIED, ADMIN)
     * 제약: 본인 질문에는 답변 불가, 중복 답변 불가
     */
    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<ApiResponse<QaDto.AnswerResponse>> createAnswer(
            @PathVariable final Long questionId,
            @Valid @RequestBody final QaDto.CreateAnswerRequest request,
            @AuthMember CustomUserDetails userDetails) {

        // questionId 검증 (request의 questionId와 path variable 일치 확인)
        if (!Objects.equals(request.questionId(), questionId)) {
            throw new IllegalArgumentException("경로의 질문 ID와 요청의 질문 ID가 일치하지 않습니다");
        }

        final QaDto.AnswerResponse response = qaService.createAnswer(request, userDetails.getMemberId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 답변 수정
     * PATCH /api/v1/answers/{answerId}
     *
     * 권한: 본인이 작성한 답변만 수정 가능
     */
    @PatchMapping("/answers/{answerId}")
    public ResponseEntity<ApiResponse<QaDto.AnswerResponse>> updateAnswer(
            @PathVariable final Long answerId,
            @Valid @RequestBody final QaDto.UpdateAnswerRequest request,
            @AuthMember CustomUserDetails userDetails) {

        // answerId 검증 (request의 answerId와 path variable 일치 확인)
        if (!Objects.equals(request.answerId(), answerId)) {
            throw new IllegalArgumentException("경로의 답변 ID와 요청의 답변 ID가 일치하지 않습니다");
        }

        final QaDto.AnswerResponse response = qaService.updateAnswer(request, userDetails.getMemberId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}