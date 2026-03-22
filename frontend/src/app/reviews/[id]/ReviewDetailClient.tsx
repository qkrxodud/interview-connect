'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button } from '@/components/common/Button';
import { BlurredContent } from '@/components/common/BlurredContent';

interface Review {
  id: number;
  company: {
    name: string;
  };
  position: string;
  difficulty: number;
  atmosphere: number;
  result: 'PASS' | 'FAIL' | 'PENDING';
  content: string;
  questions: string[];
  interviewTypes: string[];
  interviewDate: string;
  viewCount: number;
  createdAt: string;
  member: {
    nickname: string;
  };
}

interface QAItem {
  id: number;
  question: string;
  answers: Array<{
    id: number;
    content?: string;
    blurred: boolean;
    preview?: string;
    answerer: {
      nickname: string;
    };
    createdAt: string;
  }>;
  createdAt: string;
  questioner: {
    nickname: string;
  };
}

interface ReviewDetailClientProps {
  reviewId: string;
  initialReview?: Review;
  initialQaItems?: QAItem[];
}

export function ReviewDetailClient({ reviewId, initialReview, initialQaItems }: ReviewDetailClientProps) {
  const [review, setReview] = useState<Review | null>(initialReview || null);
  const [qaItems, setQaItems] = useState<QAItem[]>(initialQaItems || []);
  const [loading, setLoading] = useState(!initialReview);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    if (!initialReview) {
      fetchReviewData();
    }
    checkAuthStatus();
  }, [reviewId, initialReview]);

  const fetchReviewData = async () => {
    try {
      const [reviewResponse, qaResponse] = await Promise.all([
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/reviews/${reviewId}`),
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/reviews/${reviewId}/qa`)
      ]);

      const reviewData = await reviewResponse.json();
      const qaData = await qaResponse.json();

      if (reviewData.success) {
        setReview(reviewData.data);
      }

      if (qaData.success) {
        setQaItems(qaData.data || []);
      }
    } catch (error) {
      console.error('Failed to fetch review data:', error);
    } finally {
      setLoading(false);
    }
  };

  const checkAuthStatus = async () => {
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/me`, {
        credentials: 'include'
      });
      setIsLoggedIn(response.ok);
    } catch (error) {
      setIsLoggedIn(false);
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
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">면접 후기를 불러오는 중...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!review) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center py-12">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">후기를 찾을 수 없습니다</h2>
            <Link href="/reviews">
              <Button>전체 후기로 돌아가기</Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Back Button */}
        <div className="mb-6">
          <Link href="/reviews">
            <Button variant="outline">← 전체 후기로 돌아가기</Button>
          </Link>
        </div>

        {/* Review Header */}
        <div className="bg-white rounded-lg shadow p-8 mb-8">
          <div className="flex items-start justify-between mb-6">
            <div className="flex-1">
              <div className="flex items-center space-x-3 mb-3">
                <h1 className="text-2xl font-bold text-gray-900">{review.company.name}</h1>
                <span className={`px-3 py-1 rounded-full text-sm font-medium ${getResultColor(review.result)}`}>
                  {getResultText(review.result)}
                </span>
              </div>
              <p className="text-lg text-gray-700 mb-2">{review.position}</p>
              <div className="flex items-center space-x-4 text-sm text-gray-500">
                <span>난이도: {review.difficulty}/5</span>
                <span>•</span>
                <span>분위기: {review.atmosphere}/5</span>
                <span>•</span>
                <span>조회수: {review.viewCount}</span>
                <span>•</span>
                <span>{new Date(review.createdAt).toLocaleDateString()}</span>
              </div>
            </div>
          </div>

          {/* Interview Details */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div>
              <h3 className="font-semibold text-gray-900 mb-2">면접 유형</h3>
              <div className="flex flex-wrap gap-2">
                {review.interviewTypes.map((type, index) => (
                  <span key={index} className="px-2 py-1 bg-blue-100 text-blue-800 text-sm rounded">
                    {type}
                  </span>
                ))}
              </div>
            </div>
            <div>
              <h3 className="font-semibold text-gray-900 mb-2">면접 날짜</h3>
              <p className="text-gray-700">{new Date(review.interviewDate).toLocaleDateString()}</p>
            </div>
          </div>

          {/* Questions */}
          {review.questions.length > 0 && (
            <div className="mb-6">
              <h3 className="font-semibold text-gray-900 mb-3">면접 질문</h3>
              <ul className="space-y-2">
                {review.questions.map((question, index) => (
                  <li key={index} className="flex items-start">
                    <span className="flex-shrink-0 w-6 h-6 bg-blue-100 text-blue-800 text-sm rounded-full flex items-center justify-center mr-3 mt-0.5">
                      {index + 1}
                    </span>
                    <span className="text-gray-700">{question}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Review Content */}
          <div>
            <h3 className="font-semibold text-gray-900 mb-3">면접 후기</h3>
            <div className="prose max-w-none text-gray-700">
              {review.content.split('\n').map((paragraph, index) => (
                <p key={index} className="mb-3">{paragraph}</p>
              ))}
            </div>
          </div>

          {/* Author */}
          <div className="border-t pt-4 mt-6">
            <p className="text-sm text-gray-500">작성자: {review.member.nickname}</p>
          </div>
        </div>

        {/* Q&A Section */}
        <div className="bg-white rounded-lg shadow p-8">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-bold text-gray-900">질문 & 답변</h2>
            {isLoggedIn && (
              <Button size="sm">질문하기</Button>
            )}
          </div>

          {qaItems.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-500 mb-4">아직 등록된 질문이 없습니다.</p>
              {isLoggedIn ? (
                <Button>첫 번째 질문을 남겨보세요</Button>
              ) : (
                <div>
                  <p className="text-gray-600 mb-4">궁금한 점이 있으신가요?</p>
                  <Link href="/auth/login">
                    <Button>로그인하고 질문하기</Button>
                  </Link>
                </div>
              )}
            </div>
          ) : (
            <div className="space-y-6">
              {qaItems.map((item) => (
                <div key={item.id} className="border-l-4 border-blue-200 pl-4">
                  <div className="mb-3">
                    <h4 className="font-medium text-gray-900 mb-1">{item.question}</h4>
                    <p className="text-sm text-gray-500">
                      {item.questioner.nickname} • {new Date(item.createdAt).toLocaleDateString()}
                    </p>
                  </div>

                  {/* Answers */}
                  <div className="space-y-3 ml-4">
                    {item.answers.length === 0 ? (
                      <p className="text-sm text-gray-500 italic">아직 답변이 없습니다.</p>
                    ) : (
                      item.answers.map((answer) => (
                        <div key={answer.id} className="bg-gray-50 rounded-lg p-4">
                          <BlurredContent
                            content={answer.content}
                            blurred={answer.blurred}
                            preview={answer.preview}
                            isLoggedIn={isLoggedIn}
                          />
                          <div className="flex items-center justify-between mt-3">
                            <p className="text-sm text-gray-500">
                              {answer.answerer.nickname} • {new Date(answer.createdAt).toLocaleDateString()}
                            </p>
                            {answer.blurred && !isLoggedIn && (
                              <Link href="/auth/login">
                                <Button size="sm">로그인하고 전체보기</Button>
                              </Link>
                            )}
                          </div>
                        </div>
                      ))
                    )}

                    {isLoggedIn && (
                      <div className="pt-2">
                        <Button variant="outline" size="sm">답변하기</Button>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Login CTA for non-logged users */}
          {!isLoggedIn && qaItems.length > 0 && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 mt-8">
              <div className="text-center">
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  더 많은 정보를 확인하세요
                </h3>
                <p className="text-gray-600 mb-4">
                  로그인하면 모든 답변을 볼 수 있고, 직접 질문을 남길 수도 있습니다
                </p>
                <div className="flex justify-center space-x-3">
                  <Link href="/auth/login">
                    <Button>로그인</Button>
                  </Link>
                  <Link href="/auth/register">
                    <Button variant="outline">회원가입</Button>
                  </Link>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}