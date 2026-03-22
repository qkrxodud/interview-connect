import { api } from '@/lib/axios';
import {
  ReviewQuestion,
  ReviewAnswer,
  ApiResponse,
  PageResponse
} from '@/types';

export const qaApi = {
  // 특정 후기의 질문 목록 조회
  getQuestionsByReview: async (
    reviewId: number,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<ReviewQuestion>> => {
    const response = await api.get<ApiResponse<PageResponse<ReviewQuestion>>>(
      `/reviews/${reviewId}/qa/questions`,
      { params }
    );
    return response.data.data;
  },

  // 질문 생성
  createQuestion: async (reviewId: number, content: string): Promise<ReviewQuestion> => {
    const response = await api.post<ApiResponse<ReviewQuestion>>(
      `/reviews/${reviewId}/qa/questions`,
      { content }
    );
    return response.data.data;
  },

  // 특정 질문의 답변 목록 조회
  getAnswersByQuestion: async (
    questionId: number,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<ReviewAnswer>> => {
    const response = await api.get<ApiResponse<PageResponse<ReviewAnswer>>>(
      `/qa/questions/${questionId}/answers`,
      { params }
    );
    return response.data.data;
  },

  // 답변 생성
  createAnswer: async (questionId: number, content: string): Promise<ReviewAnswer> => {
    const response = await api.post<ApiResponse<ReviewAnswer>>(
      `/qa/questions/${questionId}/answers`,
      { content }
    );
    return response.data.data;
  },

  // 모든 질문 목록 조회 (최신순)
  getAllQuestions: async (params?: {
    page?: number;
    size?: number;
  }): Promise<PageResponse<ReviewQuestion>> => {
    const response = await api.get<ApiResponse<PageResponse<ReviewQuestion>>>(
      '/qa/questions',
      { params }
    );
    return response.data.data;
  },
};