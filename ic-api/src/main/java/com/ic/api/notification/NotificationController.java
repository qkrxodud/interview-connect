package com.ic.api.notification;

import com.ic.api.config.security.AuthMember;
import com.ic.api.config.security.CustomUserDetails;
import com.ic.api.notification.dto.NotificationResponse;
import com.ic.api.notification.dto.NotificationSummaryResponse;
import com.ic.common.response.ApiResponse;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberService;
import com.ic.domain.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final MemberService memberService;

    /**
     * 사용자의 알림 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthMember CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        final Member currentMember = memberService.findById(userDetails.getMemberId());
        final Page<NotificationResponse> notifications = notificationService.getNotifications(currentMember, pageable)
                .map(NotificationResponse::from);

        return ResponseEntity.ok(ApiResponse.ok(notifications));
    }

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnreadNotifications(
            @AuthMember CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        final Member currentMember = memberService.findById(userDetails.getMemberId());
        final Page<NotificationResponse> unreadNotifications = notificationService.getUnreadNotifications(currentMember, pageable)
                .map(NotificationResponse::from);

        return ResponseEntity.ok(ApiResponse.ok(unreadNotifications));
    }

    /**
     * 알림 요약 정보 조회 (읽지 않은 개수, 전체 개수)
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<NotificationSummaryResponse>> getNotificationSummary(
            @AuthMember CustomUserDetails userDetails) {

        final Member currentMember = memberService.findById(userDetails.getMemberId());
        final long unreadCount = notificationService.getUnreadCount(currentMember);
        final long totalCount = notificationService.getNotifications(currentMember, Pageable.unpaged())
                .getTotalElements();

        final NotificationSummaryResponse summary = NotificationSummaryResponse.of(unreadCount, totalCount);

        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    /**
     * 특정 알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @AuthMember CustomUserDetails userDetails) {

        final Member currentMember = memberService.findById(userDetails.getMemberId());
        notificationService.markAsRead(notificationId, currentMember);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthMember CustomUserDetails userDetails) {

        final Member currentMember = memberService.findById(userDetails.getMemberId());
        notificationService.markAllAsRead(currentMember);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}