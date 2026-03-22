package com.ic.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTypeTest {

    @Test
    @DisplayName("Q&A 관련 알림 타입을 올바르게 구분한다")
    void shouldIdentifyQaRelatedTypes() {
        // when & then
        assertThat(NotificationType.QA_NEW_QUESTION.isQaRelated()).isTrue();
        assertThat(NotificationType.QA_NEW_ANSWER.isQaRelated()).isTrue();
        assertThat(NotificationType.REVIEW_LIKED.isQaRelated()).isFalse();
        assertThat(NotificationType.SYSTEM_ANNOUNCEMENT.isQaRelated()).isFalse();
    }

    @Test
    @DisplayName("후기 관련 알림 타입을 올바르게 구분한다")
    void shouldIdentifyReviewRelatedTypes() {
        // when & then
        assertThat(NotificationType.REVIEW_LIKED.isReviewRelated()).isTrue();
        assertThat(NotificationType.REVIEW_COMMENTED.isReviewRelated()).isTrue();
        assertThat(NotificationType.QA_NEW_QUESTION.isReviewRelated()).isFalse();
        assertThat(NotificationType.MESSAGE_RECEIVED.isReviewRelated()).isFalse();
    }

    @Test
    @DisplayName("쪽지 관련 알림 타입을 올바르게 구분한다")
    void shouldIdentifyMessageRelatedTypes() {
        // when & then
        assertThat(NotificationType.MESSAGE_RECEIVED.isMessageRelated()).isTrue();
        assertThat(NotificationType.QA_NEW_QUESTION.isMessageRelated()).isFalse();
        assertThat(NotificationType.REVIEW_LIKED.isMessageRelated()).isFalse();
    }

    @Test
    @DisplayName("시스템 관련 알림 타입을 올바르게 구분한다")
    void shouldIdentifySystemRelatedTypes() {
        // when & then
        assertThat(NotificationType.SYSTEM_ANNOUNCEMENT.isSystemRelated()).isTrue();
        assertThat(NotificationType.QA_NEW_QUESTION.isSystemRelated()).isFalse();
        assertThat(NotificationType.MESSAGE_RECEIVED.isSystemRelated()).isFalse();
    }

    @Test
    @DisplayName("알림 타입의 설명을 확인할 수 있다")
    void shouldHaveCorrectDescriptions() {
        // when & then
        assertThat(NotificationType.QA_NEW_QUESTION.getDescription()).isEqualTo("새로운 질문이 등록되었습니다");
        assertThat(NotificationType.QA_NEW_ANSWER.getDescription()).isEqualTo("질문에 답변이 등록되었습니다");
        assertThat(NotificationType.REVIEW_LIKED.getDescription()).isEqualTo("후기에 좋아요가 등록되었습니다");
        assertThat(NotificationType.MESSAGE_RECEIVED.getDescription()).isEqualTo("새로운 쪽지를 받았습니다");
        assertThat(NotificationType.SYSTEM_ANNOUNCEMENT.getDescription()).isEqualTo("시스템 공지사항입니다");
    }
}