package com.ic.domain.notification;

import com.ic.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NotificationTest {

    @Test
    @DisplayName("알림 생성 시 필수 값들이 올바르게 설정된다")
    void shouldCreateNotificationWithRequiredValues() {
        // given
        final Member recipient = createTestMember();
        final NotificationType type = NotificationType.QA_NEW_QUESTION;
        final String title = "새로운 질문이 등록되었습니다";
        final String content = "'카카오 백엔드 면접' 후기에 새로운 질문이 등록되었습니다.";
        final Long referenceId = 1L;
        final String referenceType = "QUESTION";

        // when
        final Notification notification = Notification.create(recipient, type, title, content,
                                                             referenceId, referenceType);

        // then
        assertThat(notification.getRecipient()).isEqualTo(recipient);
        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getTitle()).isEqualTo(title);
        assertThat(notification.getContent()).isEqualTo(content);
        assertThat(notification.getReferenceId()).isEqualTo(referenceId);
        assertThat(notification.getReferenceType()).isEqualTo(referenceType);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Q&A 질문 알림이 올바르게 생성된다")
    void shouldCreateQuestionNotification() {
        // given
        final Member recipient = createTestMember();
        final Long questionId = 1L;
        final String reviewTitle = "카카오 백엔드 개발자 면접 후기";

        // when
        final Notification notification = Notification.forNewQuestion(recipient, questionId, reviewTitle);

        // then
        assertThat(notification.getType()).isEqualTo(NotificationType.QA_NEW_QUESTION);
        assertThat(notification.getTitle()).isEqualTo("새로운 질문이 등록되었습니다");
        assertThat(notification.getContent()).contains(reviewTitle);
        assertThat(notification.getReferenceId()).isEqualTo(questionId);
        assertThat(notification.getReferenceType()).isEqualTo("QUESTION");
    }

    @Test
    @DisplayName("Q&A 답변 알림이 올바르게 생성된다")
    void shouldCreateAnswerNotification() {
        // given
        final Member recipient = createTestMember();
        final Long answerId = 1L;
        final String questionContent = "면접에서 어떤 질문을 받으셨나요?";

        // when
        final Notification notification = Notification.forNewAnswer(recipient, answerId, questionContent);

        // then
        assertThat(notification.getType()).isEqualTo(NotificationType.QA_NEW_ANSWER);
        assertThat(notification.getTitle()).isEqualTo("질문에 답변이 등록되었습니다");
        assertThat(notification.getContent()).contains("면접에서 어떤 질문을 받으셨나요?");
        assertThat(notification.getReferenceId()).isEqualTo(answerId);
        assertThat(notification.getReferenceType()).isEqualTo("ANSWER");
    }

    @Test
    @DisplayName("시스템 알림이 올바르게 생성된다")
    void shouldCreateSystemNotification() {
        // given
        final Member recipient = createTestMember();
        final String title = "서비스 점검 안내";
        final String content = "오늘 밤 12시부터 2시간 동안 서비스 점검이 예정되어 있습니다.";

        // when
        final Notification notification = Notification.forSystemAnnouncement(recipient, title, content);

        // then
        assertThat(notification.getType()).isEqualTo(NotificationType.SYSTEM_ANNOUNCEMENT);
        assertThat(notification.getTitle()).isEqualTo(title);
        assertThat(notification.getContent()).isEqualTo(content);
        assertThat(notification.getReferenceType()).isEqualTo("SYSTEM");
    }

    @Test
    @DisplayName("알림을 읽음 처리할 수 있다")
    void shouldMarkNotificationAsRead() {
        // given
        final Member recipient = createTestMember();
        final Notification notification = Notification.create(recipient, NotificationType.QA_NEW_QUESTION,
                                                             "제목", "내용", 1L, "QUESTION");

        // when
        notification.markAsRead();

        // then
        assertThat(notification.isRead()).isTrue();
        assertThat(notification.isUnread()).isFalse();
    }

    @Test
    @DisplayName("읽은 알림을 다시 읽지 않음으로 변경할 수 있다")
    void shouldMarkNotificationAsUnread() {
        // given
        final Member recipient = createTestMember();
        final Notification notification = Notification.create(recipient, NotificationType.QA_NEW_QUESTION,
                                                             "제목", "내용", 1L, "QUESTION");
        notification.markAsRead();

        // when
        notification.markAsUnread();

        // then
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.isUnread()).isTrue();
    }

    @Test
    @DisplayName("알림이 특정 사용자에게 속하는지 확인할 수 있다")
    void shouldCheckIfNotificationBelongsToMember() {
        // given
        final Member recipient = createTestMember();
        final Member otherMember = createOtherTestMember();
        final Notification notification = Notification.create(recipient, NotificationType.QA_NEW_QUESTION,
                                                             "제목", "내용", 1L, "QUESTION");

        // when & then
        assertThat(notification.belongsTo(recipient)).isTrue();
        assertThat(notification.belongsTo(otherMember)).isFalse();
    }

    @Test
    @DisplayName("긴 질문 내용은 적절히 잘린다")
    void shouldTruncateLongQuestionContent() {
        // given
        final Member recipient = createTestMember();
        final Long answerId = 1L;
        final String longQuestionContent = "이것은 매우 긴 질문 내용으로 20자를 넘어가는 텍스트입니다. 더 많은 내용이 있습니다.";

        // when
        final Notification notification = Notification.forNewAnswer(recipient, answerId, longQuestionContent);

        // then
        assertThat(notification.getContent()).contains("이것은 매우 긴 질문 내용으로 20");
        assertThat(notification.getContent()).doesNotContain("텍스트입니다. 더 많은 내용이 있습니다.");
    }

    @Test
    @DisplayName("수신자가 null이면 예외가 발생한다")
    void shouldThrowExceptionWhenRecipientIsNull() {
        // when & then
        assertThatThrownBy(() -> Notification.create(null, NotificationType.QA_NEW_QUESTION,
                                                    "제목", "내용", 1L, "QUESTION"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("수신자는 필수입니다");
    }

    @Test
    @DisplayName("알림 타입이 null이면 예외가 발생한다")
    void shouldThrowExceptionWhenTypeIsNull() {
        // given
        final Member recipient = createTestMember();

        // when & then
        assertThatThrownBy(() -> Notification.create(recipient, null, "제목", "내용", 1L, "QUESTION"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("알림 타입은 필수입니다");
    }

    private Member createTestMember() {
        return Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword123!")
                .nickname("테스터")
                .build();
    }

    private Member createOtherTestMember() {
        return Member.builder()
                .id(2L)
                .email("other@example.com")
                .password("hashedPassword123!")
                .nickname("다른사람")
                .build();
    }
}