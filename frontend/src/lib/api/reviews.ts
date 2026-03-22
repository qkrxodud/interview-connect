import { api } from '@/lib/axios';
import {
  InterviewReview,
  CreateReviewRequest,
  ApiResponse,
  PageResponse
} from '@/types';

export const reviewsApi = {
  // 후기 목록 조회
  getReviews: async (params?: {
    page?: number;
    size?: number;
    company?: string;
    position?: string;
    difficulty?: number;
  }): Promise<PageResponse<InterviewReview>> => {
    const response = await api.get<ApiResponse<PageResponse<InterviewReview>>>(
      '/reviews',
      { params }
    );
    return response.data.data;
  },

  // 후기 상세 조회
  getReview: async (id: number): Promise<InterviewReview> => {
    const response = await api.get<ApiResponse<InterviewReview>>(`/reviews/${id}`);
    return response.data.data;
  },

  // 후기 생성
  createReview: async (data: CreateReviewRequest): Promise<InterviewReview> => {
    const response = await api.post<ApiResponse<InterviewReview>>('/reviews', data);
    return response.data.data;
  },

  // 후기 수정
  updateReview: async (id: number, data: Partial<CreateReviewRequest>): Promise<InterviewReview> => {
    const response = await api.patch<ApiResponse<InterviewReview>>(`/reviews/${id}`, data);
    return response.data.data;
  },

  // 후기 삭제
  deleteReview: async (id: number): Promise<void> => {
    await api.delete(`/reviews/${id}`);
  },

  // 사용자의 후기 목록 조회
  getMyReviews: async (params?: {
    page?: number;
    size?: number;
  }): Promise<PageResponse<InterviewReview>> => {
    const response = await api.get<ApiResponse<PageResponse<InterviewReview>>>(
      '/reviews/me',
      { params }
    );
    return response.data.data;
  },
};