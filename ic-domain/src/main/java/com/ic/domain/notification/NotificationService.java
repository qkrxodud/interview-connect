package com.ic.domain.notification;

import com.ic.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성 및 저장
     */
    @Transactional
    public Notification createNotification(Member recipient, NotificationType type, String title,
                                         String content, Long referenceId, String referenceType) {
        Objects.requireNonNull(recipient, "수신자는 필수입니다");
        Objects.requireNonNull(type, "알림 타입은 필수입니다");

        // 중복 알림 방지 (같은 참조 대상에 대한 알림이 이미 존재하는 경우)
        if (Objects.nonNull(referenceId) && Objects.nonNull(referenceType)) {
            if (notificationRepository.existsByRecipientAndReferenceIdAndReferenceType(
                    recipient, referenceId, referenceType)) {
                log.debug("Duplicate notification skipped for recipient: {}, referenceId: {}, referenceType: {}",
                         recipient.getId(), referenceId, referenceType);
                return null;
            }
        }

        final Notification notification = Notification.create(recipient, type, title, content,
                                                             referenceId, referenceType);
        final Notification savedNotification = notificationRepository.save(notification);

        log.info("Notification created for recipient: {}, type: {}, title: {}",
                recipient.getId(), type, title);

        return savedNotification;
    }

    /**
     * Q&A 새 질문 알림 생성
     */
    @Transactional
    public Notification createQuestionNotification(Member recipient, Long questionId, String reviewTitle) {
        final Notification notification = Notification.forNewQuestion(recipient, questionId, reviewTitle);
        return notificationRepository.save(notification);
    }

    /**
     * Q&A 새 답변 알림 생성
     */
    @Transactional
    public Notification createAnswerNotification(Member recipient, Long answerId, String questionContent) {
        final Notification notification = Notification.forNewAnswer(recipient, answerId, questionContent);
        return notificationRepository.save(notification);
    }

    /**
     * 시스템 알림 생성
     */
    @Transactional
    public Notification createSystemNotification(Member recipient, String title, String content) {
        final Notification notification = Notification.forSystemAnnouncement(recipient, title, content);
        return notificationRepository.save(notification);
    }

    /**
     * 사용자의 알림 목록 조회 (읽음/읽지 않음 구분 없이)
     */
    public Page<Notification> getNotifications(Member member, Pageable pageable) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(member, pageable);
    }

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     */
    public Page<Notification> getUnreadNotifications(Member member, Pageable pageable) {
        return notificationRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(member, pageable);
    }

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    public long getUnreadCount(Member member) {
        return notificationRepository.countByRecipientAndIsReadFalse(member);
    }

    /**
     * 특정 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, Member member) {
        final Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다"));

        if (!notification.belongsTo(member)) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다");
        }

        notification.markAsRead();
        log.info("Notification marked as read: {}", notificationId);
    }

    /**
     * 사용자의 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Member member) {
        notificationRepository.markAllAsReadByRecipient(member);
        log.info("All notifications marked as read for member: {}", member.getId());
    }

    /**
     * 특정 타입의 알림 목록 조회
     */
    public List<Notification> getNotificationsByType(Member member, NotificationType type) {
        return notificationRepository.findByRecipientAndTypeOrderByCreatedAtDesc(member, type);
    }

    /**
     * 특정 참조 대상의 알림 삭제 (참조 대상이 삭제될 때)
     */
    @Transactional
    public void deleteNotificationsByReference(Long referenceId, String referenceType) {
        notificationRepository.deleteByReferenceIdAndReferenceType(referenceId, referenceType);
        log.info("Notifications deleted for referenceId: {}, referenceType: {}", referenceId, referenceType);
    }
}