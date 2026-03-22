package com.ic.domain.notification;

import com.ic.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private Notification(Member recipient, NotificationType type, String title,
                        String content, Long referenceId, String referenceType) {
        this.recipient = Objects.requireNonNull(recipient, "수신자는 필수입니다");
        this.type = Objects.requireNonNull(type, "알림 타입은 필수입니다");
        this.title = Objects.requireNonNull(title, "제목은 필수입니다");
        this.content = Objects.requireNonNull(content, "내용은 필수입니다");
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.createdAt = LocalDateTime.now();
    }

    public static Notification create(Member recipient, NotificationType type, String title,
                                    String content, Long referenceId, String referenceType) {
        return Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .content(content)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
    }

    // Q&A 질문 알림 생성
    public static Notification forNewQuestion(Member recipient, Long questionId, String reviewTitle) {
        final String title = "새로운 질문이 등록되었습니다";
        final String content = String.format("'%s' 후기에 새로운 질문이 등록되었습니다.", reviewTitle);

        return create(recipient, NotificationType.QA_NEW_QUESTION, title, content,
                     questionId, "QUESTION");
    }

    // Q&A 답변 알림 생성
    public static Notification forNewAnswer(Member recipient, Long answerId, String questionContent) {
        final String title = "질문에 답변이 등록되었습니다";
        final String content = String.format("'%s...' 질문에 답변이 등록되었습니다.",
                                           truncateContent(questionContent, 20));

        return create(recipient, NotificationType.QA_NEW_ANSWER, title, content,
                     answerId, "ANSWER");
    }

    // 시스템 알림 생성
    public static Notification forSystemAnnouncement(Member recipient, String title, String content) {
        return create(recipient, NotificationType.SYSTEM_ANNOUNCEMENT, title, content,
                     null, "SYSTEM");
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsUnread() {
        this.isRead = false;
    }

    public boolean isUnread() {
        return !isRead;
    }

    public boolean belongsTo(Member member) {
        return this.recipient.equals(member);
    }

    private static String truncateContent(String content, int maxLength) {
        if (Objects.isNull(content) || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength);
    }
}