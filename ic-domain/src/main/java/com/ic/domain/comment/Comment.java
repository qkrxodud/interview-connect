package com.ic.domain.comment;

import com.ic.common.entity.BaseTimeEntity;
import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.domain.member.Member;
import com.ic.domain.review.InterviewReview;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    private static final int MAX_CONTENT_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_review_id", nullable = false)
    private InterviewReview interviewReview;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column
    private LocalDateTime deletedAt;

    // 대댓글 관계: 부모 댓글 (대댓글인 경우에만 값이 있음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 대댓글 관계: 자식 댓글들 (이 댓글에 달린 대댓글들)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Comment> replies = new ArrayList<>();

    // 댓글 가시성 설정
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentVisibility visibility = CommentVisibility.PUBLIC;

    @Builder
    private Comment(Member author, InterviewReview interviewReview, String content, Comment parent, CommentVisibility visibility) {
        this.author = Objects.requireNonNull(author, "댓글 작성자는 필수입니다");
        this.interviewReview = Objects.requireNonNull(interviewReview, "면접 후기는 필수입니다");
        this.content = Objects.requireNonNull(content, "댓글 내용은 필수입니다");
        this.parent = parent;
        this.visibility = Objects.nonNull(visibility) ? visibility : CommentVisibility.PUBLIC;

        validateContent(content);
    }

    public static Comment create(Member author, InterviewReview interviewReview, String content) {
        return create(author, interviewReview, content, CommentVisibility.PUBLIC);
    }

    public static Comment create(Member author, InterviewReview interviewReview, String content, CommentVisibility visibility) {
        validateRequired(author, interviewReview, content);

        return Comment.builder()
                .author(author)
                .interviewReview(interviewReview)
                .content(content.trim())
                .visibility(visibility)
                .build();
    }

    public static Comment createReply(Member author, InterviewReview interviewReview, String content, Comment parent) {
        return createReply(author, interviewReview, content, parent, CommentVisibility.PUBLIC);
    }

    public static Comment createReply(Member author, InterviewReview interviewReview, String content, Comment parent, CommentVisibility visibility) {
        validateRequired(author, interviewReview, content);
        validateParentComment(parent);

        return Comment.builder()
                .author(author)
                .interviewReview(interviewReview)
                .content(content.trim())
                .parent(parent)
                .visibility(visibility)
                .build();
    }

    public void changeContent(final Long memberId, final String newContent) {
        validateOwnership(memberId, "본인의 댓글만 수정할 수 있습니다");
        validateContent(newContent);

        this.content = newContent.trim();
    }

    public void delete(final Long memberId) {
        validateOwnership(memberId, "본인의 댓글만 삭제할 수 있습니다");
        validateNotDeleted();

        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isWrittenBy(final Long memberId) {
        return Objects.nonNull(memberId) && Objects.equals(this.author.getId(), memberId);
    }

    public boolean isActive() {
        return !deleted;
    }

    public boolean isTopLevelComment() {
        return Objects.isNull(parent);
    }

    public boolean isReply() {
        return Objects.nonNull(parent);
    }

    public Comment getRootComment() {
        return isTopLevelComment() ? this : parent;
    }

    public List<Comment> getActiveReplies() {
        return replies.stream()
                .filter(Comment::isActive)
                .toList();
    }

    public String getBlurredContent() {
        if (Objects.isNull(content)) {
            return "";
        }

        final StringBuilder blurred = new StringBuilder();
        for (char c : content.toCharArray()) {
            if (Character.isWhitespace(c)) {
                blurred.append(c); // 공백 문자는 그대로 유지
            } else if (Character.isDigit(c)) {
                blurred.append('0'); // 숫자는 0으로 대체
            } else if (c >= 'A' && c <= 'Z') {
                blurred.append('A'); // 대문자는 A로 대체
            } else if (c >= 'a' && c <= 'z') {
                blurred.append('a'); // 소문자는 a로 대체
            } else if (c >= '가' && c <= '힣') {
                blurred.append('가'); // 한글은 '가'로 대체
            } else {
                blurred.append('*'); // 기타 문자는 *로 대체
            }
        }
        return blurred.toString();
    }

    public void changeVisibility(final Long memberId, final CommentVisibility newVisibility) {
        validateOwnership(memberId, "본인의 댓글만 가시성을 변경할 수 있습니다");
        this.visibility = Objects.nonNull(newVisibility) ? newVisibility : CommentVisibility.PUBLIC;
    }

    public boolean isVisibleTo(final Long viewerId, final Long reviewAuthorId) {
        // 삭제된 댓글은 볼 수 없음
        if (!isActive()) {
            return false;
        }

        // 전체 공개인 경우 누구나 볼 수 있음
        if (visibility.isPublic()) {
            return true;
        }

        // 작성자 전용인 경우 댓글 작성자와 글 작성자만 볼 수 있음
        if (visibility.isAuthorOnly()) {
            return isWrittenBy(viewerId) || Objects.equals(viewerId, reviewAuthorId);
        }

        return false;
    }

    public List<Comment> getVisibleReplies(final Long viewerId, final Long reviewAuthorId) {
        return replies.stream()
                .filter(reply -> reply.isVisibleTo(viewerId, reviewAuthorId))
                .toList();
    }

    private static void validateRequired(final Member author, final InterviewReview interviewReview, final String content) {
        if (Objects.isNull(author)) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "댓글 작성자는 필수입니다");
        }
        if (Objects.isNull(interviewReview)) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "면접 후기는 필수입니다");
        }
        if (Objects.isNull(content) || content.trim().isEmpty()) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "댓글 내용은 필수입니다");
        }
    }

    private void validateContent(final String content) {
        if (Objects.isNull(content) || content.trim().isEmpty()) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "댓글 내용은 필수입니다");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "댓글은 1000자 이하로 입력해주세요");
        }
    }

    private void validateOwnership(final Long memberId, final String message) {
        if (!isWrittenBy(memberId)) {
            throw BusinessException.of(ErrorCode.COMMENT_PERMISSION_DENIED, message);
        }
    }

    private void validateNotDeleted() {
        if (deleted) {
            throw BusinessException.of(ErrorCode.COMMENT_ALREADY_DELETED, "이미 삭제된 댓글입니다");
        }
    }

    private static void validateParentComment(final Comment parent) {
        if (Objects.isNull(parent)) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "부모 댓글은 필수입니다");
        }
        if (parent.isReply()) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "대댓글에는 대댓글을 달 수 없습니다");
        }
        if (!parent.isActive()) {
            throw BusinessException.of(ErrorCode.COMMENT_ALREADY_DELETED, "삭제된 댓글에는 답글을 달 수 없습니다");
        }
    }
}