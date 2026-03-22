import { api } from '@/lib/axios';
import { AuthResponse, LoginRequest, RegisterRequest, ApiResponse } from '@/types';

export const authApi = {
  // 로그인
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', data);
    return response.data.data;
  },

  // 회원가입
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/register', data);
    return response.data.data;
  },

  // 로그아웃
  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },

  // 토큰 갱신
  refresh: async (): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/refresh');
    return response.data.data;
  },

  // 현재 사용자 정보 조회
  me: async (): Promise<AuthResponse> => {
    const response = await api.get<ApiResponse<AuthResponse>>('/auth/me');
    return response.data.data;
  },
};