package com.ic.domain.comment.service;

import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.domain.comment.Comment;
import com.ic.domain.comment.CommentRepository;
import com.ic.domain.comment.CommentVisibility;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.InterviewReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final InterviewReviewRepository interviewReviewRepository;

    @Transactional
    public Comment createComment(final Long memberId, final Long reviewId, final String content) {
        return createComment(memberId, reviewId, content, CommentVisibility.PUBLIC);
    }

    @Transactional
    public Comment createComment(final Long memberId, final Long reviewId, final String content, final CommentVisibility visibility) {
        validateMemberExists(memberId);
        validateReviewExists(reviewId);

        final Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

        final InterviewReview review = interviewReviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.REVIEW_NOT_FOUND));

        final Comment comment = Comment.create(author, review, content, visibility);
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment createReply(final Long memberId, final Long reviewId, final Long parentId, final String content) {
        return createReply(memberId, reviewId, parentId, content, CommentVisibility.PUBLIC);
    }

    @Transactional
    public Comment createReply(final Long memberId, final Long reviewId, final Long parentId, final String content, final CommentVisibility visibility) {
        validateMemberExists(memberId);
        validateReviewExists(reviewId);

        final Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

        final InterviewReview review = interviewReviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.REVIEW_NOT_FOUND));

        final Comment parentComment = findCommentById(parentId);

        final Comment reply = Comment.createReply(author, review, content, parentComment, visibility);
        return commentRepository.save(reply);
    }

    @Transactional
    public Comment updateComment(final Long commentId, final Long memberId, final String newContent) {
        validateMemberExists(memberId);

        final Comment comment = findCommentById(commentId);
        comment.changeContent(memberId, newContent);

        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(final Long commentId, final Long memberId) {
        validateMemberExists(memberId);

        final Comment comment = findCommentById(commentId);
        comment.delete(memberId);

        commentRepository.save(comment);
    }

    public List<Comment> getCommentsByReviewId(final Long reviewId) {
        return getCommentsByReviewIdWithViewerId(reviewId, null);
    }

    public List<Comment> getCommentsByReviewIdWithViewerId(final Long reviewId, final Long viewerId) {
        validateReviewExists(reviewId);

        final List<Comment> comments = commentRepository.findTopLevelCommentsByReviewId(reviewId);

        // 모든 댓글을 반환 (권한 체크는 뷰에서 처리)
        return comments;
    }

    public Page<Comment> getCommentsByReviewId(final Long reviewId, final Pageable pageable) {
        validateReviewExists(reviewId);
        return commentRepository.findByInterviewReviewIdAndDeletedFalseOrderByCreatedAtAsc(reviewId, pageable);
    }

    public long getCommentCountByReviewId(final Long reviewId) {
        validateReviewExists(reviewId);
        return commentRepository.countByInterviewReviewIdAndDeletedFalse(reviewId);
    }

    public Comment findCommentById(final Long commentId) {
        if (Objects.isNull(commentId)) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "댓글 ID는 필수입니다");
        }

        return commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void validateMemberExists(final Long memberId) {
        if (Objects.isNull(memberId)) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "회원 ID는 필수입니다");
        }

        if (!memberRepository.existsById(memberId)) {
            throw BusinessException.from(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Transactional
    public Comment changeCommentVisibility(final Long commentId, final Long memberId, final CommentVisibility visibility) {
        validateMemberExists(memberId);

        final Comment comment = findCommentById(commentId);
        comment.changeVisibility(memberId, visibility);

        return commentRepository.save(comment);
    }

    private List<Comment> filterVisibleComments(final List<Comment> comments, final Long viewerId, final Long reviewId) {
        final Long reviewAuthorId = getReviewAuthorId(reviewId);

        return comments.stream()
                .filter(comment -> comment.isVisibleTo(viewerId, reviewAuthorId))
                .map(comment -> filterReplies(comment, viewerId, reviewAuthorId))
                .toList();
    }

    private Comment filterReplies(final Comment comment, final Long viewerId, final Long reviewAuthorId) {
        comment.getVisibleReplies(viewerId, reviewAuthorId);
        return comment;
    }

    private Long getReviewAuthorId(final Long reviewId) {
        return interviewReviewRepository.findById(reviewId)
                .map(review -> review.getMember().getId())
                .orElseThrow(() -> BusinessException.from(ErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateReviewExists(final Long reviewId) {
        if (Objects.isNull(reviewId)) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "후기 ID는 필수입니다");
        }

        if (!interviewReviewRepository.existsById(reviewId)) {
            throw BusinessException.from(ErrorCode.REVIEW_NOT_FOUND);
        }
    }
}