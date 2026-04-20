package com.ic.api.notification;

import com.ic.api.integration.config.IntegrationTestFakesConfig;
import com.ic.api.integration.config.TestApplicationConfig;
import com.ic.api.integration.config.TestSecurityConfig;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;
import com.ic.domain.notification.Notification;
import com.ic.domain.notification.NotificationService;
import com.ic.domain.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({IntegrationTestFakesConfig.class, TestApplicationConfig.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IntegrationTestFakesConfig fakesConfig;

    @MockBean
    private NotificationService notificationService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        fakesConfig.resetAllFakes();
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword123!")
                .nickname("테스터")
                .role(MemberRole.VERIFIED)
                .emailVerified(true)
                .build();
        fakesConfig.getMemberRepository().save(testMember);
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("알림 목록 조회 API가 정상 작동한다")
    void shouldGetNotificationsSuccessfully() throws Exception {
        // given
        final List<Notification> notifications = List.of(
                createTestNotification(testMember, NotificationType.QA_NEW_QUESTION),
                createTestNotification(testMember, NotificationType.QA_NEW_ANSWER)
        );
        final Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, 20), 2);

        given(notificationService.getNotifications(any(Member.class), any(Pageable.class)))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("읽지 않은 알림 목록 조회 API가 정상 작동한다")
    void shouldGetUnreadNotificationsSuccessfully() throws Exception {
        // given
        final List<Notification> unreadNotifications = List.of(
                createTestNotification(testMember, NotificationType.QA_NEW_QUESTION)
        );
        final Page<Notification> page = new PageImpl<>(unreadNotifications, PageRequest.of(0, 20), 1);

        given(notificationService.getUnreadNotifications(any(Member.class), any(Pageable.class)))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/notifications/unread")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("알림 요약 정보 조회 API가 정상 작동한다")
    void shouldGetNotificationSummarySuccessfully() throws Exception {
        // given
        final long unreadCount = 3L;
        final Page<Notification> totalPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 10L);

        given(notificationService.getUnreadCount(any(Member.class))).willReturn(unreadCount);
        given(notificationService.getNotifications(any(Member.class), eq(Pageable.unpaged())))
                .willReturn(totalPage);

        // when & then
        mockMvc.perform(get("/api/v1/notifications/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(3))
                .andExpect(jsonPath("$.data.totalCount").value(10));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("특정 알림 읽음 처리 API가 정상 작동한다")
    void shouldMarkNotificationAsReadSuccessfully() throws Exception {
        // given
        final Long notificationId = 1L;

        // when & then
        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAsRead(eq(notificationId), any(Member.class));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("모든 알림 읽음 처리 API가 정상 작동한다")
    void shouldMarkAllNotificationsAsReadSuccessfully() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAllAsRead(any(Member.class));
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
}
