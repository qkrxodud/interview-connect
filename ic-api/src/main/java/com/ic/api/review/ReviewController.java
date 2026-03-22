package com.ic.api.review;

import com.ic.api.config.security.AuthMember;
import com.ic.api.review.dto.ReviewCreateRequest;
import com.ic.api.review.dto.ReviewListResponse;
import com.ic.api.review.dto.ReviewResponse;
import com.ic.api.review.dto.ReviewUpdateRequest;
import com.ic.common.response.ApiResponse;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 면접 후기 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 후기 생성 (인증 필요)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createReview(
            @AuthMember Long memberId,
            @Valid @RequestBody ReviewCreateRequest request) {

        final ReviewService.CreateReviewRequest serviceRequest = new ReviewService.CreateReviewRequest(
                request.companyId(),
                request.interviewDate(),
                request.position(),
                request.interviewTypes(),
                request.questions(),
                request.difficulty(),
                request.atmosphere(),
                request.result(),
                request.content()
        );

        final InterviewReview review = reviewService.createReview(memberId, serviceRequest);
        return ApiResponse.ok(ReviewResponse.from(review));
    }

    /**
     * 후기 목록 조회 (비로그인 허용, 페이징)
     */
    @GetMapping
    public ApiResponse<Page<ReviewListResponse>> getReviews(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) InterviewResult result) {

        final Page<InterviewReview> reviews;

        if (companyId != null) {
            reviews = reviewService.getReviewsByCompany(companyId, pageable);
        } else {
            reviews = reviewService.getReviews(pageable);
        }

        final Page<ReviewListResponse> response = reviews.map(ReviewListResponse::from);
        return ApiResponse.ok(response);
    }

    /**
     * 후기 상세 조회 (비로그인 허용, 조회수 증가)
     */
    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> getReview(@PathVariable Long reviewId) {
        final InterviewReview review = reviewService.getReview(reviewId);
        return ApiResponse.ok(ReviewResponse.from(review));
    }

    /**
     * 후기 수정 (인증 필요, 본인만)
     */
    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @AuthMember Long memberId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {

        final ReviewService.UpdateReviewRequest serviceRequest = new ReviewService.UpdateReviewRequest(
                request.position(),
                request.interviewTypes(),
                request.questions(),
                request.difficulty() != null ? request.difficulty() : 0,
                request.atmosphere() != null ? request.atmosphere() : 0,
                request.result(),
                request.content()
        );

        final InterviewReview review = reviewService.updateReview(memberId, reviewId, serviceRequest);
        return ApiResponse.ok(ReviewResponse.from(review));
    }

    /**
     * 후기 삭제 (인증 필요, 본인만)
     */
    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteReview(
            @AuthMember Long memberId,
            @PathVariable Long reviewId) {

        reviewService.deleteReview(memberId, reviewId);
        return ApiResponse.ok();
    }

    /**
     * 내가 작성한 후기 목록 조회 (인증 필요)
     */
    @GetMapping("/me")
    public ApiResponse<List<ReviewListResponse>> getMyReviews(@AuthMember Long memberId) {
        final List<InterviewReview> reviews = reviewService.getUserReviews(memberId);
        final List<ReviewListResponse> response = reviews.stream()
                .map(ReviewListResponse::from)
                .toList();
        return ApiResponse.ok(response);
    }
}