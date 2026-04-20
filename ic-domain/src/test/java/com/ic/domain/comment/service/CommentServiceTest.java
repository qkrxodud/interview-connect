package com.ic.domain.comment.service;

import com.ic.domain.fake.FakeCommentRepository;
import com.ic.domain.fake.FakeInterviewReviewRepository;
import com.ic.domain.fake.FakeMemberRepository;
import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.domain.comment.Comment;
import com.ic.domain.company.Company;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("댓글 서비스")
class CommentServiceTest {

    private FakeCommentRepository commentRepository;
    private FakeMemberRepository memberRepository;
    private FakeInterviewReviewRepository reviewRepository;
    private CommentService commentService;

    private Member testMember;
    private Member otherMember;
    private InterviewReview testReview;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        // given - Fake Repository 초기화
        commentRepository = new FakeCommentRepository();
        memberRepository = new FakeMemberRepository();
        reviewRepository = new FakeInterviewReviewRepository();

        commentService = new CommentService(commentRepository, memberRepository, reviewRepository);

        // given - 테스트 데이터 준비
        setupTestData();
    }

    private void setupTestData() {
        // 테스트용 회원 생성
        testMember = Member.builder()
                .email("test@example.com")
                .password("password123!")
                .nickname("테스터")
                .role(MemberRole.VERIFIED)
                .emailVerified(true)
                .build();
        testMember = memberRepository.save(testMember);

        otherMember = Member.builder()
                .email("other@example.com")
                .password("password123!")
                .nickname("다른사용자")
                .role(MemberRole.VERIFIED)
                .emailVerified(true)
                .build();
        otherMember = memberRepository.save(otherMember);

        // 테스트용 회사 생성
        testCompany = Company.builder()
                .name("테스트회사")
                .industry("IT")
                .build();

        // 테스트용 후기 생성
        testReview = InterviewReview.builder()
                .member(testMember)
                .company(testCompany)
                .position("백엔드 개발자")
                .difficulty(3)
                .atmosphere(4)
                .result(InterviewResult.PASS)
                .content("면접 후기 내용")
                .build();
        testReview = reviewRepository.save(testReview);
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {

        @Test
        @DisplayName("정상적인 댓글 생성")
        void shouldCreateCommentSuccessfully() {
            // given
            final Long memberId = testMember.getId();
            final Long reviewId = testReview.getId();
            final String content = "좋은 후기네요!";

            // when
            final Comment createdComment = commentService.createComment(memberId, reviewId, content);

            // then
            assertThat(createdComment).isNotNull();
            assertThat(createdComment.getContent()).isEqualTo(content.trim());
            assertThat(createdComment.getAuthor().getId()).isEqualTo(memberId);
            assertThat(createdComment.getInterviewReview().getId()).isEqualTo(reviewId);
            assertThat(createdComment.isActive()).isTrue();
            assertThat(commentRepository.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID로 댓글 생성 시 예외 발생")
        void shouldThrowExceptionWhenMemberNotFound() {
            // given
            final Long nonExistentMemberId = 999L;
            final Long reviewId = testReview.getId();
            final String content = "댓글 내용";

            // when & then
            assertThatThrownBy(() -> commentService.createComment(nonExistentMemberId, reviewId, content))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 후기 ID로 댓글 생성 시 예외 발생")
        void shouldThrowExceptionWhenReviewNotFound() {
            // given
            final Long memberId = testMember.getId();
            final Long nonExistentReviewId = 999L;
            final String content = "댓글 내용";

            // when & then
            assertThatThrownBy(() -> commentService.createComment(memberId, nonExistentReviewId, content))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
        }

        @Test
        @DisplayName("null 회원 ID로 댓글 생성 시 예외 발생")
        void shouldThrowExceptionWhenMemberIdIsNull() {
            // given
            final Long memberId = null;
            final Long reviewId = testReview.getId();
            final String content = "댓글 내용";

            // when & then
            assertThatThrownBy(() -> commentService.createComment(memberId, reviewId, content))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("null 후기 ID로 댓글 생성 시 예외 발생")
        void shouldThrowExceptionWhenReviewIdIsNull() {
            // given
            final Long memberId = testMember.getId();
            final Long reviewId = null;
            final String content = "댓글 내용";

            // when & then
            assertThatThrownBy(() -> commentService.createComment(memberId, reviewId, content))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        private Comment savedComment;

        @BeforeEach
        void setupComment() {
            savedComment = Comment.create(testMember, testReview, "원래 댓글 내용");
            savedComment = commentRepository.save(savedComment);
        }

        @Test
        @DisplayName("작성자가 자신의 댓글 수정")
        void shouldUpdateCommentByOwner() {
            // given
            final Long commentId = savedComment.getId();
            final Long authorId = testMember.getId();
            final String newContent = "수정된 댓글 내용";

            // when
            final Comment updatedComment = commentService.updateComment(commentId, authorId, newContent);

            // then
            assertThat(updatedComment.getContent()).isEqualTo(newContent.trim());
            assertThat(updatedComment.getId()).isEqualTo(commentId);
        }

        @Test
        @DisplayName("다른 사용자가 댓글 수정 시도 시 예외 발생")
        void shouldThrowExceptionWhenUnauthorizedUserTriesToUpdate() {
            // given
            final Long commentId = savedComment.getId();
            final Long unauthorizedUserId = otherMember.getId();
            final String newContent = "수정된 댓글 내용";

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(commentId, unauthorizedUserId, newContent))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_PERMISSION_DENIED);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 ID로 수정 시도 시 예외 발생")
        void shouldThrowExceptionWhenCommentNotFound() {
            // given
            final Long nonExistentCommentId = 999L;
            final Long authorId = testMember.getId();
            final String newContent = "수정된 댓글 내용";

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(nonExistentCommentId, authorId, newContent))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("null 회원 ID로 수정 시도 시 예외 발생")
        void shouldThrowExceptionWhenMemberIdIsNullForUpdate() {
            // given
            final Long commentId = savedComment.getId();
            final Long memberId = null;
            final String newContent = "수정된 댓글 내용";

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(commentId, memberId, newContent))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        private Comment savedComment;

        @BeforeEach
        void setupComment() {
            savedComment = Comment.create(testMember, testReview, "삭제될 댓글 내용");
            savedComment = commentRepository.save(savedComment);
        }

        @Test
        @DisplayName("작성자가 자신의 댓글 삭제")
        void shouldDeleteCommentByOwner() {
            // given
            final Long commentId = savedComment.getId();
            final Long authorId = testMember.getId();

            // when
            commentService.deleteComment(commentId, authorId);

            // then
            final Comment deletedComment = commentRepository.findById(commentId).orElse(null);
            assertThat(deletedComment).isNotNull();
            assertThat(deletedComment.isActive()).isFalse();
            assertThat(deletedComment.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("다른 사용자가 댓글 삭제 시도 시 예외 발생")
        void shouldThrowExceptionWhenUnauthorizedUserTriesToDelete() {
            // given
            final Long commentId = savedComment.getId();
            final Long unauthorizedUserId = otherMember.getId();

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(commentId, unauthorizedUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_PERMISSION_DENIED);
        }

        @Test
        @DisplayName("이미 삭제된 댓글 재삭제 시도 시 예외 발생")
        void shouldThrowExceptionWhenTryingToDeleteAlreadyDeletedComment() {
            // given
            final Long commentId = savedComment.getId();
            final Long authorId = testMember.getId();

            // 먼저 댓글 삭제
            commentService.deleteComment(commentId, authorId);

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(commentId, authorId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_ALREADY_DELETED);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 ID로 삭제 시도 시 예외 발생")
        void shouldThrowExceptionWhenCommentNotFoundForDelete() {
            // given
            final Long nonExistentCommentId = 999L;
            final Long authorId = testMember.getId();

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(nonExistentCommentId, authorId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("후기별 댓글 조회")
    class GetCommentsByReviewId {

        @BeforeEach
        void setupComments() {
            // 활성 댓글 3개 생성 (시간 순서대로)
            final Comment comment1 = Comment.create(testMember, testReview, "첫 번째 댓글");
            final Comment comment2 = Comment.create(otherMember, testReview, "두 번째 댓글");
            final Comment comment3 = Comment.create(testMember, testReview, "세 번째 댓글");

            commentRepository.save(comment1);
            commentRepository.save(comment2);
            commentRepository.save(comment3);

            // 삭제된 댓글 1개 생성
            final Comment deletedComment = Comment.create(testMember, testReview, "삭제된 댓글");
            commentRepository.save(deletedComment);
            deletedComment.delete(testMember.getId());
            commentRepository.save(deletedComment);
        }

        @Test
        @DisplayName("삭제되지 않은 댓글만 조회되고 생성일시 오름차순으로 정렬")
        void shouldReturnOnlyActiveCommentsOrderedByCreatedAt() {
            // given
            final Long reviewId = testReview.getId();

            // when
            final List<Comment> comments = commentService.getCommentsByReviewId(reviewId);

            // then
            assertThat(comments).hasSize(3);
            assertThat(comments).allMatch(Comment::isActive);

            // 생성일시 오름차순 정렬 확인
            for (int i = 0; i < comments.size() - 1; i++) {
                assertThat(comments.get(i).getCreatedAt())
                        .isBeforeOrEqualTo(comments.get(i + 1).getCreatedAt());
            }
        }

        @Test
        @DisplayName("페이징 처리된 댓글 조회")
        void shouldReturnPagedComments() {
            // given
            final Long reviewId = testReview.getId();
            final Pageable pageable = PageRequest.of(0, 2);

            // when
            final Page<Comment> comments = commentService.getCommentsByReviewId(reviewId, pageable);

            // then
            assertThat(comments.getContent()).hasSize(2);
            assertThat(comments.getContent()).allMatch(Comment::isActive);
        }

        @Test
        @DisplayName("존재하지 않는 후기 ID로 댓글 조회 시 예외 발생")
        void shouldThrowExceptionWhenReviewNotFoundForCommentsRetrieval() {
            // given
            final Long nonExistentReviewId = 999L;

            // when & then
            assertThatThrownBy(() -> commentService.getCommentsByReviewId(nonExistentReviewId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("댓글 개수 조회")
    class GetCommentCountByReviewId {

        @BeforeEach
        void setupComments() {
            // 활성 댓글 2개 생성
            final Comment activeComment1 = Comment.create(testMember, testReview, "활성 댓글 1");
            final Comment activeComment2 = Comment.create(otherMember, testReview, "활성 댓글 2");

            commentRepository.save(activeComment1);
            commentRepository.save(activeComment2);

            // 삭제된 댓글 1개 생성
            final Comment deletedComment = Comment.create(testMember, testReview, "삭제된 댓글");
            commentRepository.save(deletedComment);
            deletedComment.delete(testMember.getId());
            commentRepository.save(deletedComment);
        }

        @Test
        @DisplayName("삭제되지 않은 댓글 개수만 계산")
        void shouldCountOnlyActiveComments() {
            // given
            final Long reviewId = testReview.getId();

            // when
            final long commentCount = commentService.getCommentCountByReviewId(reviewId);

            // then
            assertThat(commentCount).isEqualTo(2L);
        }

        @Test
        @DisplayName("존재하지 않는 후기 ID로 댓글 개수 조회 시 예외 발생")
        void shouldThrowExceptionWhenReviewNotFoundForCommentCount() {
            // given
            final Long nonExistentReviewId = 999L;

            // when & then
            assertThatThrownBy(() -> commentService.getCommentCountByReviewId(nonExistentReviewId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
        }
    }
}