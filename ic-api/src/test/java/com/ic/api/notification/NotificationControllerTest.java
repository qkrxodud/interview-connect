package com.ic.api.notification;

import com.ic.domain.member.Member;
import com.ic.domain.notification.Notification;
import com.ic.domain.notification.NotificationService;
import com.ic.domain.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = NotificationController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    @WithMockUser
    @DisplayName("알림 목록 조회 API가 정상 작동한다")
    void shouldGetNotificationsSuccessfully() throws Exception {
        // given
        final Member member = createTestMember();
        final List<Notification> notifications = List.of(
                createTestNotification(member, NotificationType.QA_NEW_QUESTION),
                createTestNotification(member, NotificationType.QA_NEW_ANSWER)
        );
        final Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, 20), 2);

        given(notificationService.getNotifications(any(Member.class), any(Pageable.class)))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/notifications")
                        .with(authentication(createAuthentication(member)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @WithMockUser
    @DisplayName("읽지 않은 알림 목록 조회 API가 정상 작동한다")
    void shouldGetUnreadNotificationsSuccessfully() throws Exception {
        // given
        final Member member = createTestMember();
        final List<Notification> unreadNotifications = List.of(
                createTestNotification(member, NotificationType.QA_NEW_QUESTION)
        );
        final Page<Notification> page = new PageImpl<>(unreadNotifications, PageRequest.of(0, 20), 1);

        given(notificationService.getUnreadNotifications(any(Member.class), any(Pageable.class)))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/notifications/unread")
                        .with(authentication(createAuthentication(member)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("알림 요약 정보 조회 API가 정상 작동한다")
    void shouldGetNotificationSummarySuccessfully() throws Exception {
        // given
        final Member member = createTestMember();
        final long unreadCount = 3L;
        final Page<Notification> totalPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 10L);

        given(notificationService.getUnreadCount(any(Member.class))).willReturn(unreadCount);
        given(notificationService.getNotifications(any(Member.class), eq(Pageable.unpaged())))
                .willReturn(totalPage);

        // when & then
        mockMvc.perform(get("/api/v1/notifications/summary")
                        .with(authentication(createAuthentication(member)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(3))
                .andExpect(jsonPath("$.data.totalCount").value(10));
    }

    @Test
    @WithMockUser
    @DisplayName("특정 알림 읽음 처리 API가 정상 작동한다")
    void shouldMarkNotificationAsReadSuccessfully() throws Exception {
        // given
        final Member member = createTestMember();
        final Long notificationId = 1L;

        // when & then
        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", notificationId)
                        .with(authentication(createAuthentication(member)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAsRead(notificationId, member);
    }

    @Test
    @WithMockUser
    @DisplayName("모든 알림 읽음 처리 API가 정상 작동한다")
    void shouldMarkAllNotificationsAsReadSuccessfully() throws Exception {
        // given
        final Member member = createTestMember();

        // when & then
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .with(authentication(createAuthentication(member)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAllAsRead(member);
    }

    private Member createTestMember() {
        return Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword123!")
                .nickname("테스터")
                .build();
    }

    private Notification createTestNotification(Member recipient, NotificationType type) {
        return Notification.builder()
                .recipient(recipient)
                .type(type)
                .title("테스트 알림")
                .content("테스트 내용")
                .referenceId(1L)
                .referenceType("TEST")
                .build();
    }

    private org.springframework.security.core.Authentication createAuthentication(Member member) {
        return new org.springframework.security.authentication.TestingAuthenticationToken(member, null);
    }
}