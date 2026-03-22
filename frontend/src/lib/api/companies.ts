import { api } from '@/lib/axios';
import { Company, ApiResponse, PageResponse } from '@/types';

export const companiesApi = {
  // 회사 목록 조회
  getCompanies: async (params?: {
    page?: number;
    size?: number;
    search?: string;
    industry?: string;
  }): Promise<PageResponse<Company>> => {
    const response = await api.get<ApiResponse<PageResponse<Company>>>(
      '/companies',
      { params }
    );
    return response.data.data;
  },

  // 회사 상세 조회
  getCompany: async (id: number): Promise<Company> => {
    const response = await api.get<ApiResponse<Company>>(`/companies/${id}`);
    return response.data.data;
  },

  // 회사 검색 (자동완성용)
  searchCompanies: async (query: string): Promise<Company[]> => {
    const response = await api.get<ApiResponse<Company[]>>('/companies/search', {
      params: { q: query }
    });
    return response.data.data;
  },
};