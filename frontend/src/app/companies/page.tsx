'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button } from '@/components/common/Button';

interface Company {
  id: number;
  name: string;
  description?: string;
  industry?: string;
  reviewCount?: number;
}

export default function CompaniesPage() {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchCompanies();
  }, []);

  const fetchCompanies = async () => {
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/companies`);
      const data = await response.json();

      if (data.success) {
        setCompanies(data.data || []);
      }
    } catch (error) {
      console.error('Failed to fetch companies:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredCompanies = companies.filter(company =>
    company.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">회사 정보를 불러오는 중...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">회사별 면접 후기</h1>
          <p className="text-lg text-gray-600">
            관심 있는 회사의 면접 후기를 확인해보세요
          </p>
        </div>

        {/* Search */}
        <div className="mb-6">
          <div className="relative">
            <input
              type="text"
              placeholder="회사명으로 검색..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
            <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
              <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
          </div>
        </div>

        {/* Back to Reviews */}
        <div className="mb-6">
          <Link href="/reviews">
            <Button variant="outline">← 전체 후기로 돌아가기</Button>
          </Link>
        </div>

        {/* Companies List */}
        {filteredCompanies.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-lg shadow">
            <div className="text-gray-500 mb-4">
              <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              {searchTerm ? '검색 결과가 없습니다' : '아직 등록된 회사가 없습니다'}
            </h3>
            <p className="text-gray-600 mb-6">
              {searchTerm ? '다른 키워드로 검색해보세요' : '첫 번째 면접 후기를 작성해서 회사를 등록해보세요!'}
            </p>
            {!searchTerm && (
              <Link href="/auth/register">
                <Button>회원가입하고 후기 작성하기</Button>
              </Link>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredCompanies.map((company) => (
              <div key={company.id} className="bg-white p-6 rounded-lg shadow hover:shadow-md transition-shadow">
                <div className="mb-4">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">
                    {company.name}
                  </h3>
                  {company.industry && (
                    <span className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full mb-2">
                      {company.industry}
                    </span>
                  )}
                  {company.description && (
                    <p className="text-gray-600 text-sm mb-3 line-clamp-2">
                      {company.description}
                    </p>
                  )}
                </div>

                <div className="flex items-center justify-between">
                  <div className="text-sm text-gray-500">
                    {company.reviewCount ? `후기 ${company.reviewCount}개` : '후기 없음'}
                  </div>
                  <Link href={`/companies/${company.id}/reviews`}>
                    <Button variant="outline" size="sm">후기 보기</Button>
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}