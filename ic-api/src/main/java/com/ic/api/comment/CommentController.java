package com.ic.api.comment;

import com.ic.api.comment.dto.CommentCreateRequest;
import com.ic.api.comment.dto.CommentResponse;
import com.ic.api.comment.dto.CommentUpdateRequest;
import com.ic.api.config.security.AuthMember;
import com.ic.common.response.ApiResponse;
import com.ic.domain.comment.Comment;
import com.ic.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 댓글 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 생성 (인증 필요)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createComment(
            @AuthMember Long memberId,
            @Valid @RequestBody CommentCreateRequest request) {

        final Comment comment = commentService.createComment(
                memberId,
                request.reviewId(),
                request.content()
        );

        return ApiResponse.ok(CommentResponse.from(comment));
    }

    /**
     * 대댓글 생성 (인증 필요)
     */
    @PostMapping("/{parentId}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createReply(
            @PathVariable Long parentId,
            @AuthMember Long memberId,
            @Valid @RequestBody CommentCreateRequest request) {

        final Comment reply = commentService.createReply(
                memberId,
                request.reviewId(),
                parentId,
                request.content()
        );

        return ApiResponse.ok(CommentResponse.from(reply));
    }

    /**
     * 댓글 수정 (인증 필요)
     */
    @PatchMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @AuthMember Long memberId,
            @Valid @RequestBody CommentUpdateRequest request) {

        final Comment comment = commentService.updateComment(
                commentId,
                memberId,
                request.content()
        );

        return ApiResponse.ok(CommentResponse.from(comment));
    }

    /**
     * 댓글 삭제 (인증 필요)
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthMember Long memberId) {

        commentService.deleteComment(commentId, memberId);
        return ApiResponse.ok();
    }

    /**
     * 후기별 댓글 목록 조회 (비로그인 허용)
     */
    @GetMapping
    public ApiResponse<List<CommentResponse>> getCommentsByReviewId(
            @RequestParam Long reviewId) {

        final List<Comment> comments = commentService.getCommentsByReviewId(reviewId);
        final List<CommentResponse> responses = comments.stream()
                .map(CommentResponse::from)
                .toList();

        return ApiResponse.ok(responses);
    }

    /**
     * 후기별 댓글 목록 조회 (페이징, 비로그인 허용)
     */
    @GetMapping("/paged")
    public ApiResponse<Page<CommentResponse>> getCommentsByReviewIdWithPaging(
            @RequestParam Long reviewId,
            @PageableDefault(size = 10) Pageable pageable) {

        final Page<Comment> comments = commentService.getCommentsByReviewId(reviewId, pageable);
        final Page<CommentResponse> responses = comments.map(CommentResponse::from);

        return ApiResponse.ok(responses);
    }

    /**
     * 후기별 댓글 개수 조회 (비로그인 허용)
     */
    @GetMapping("/count")
    public ApiResponse<Long> getCommentCountByReviewId(@RequestParam Long reviewId) {
        final long count = commentService.getCommentCountByReviewId(reviewId);
        return ApiResponse.ok(count);
    }
}