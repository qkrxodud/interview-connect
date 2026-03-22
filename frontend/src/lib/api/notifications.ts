import { api } from '@/lib/axios';
import {
  Notification,
  NotificationSummary,
  ApiResponse,
  PageResponse
} from '@/types';

export const notificationsApi = {
  // 알림 목록 조회
  getNotifications: async (params?: {
    page?: number;
    size?: number;
  }): Promise<PageResponse<Notification>> => {
    const response = await api.get<ApiResponse<PageResponse<Notification>>>(
      '/notifications',
      { params }
    );
    return response.data.data;
  },

  // 읽지 않은 알림 목록 조회
  getUnreadNotifications: async (params?: {
    page?: number;
    size?: number;
  }): Promise<PageResponse<Notification>> => {
    const response = await api.get<ApiResponse<PageResponse<Notification>>>(
      '/notifications/unread',
      { params }
    );
    return response.data.data;
  },

  // 알림 요약 정보 조회
  getNotificationSummary: async (): Promise<NotificationSummary> => {
    const response = await api.get<ApiResponse<NotificationSummary>>(
      '/notifications/summary'
    );
    return response.data.data;
  },

  // 특정 알림 읽음 처리
  markAsRead: async (notificationId: number): Promise<void> => {
    await api.patch(`/notifications/${notificationId}/read`);
  },

  // 모든 알림 읽음 처리
  markAllAsRead: async (): Promise<void> => {
    await api.patch('/notifications/read-all');
  },
};