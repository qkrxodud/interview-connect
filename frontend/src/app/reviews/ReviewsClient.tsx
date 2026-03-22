'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button } from '@/components/common/Button';

interface Review {
  id: number;
  company: {
    name: string;
  };
  position: string;
  difficulty: number;
  result: 'PASS' | 'FAIL' | 'PENDING';
  createdAt: string;
}

interface ReviewsClientProps {
  initialReviews: Review[];
}

export function ReviewsClient({ initialReviews }: ReviewsClientProps) {
  const [reviews, setReviews] = useState<Review[]>(initialReviews);
  const [loading, setLoading] = useState(false);

  const fetchReviews = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/reviews`);
      const data = await response.json();

      if (data.success) {
        setReviews(data.data.content || []);
      }
    } catch (error) {
      console.error('Failed to fetch reviews:', error);
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
            <p className="mt-4 text-gray-600">면접 후기를 불러오는 중...</p>
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
          <h1 className="text-3xl font-bold text-gray-900 mb-4">면접 후기</h1>
          <p className="text-lg text-gray-600">
            다양한 회사의 면접 경험을 확인하고 참고해보세요
          </p>
        </div>

        {/* Actions */}
        <div className="mb-6 flex justify-between items-center">
          <div className="flex space-x-4">
            <Link href="/companies">
              <Button variant="outline">회사별 보기</Button>
            </Link>
          </div>
          <Link href="/auth/login">
            <Button>후기 작성하기</Button>
          </Link>
        </div>

        {/* Reviews List */}
        {reviews.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-lg shadow">
            <div className="text-gray-500 mb-4">
              <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">아직 등록된 면접 후기가 없습니다</h3>
            <p className="text-gray-600 mb-6">첫 번째 면접 후기를 작성해보세요!</p>
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
                        {review.company.name}
                      </h3>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getResultColor(review.result)}`}>
                        {getResultText(review.result)}
                      </span>
                    </div>
                    <p className="text-gray-600 mb-2">{review.position}</p>
                    <div className="flex items-center space-x-4 text-sm text-gray-500">
                      <span>난이도: {review.difficulty}/5</span>
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
      </div>
    </div>
  );
}