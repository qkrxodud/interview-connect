package com.ic.domain.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    // Q&A 관련 알림
    QA_NEW_QUESTION("새로운 질문이 등록되었습니다"),
    QA_NEW_ANSWER("질문에 답변이 등록되었습니다"),

    // 후기 관련 알림 (Phase 2에서 확장)
    REVIEW_LIKED("후기에 좋아요가 등록되었습니다"),
    REVIEW_COMMENTED("후기에 댓글이 등록되었습니다"),

    // 쪽지 관련 알림 (Phase 2에서 구현)
    MESSAGE_RECEIVED("새로운 쪽지를 받았습니다"),

    // 시스템 알림
    SYSTEM_ANNOUNCEMENT("시스템 공지사항입니다");

    private final String description;

    public boolean isQaRelated() {
        return this == QA_NEW_QUESTION || this == QA_NEW_ANSWER;
    }

    public boolean isReviewRelated() {
        return this == REVIEW_LIKED || this == REVIEW_COMMENTED;
    }

    public boolean isMessageRelated() {
        return this == MESSAGE_RECEIVED;
    }

    public boolean isSystemRelated() {
        return this == SYSTEM_ANNOUNCEMENT;
    }
}