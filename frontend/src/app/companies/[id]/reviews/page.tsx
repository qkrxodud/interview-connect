'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { Button } from '@/components/common/Button';

interface Company {
  id: number;
  name: string;
  description?: string;
  industry?: string;
}

interface Review {
  id: number;
  position: string;
  difficulty: number;
  result: 'PASS' | 'FAIL' | 'PENDING';
  createdAt: string;
  member: {
    nickname: string;
  };
}

export default function CompanyReviewsPage() {
  const params = useParams();
  const companyId = params.id as string;
  const [company, setCompany] = useState<Company | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCompanyData();
  }, [companyId]);

  const fetchCompanyData = async () => {
    try {
      const [companyResponse, reviewsResponse] = await Promise.all([
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/companies/${companyId}`),
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/companies/${companyId}/reviews`)
      ]);

      const companyData = await companyResponse.json();
      const reviewsData = await reviewsResponse.json();

      if (companyData.success) {
        setCompany(companyData.data);
      }

      if (reviewsData.success) {
        setReviews(reviewsData.data.content || []);
      }
    } catch (error) {
      console.error('Failed to fetch company data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getResultColor = (result: string) => {
    switch (result) {
      case 'PASS': return 'bg-green-100 text-green-800';
      case 'FAIL': return 'bg-red-100 text-red-800';
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getResultText = (result: string) => {
    switch (result) {
      case 'PASS': return '합격';
      case 'FAIL': return '불합격';
      case 'PENDING': return '대기중';
      default: return '미정';
    }
  };

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

  if (!company) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center py-12">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">회사를 찾을 수 없습니다</h2>
            <Link href="/companies">
              <Button>회사 목록으로 돌아가기</Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Breadcrumb */}
        <div className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/companies" className="hover:text-blue-600">회사</Link>
          <span>›</span>
          <span className="text-gray-900">{company.name}</span>
        </div>

        {/* Company Header */}
        <div className="bg-white rounded-lg shadow p-8 mb-8">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{company.name}</h1>
              {company.industry && (
                <span className="inline-block bg-blue-100 text-blue-800 text-sm px-3 py-1 rounded-full mb-3">
                  {company.industry}
                </span>
              )}
              {company.description && (
                <p className="text-gray-600 mb-4">{company.description}</p>
              )}
              <div className="text-sm text-gray-500">
                면접 후기 {reviews.length}개
              </div>
            </div>
            <Link href="/auth/login">
              <Button>후기 작성하기</Button>
            </Link>
          </div>
        </div>

        {/* Reviews */}
        {reviews.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-8 text-center">
            <div className="text-gray-500 mb-4">
              <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              아직 등록된 면접 후기가 없습니다
            </h3>
            <p className="text-gray-600 mb-6">
              {company.name}의 첫 번째 면접 후기를 작성해보세요!
            </p>
            <Link href="/auth/register">
              <Button>회원가입하고 후기 작성하기</Button>
            </Link>
          </div>
        ) : (
          <div className="space-y-6">
            {reviews.map((review) => (
              <div key={review.id} className="bg-white p-6 rounded-lg shadow hover:shadow-md transition-shadow">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-3 mb-2">
                      <h3 className="text-lg font-semibold text-gray-900">
                        {review.position}
                      </h3>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getResultColor(review.result)}`}>
                        {getResultText(review.result)}
                      </span>
                    </div>
                    <div className="flex items-center space-x-4 text-sm text-gray-500 mb-2">
                      <span>난이도: {review.difficulty}/5</span>
                      <span>•</span>
                      <span>작성자: {review.member.nickname}</span>
                      <span>•</span>
                      <span>{new Date(review.createdAt).toLocaleDateString()}</span>
                    </div>
                  </div>
                  <Link href={`/reviews/${review.id}`}>
                    <Button variant="outline" size="sm">자세히 보기</Button>
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Back to Companies */}
        <div className="mt-8 text-center">
          <Link href="/companies">
            <Button variant="outline">← 다른 회사 보기</Button>
          </Link>
        </div>
      </div>
    </div>
  );
}