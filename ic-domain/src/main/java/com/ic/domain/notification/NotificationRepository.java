package com.ic.domain.notification;

import com.ic.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 사용자의 알림 목록을 최신순으로 조회
     */
    Page<Notification> findByRecipientOrderByCreatedAtDesc(Member recipient, Pageable pageable);

    /**
     * 특정 사용자의 읽지 않은 알림 목록을 최신순으로 조회
     */
    Page<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(Member recipient, Pageable pageable);

    /**
     * 특정 사용자의 읽지 않은 알림 개수 조회
     */
    long countByRecipientAndIsReadFalse(Member recipient);

    /**
     * 특정 사용자의 모든 알림을 읽음 처리
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsReadByRecipient(@Param("recipient") Member recipient);

    /**
     * 특정 사용자의 특정 타입 알림 목록 조회
     */
    List<Notification> findByRecipientAndTypeOrderByCreatedAtDesc(Member recipient, NotificationType type);

    /**
     * 특정 참조 ID와 타입으로 알림 존재 여부 확인
     */
    boolean existsByRecipientAndReferenceIdAndReferenceType(Member recipient, Long referenceId, String referenceType);

    /**
     * 특정 참조 ID와 타입의 알림 삭제
     */
    void deleteByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}