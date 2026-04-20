package com.ic.api.web;

import com.ic.api.config.security.AuthMember;
import com.ic.api.review.dto.ReviewCreateRequest;
import com.ic.api.review.dto.ReviewListResponse;
import com.ic.api.review.dto.ReviewResponse;
import com.ic.api.review.dto.ReviewUpdateRequest;
import com.ic.domain.company.Company;
import com.ic.domain.company.CompanyService;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.service.ReviewService;
import com.ic.domain.comment.Comment;
import com.ic.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;

/**
 * 면접 후기 웹 컨트롤러 (Thymeleaf)
 */
@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewWebController {

    private final ReviewService reviewService;
    private final CompanyService companyService;
    private final CommentService commentService;

    /**
     * 후기 목록 페이지 (메인 페이지)
     */
    @GetMapping
    public String reviewList(
            Model model,
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

        final Page<ReviewListResponse> reviewResponses = reviews.map(ReviewListResponse::from);
        final List<Company> companies = companyService.getAllCompanies();

        model.addAttribute("reviews", reviewResponses);
        model.addAttribute("companies", companies);
        model.addAttribute("selectedCompanyId", companyId);
        model.addAttribute("selectedPosition", position);
        model.addAttribute("selectedDifficulty", difficulty);
        model.addAttribute("selectedResult", result);
        model.addAttribute("interviewResults", InterviewResult.values());

        return "reviews/list";
    }

    /**
     * 후기 상세 페이지
     */
    @GetMapping("/{reviewId}")
    public String reviewDetail(@PathVariable Long reviewId, Model model, Authentication authentication) {
        final InterviewReview review = reviewService.getReview(reviewId);
        final ReviewResponse reviewResponse = ReviewResponse.from(review);

        // 로그인 여부 및 사용자 ID 확인
        final boolean isAuthenticated = (authentication != null && authentication.isAuthenticated() &&
                                       !"anonymousUser".equals(authentication.getPrincipal()));

        final Long currentUserId;
        if (isAuthenticated && authentication.getPrincipal() instanceof com.ic.api.config.security.CustomUserDetails userDetails) {
            currentUserId = userDetails.getMemberId();
        } else {
            currentUserId = null;
        }

        // 댓글 목록 조회 (가시성 권한 적용)
        final List<Comment> comments = commentService.getCommentsByReviewIdWithViewerId(reviewId, currentUserId);
        final long commentCount = commentService.getCommentCountByReviewId(reviewId);

        // SEO 제목/설명 설정
        final String pageTitle = reviewResponse.companyName() + " " + reviewResponse.position() + " 면접 후기 — 난이도 " + reviewResponse.difficulty() + "/5";
        final String rawContent = reviewResponse.content() != null ? reviewResponse.content() : "";
        final String description = rawContent.length() > 150 ? rawContent.substring(0, 150) + "..." : rawContent;

        model.addAttribute("title", pageTitle);
        model.addAttribute("description", description);
        model.addAttribute("review", reviewResponse);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("currentUserId", currentUserId);
        return "reviews/detail";
    }

    /**
     * 후기 작성 폼 페이지 (인증 필요)
     */
    @GetMapping("/new")
    public String createReviewForm(Model model) {
        final List<Company> companies = companyService.getAllCompanies();

        model.addAttribute("reviewCreateRequest", new ReviewCreateRequest(
                null, null, "", List.of(), List.of(), null, null, null, ""));
        model.addAttribute("companies", companies);
        model.addAttribute("interviewTypes", getInterviewTypes());
        model.addAttribute("interviewResults", InterviewResult.values());

        return "reviews/form";
    }

    /**
     * 후기 작성 처리 (인증 필요)
     */
    @PostMapping("/new")
    public String createReview(
            @AuthMember Long memberId,
            @Valid @ModelAttribute ReviewCreateRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            final List<Company> companies = companyService.getAllCompanies();
            model.addAttribute("companies", companies);
            model.addAttribute("interviewTypes", getInterviewTypes());
            model.addAttribute("interviewResults", InterviewResult.values());
            return "reviews/form";
        }

        try {
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
            redirectAttributes.addFlashAttribute("message", "후기가 성공적으로 작성되었습니다.");
            return "redirect:/reviews/" + review.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "후기 작성 중 오류가 발생했습니다.");
            return "redirect:/reviews/new";
        }
    }

    /**
     * 후기 수정 폼 페이지 (인증 필요, 본인만)
     */
    @GetMapping("/{reviewId}/edit")
    public String editReviewForm(@PathVariable Long reviewId, @AuthMember Long memberId, Model model) {
        final InterviewReview review = reviewService.getReview(reviewId);

        // 본인 확인 (추후 서비스에서 처리할 수도 있음)
        if (!review.getMember().getId().equals(memberId)) {
            return "redirect:/reviews/" + reviewId;
        }

        final List<Company> companies = companyService.getAllCompanies();
        final ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
                review.getPosition(),
                review.getInterviewTypes(),
                review.getQuestions(),
                review.getDifficulty(),
                review.getAtmosphere(),
                review.getResult(),
                review.getContent()
        );

        model.addAttribute("review", review);
        model.addAttribute("reviewUpdateRequest", updateRequest);
        model.addAttribute("companies", companies);
        model.addAttribute("interviewTypes", getInterviewTypes());
        model.addAttribute("interviewResults", InterviewResult.values());

        return "reviews/edit";
    }

    /**
     * 후기 수정 처리 (인증 필요, 본인만)
     */
    @PostMapping("/{reviewId}/edit")
    public String updateReview(
            @PathVariable Long reviewId,
            @AuthMember Long memberId,
            @Valid @ModelAttribute ReviewUpdateRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            final InterviewReview review = reviewService.getReview(reviewId);
            final List<Company> companies = companyService.getAllCompanies();

            model.addAttribute("review", review);
            model.addAttribute("companies", companies);
            model.addAttribute("interviewTypes", getInterviewTypes());
            model.addAttribute("interviewResults", InterviewResult.values());
            return "reviews/edit";
        }

        try {
            final ReviewService.UpdateReviewRequest serviceRequest = new ReviewService.UpdateReviewRequest(
                    request.position(),
                    request.interviewTypes(),
                    request.questions(),
                    request.difficulty() != null ? request.difficulty() : 0,
                    request.atmosphere() != null ? request.atmosphere() : 0,
                    request.result(),
                    request.content()
            );

            reviewService.updateReview(memberId, reviewId, serviceRequest);
            redirectAttributes.addFlashAttribute("message", "후기가 성공적으로 수정되었습니다.");
            return "redirect:/reviews/" + reviewId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "후기 수정 중 오류가 발생했습니다.");
            return "redirect:/reviews/" + reviewId + "/edit";
        }
    }

    /**
     * 후기 삭제 처리 (인증 필요, 본인만)
     */
    @PostMapping("/{reviewId}/delete")
    public String deleteReview(
            @PathVariable Long reviewId,
            @AuthMember Long memberId,
            RedirectAttributes redirectAttributes) {

        try {
            reviewService.deleteReview(memberId, reviewId);
            redirectAttributes.addFlashAttribute("message", "후기가 성공적으로 삭제되었습니다.");
            return "redirect:/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "후기 삭제 중 오류가 발생했습니다.");
            return "redirect:/reviews/" + reviewId;
        }
    }

    /**
     * 댓글 작성 처리 (인증 필요)
     */
    @PostMapping("/{reviewId}/comments")
    public String createComment(
            @PathVariable Long reviewId,
            @AuthMember Long memberId,
            @RequestParam String content,
            @RequestParam(required = false) String visibility,
            RedirectAttributes redirectAttributes) {

        try {
            final com.ic.domain.comment.CommentVisibility commentVisibility =
                "AUTHOR_ONLY".equals(visibility) ?
                    com.ic.domain.comment.CommentVisibility.AUTHOR_ONLY :
                    com.ic.domain.comment.CommentVisibility.PUBLIC;

            commentService.createComment(memberId, reviewId, content, commentVisibility);
            redirectAttributes.addFlashAttribute("message", "댓글이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "댓글 작성 중 오류가 발생했습니다.");
        }

        return "redirect:/reviews/" + reviewId;
    }

    /**
     * 대댓글 작성 처리 (인증 필요)
     */
    @PostMapping("/{reviewId}/comments/{parentId}/replies")
    public String createReply(
            @PathVariable Long reviewId,
            @PathVariable Long parentId,
            @AuthMember Long memberId,
            @RequestParam String content,
            @RequestParam(required = false) String visibility,
            RedirectAttributes redirectAttributes) {

        try {
            final com.ic.domain.comment.CommentVisibility commentVisibility =
                "AUTHOR_ONLY".equals(visibility) ?
                    com.ic.domain.comment.CommentVisibility.AUTHOR_ONLY :
                    com.ic.domain.comment.CommentVisibility.PUBLIC;

            commentService.createReply(memberId, reviewId, parentId, content, commentVisibility);
            redirectAttributes.addFlashAttribute("message", "답글이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "답글 작성 중 오류가 발생했습니다.");
        }

        return "redirect:/reviews/" + reviewId;
    }

    /**
     * 댓글 삭제 처리 (인증 필요, 본인만)
     */
    @PostMapping("/{reviewId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long reviewId,
            @PathVariable Long commentId,
            @AuthMember Long memberId,
            RedirectAttributes redirectAttributes) {

        try {
            commentService.deleteComment(commentId, memberId);
            redirectAttributes.addFlashAttribute("message", "댓글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "댓글 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/reviews/" + reviewId;
    }

    /**
     * 내가 작성한 후기 목록 페이지 (인증 필요)
     */
    @GetMapping("/me")
    public String myReviews(@AuthMember Long memberId, Model model) {
        final List<InterviewReview> reviews = reviewService.getUserReviews(memberId);
        final List<ReviewListResponse> reviewResponses = reviews.stream()
                .map(ReviewListResponse::from)
                .toList();

        // 통계 계산
        final long passCount = reviews.stream()
                .mapToLong(review -> review.getResult() == InterviewResult.PASS ? 1 : 0)
                .sum();

        final long totalViewCount = reviews.stream()
                .mapToLong(InterviewReview::getViewCount)
                .sum();

        model.addAttribute("reviews", reviewResponses);
        model.addAttribute("passCount", passCount);
        model.addAttribute("totalViewCount", totalViewCount);
        return "reviews/my-reviews";
    }

    /**
     * 면접 유형 목록 반환
     */
    private List<String> getInterviewTypes() {
        return Arrays.asList(
                "기술면접",
                "인성면접",
                "임원면접",
                "화상면접",
                "전화면접",
                "그룹면접",
                "발표면접",
                "코딩테스트"
        );
    }
}