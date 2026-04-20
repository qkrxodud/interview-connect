package com.ic.domain.comment;

import com.ic.common.exception.BusinessException;
import com.ic.domain.comment.fixture.CommentFixture;
import com.ic.domain.fixture.InterviewReviewFixture;
import com.ic.domain.fixture.MemberFixture;
import com.ic.domain.member.Member;
import com.ic.domain.review.InterviewReview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("댓글 도메인 테스트")
class CommentDomainTest {

    private Member author;
    private Member otherUser;
    private InterviewReview review;

    @BeforeEach
    void setUp() {
        author = MemberFixture.createVerifiedMember();
        otherUser = MemberFixture.createGeneralMember();
        review = InterviewReviewFixture.createReview();
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {

        @Test
        @DisplayName("정상적인 댓글을 생성할 수 있다")
        void shouldCreateCommentSuccessfully() {
            // given
            final String content = "정말 유익한 후기였습니다. 감사해요!";

            // when
            Comment comment = Comment.create(author, review, content);

            // then
            assertThat(comment.getAuthor()).isEqualTo(author);
            assertThat(comment.getInterviewReview()).isEqualTo(review);
            assertThat(comment.getContent()).isEqualTo(content);
            assertThat(comment.isDeleted()).isFalse();
            assertThat(comment.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("빈 내용으로 댓글 생성 시 예외가 발생한다")
        void shouldThrowExceptionWhenContentIsEmpty() {
            // given
            final String emptyContent = "";

            // when & then
            assertThatThrownBy(() -> Comment.create(author, review, emptyContent))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("댓글 내용은 필수입니다");
        }

        @Test
        @DisplayName("공백만 있는 내용으로 댓글 생성 시 예외가 발생한다")
        void shouldThrowExceptionWhenContentIsOnlyWhitespace() {
            // given
            final String whitespaceContent = "   \t\n   ";

            // when & then
            assertThatThrownBy(() -> Comment.create(author, review, whitespaceContent))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("댓글 내용은 필수입니다");
        }

        @Test
        @DisplayName("null 내용으로 댓글 생성 시 예외가 발생한다")
        void shouldThrowExceptionWhenContentIsNull() {
            // given
            final String nullContent = null;

            // when & then
            assertThatThrownBy(() -> Comment.create(author, review, nullContent))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("댓글 내용은 필수입니다");
        }

        @Test
        @DisplayName("1000자 초과 내용으로 댓글 생성 시 예외가 발생한다")
        void shouldThrowExceptionWhenContentExceeds1000Characters() {
            // given
            final String longContent = CommentFixture.getLongContent(); // 1001자

            // when & then
            assertThatThrownBy(() -> Comment.create(author, review, longContent))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("댓글은 1000자 이하로 입력해주세요");
        }

        @Test
        @DisplayName("1000자 정확히 맞는 내용으로 댓글 생성이 가능하다")
        void shouldCreateCommentWith1000Characters() {
            // given
            final String content1000 = CommentFixture.getMaxLengthContent(); // 1000자

            // when
            Comment comment = Comment.create(author, review, content1000);

            // then
            assertThat(comment.getContent()).isEqualTo(content1000);
            assertThat(comment.getContent().length()).isEqualTo(1000);
        }

        @Test
        @DisplayName("null 작성자로 댓글 생성 시 예외가 발생한다")
        void shouldThrowExceptionWhenAuthorIsNull() {
            // given
            final Member nullAuthor = null;
            final String content = "좋은 후기입니다";

            // when & then
            assertThatThrownBy(() -> Comment.create(nullAuthor, review, content))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("댓글 작성자는 필수입니다");
        }

        @Test
        @DisplayName("null 면접 후기로 댓글 생성 시 예외가 발생한다")
        void shouldThrowExceptionWhenInterviewReviewIsNull() {
            // given
            final InterviewReview nullReview = null;
            final String content = "좋은 후기입니다";

            // when & then
            assertThatThrownBy(() -> Comment.create(author, nullReview, content))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("면접 후기는 필수입니다");
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        @Test
        @DisplayName("댓글 작성자가 자신의 댓글을 수정할 수 있다")
        void shouldUpdateCommentByAuthor() {
            // given
            Comment comment = CommentFixture.createComment(author, review, "원본 내용");
            final String newContent = "수정된 내용";

            // when
            comment.changeContent(author.getId(), newContent);

            // then
            assertThat(comment.getContent()).isEqualTo(newContent);
        }

        @Test
        @DisplayName("다른 사용자가 댓글 수정 시도 시 예외가 발생한다")
        void shouldThrowExceptionWhenOtherUserTriesToUpdateComment() {
            // given
            Comment comment = CommentFixture.createComment(author, review, "원본 내용");
            final String newContent = "수정된 내용";

            // when & then
            assertThatThrownBy(() -> comment.changeContent(otherUser.getId(), newContent))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("본인의 댓글만 수정할 수 있습니다");
        }

        @Test
        @DisplayName("빈 내용으로 댓글 수정 시 예외가 발생한다")
        void shouldThrowExceptionWhenUpdateWithEmptyContent() {
            // given
            Comment comment = CommentFixture.createComment(author, review, "원본 내용");
            final String emptyContent = "";

            // when & then
            assertThatThrownBy(() -> comment.changeContent(author.getId(), emptyContent))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("댓글 내용은 필수입니다");
        }

        @Test
        @DisplayName("1000자 초과로 댓글 수정 시 예외가 발생한다")
        void shouldThrowExceptionWhenUpdateWithTooLongContent() {
            // given
            Comment comment = CommentFixture.createComment(author, review, "원본 내용");
            final String longContent = CommentFixture.getLongContent(); // 1001자

            // when & then
            assertThatThrownBy(() -> comment.changeContent(author.getId(), longContent))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("댓글은 1000자 이하로 입력해주세요");
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("댓글 작성자가 자신의 댓글을 삭제할 수 있다")
        void shouldDeleteCommentByAuthor() {
            // given
            Comment comment = CommentFixture.createComment(author, review, "삭제될 댓글");
            final LocalDateTime beforeDelete = LocalDateTime.now();

            // when
            comment.delete(author.getId());

            // then
            assertThat(comment.isDeleted()).isTrue();
            assertThat(comment.getDeletedAt()).isAfterOrEqualTo(beforeDelete);
        }

        @Test
        @DisplayName("다른 사용자가 댓글 삭제 시도 시 예외가 발생한다")
        void shouldThrowExceptionWhenOtherUserTriesToDeleteComment() {
            // given
            Comment comment = CommentFixture.createComment(author, review, "삭제될 댓글");

            // when & then
            assertThatThrownBy(() -> comment.delete(otherUser.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("본인의 댓글만 삭제할 수 있습니다");
        }

        @Test
        @DisplayName("이미 삭제된 댓글 재삭제 시도 시 예외가 발생한다")
        void shouldThrowExceptionWhenDeleteAlreadyDeletedComment() {
            // given
            Comment comment = CommentFixture.createComment(author, review, "삭제될 댓글");
            comment.delete(author.getId()); // 첫 번째 삭제

            // when & then
            assertThatThrownBy(() -> comment.delete(author.getId())) // 두 번째 삭제 시도
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 삭제된 댓글입니다");
        }
    }

    @Nested
    @DisplayName("댓글 조회")
    class QueryComment {

        @Test
        @DisplayName("댓글 작성자 확인이 정상적으로 동작한다")
        void shouldCheckCommentAuthorCorrectly() {
            // given
            Comment comment = CommentFixture.createComment(author, review, "테스트 댓글");

            // when & then
            assertThat(comment.isWrittenBy(author.getId())).isTrue();
            assertThat(comment.isWrittenBy(otherUser.getId())).isFalse();
        }

        @Test
        @DisplayName("삭제되지 않은 댓글만 활성 상태로 판단한다")
        void shouldReturnTrueForActiveCommentOnly() {
            // given
            Comment activeComment = CommentFixture.createComment(author, review, "활성 댓글");
            Comment deletedComment = CommentFixture.createComment(author, review, "삭제될 댓글");
            deletedComment.delete(author.getId());

            // when & then
            assertThat(activeComment.isActive()).isTrue();
            assertThat(deletedComment.isActive()).isFalse();
        }
    }
}