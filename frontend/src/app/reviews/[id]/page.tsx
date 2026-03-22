import { Metadata } from 'next';
import Link from 'next/link';
import { Button } from '@/components/common/Button';
import { BlurredContent } from '@/components/common/BlurredContent';
import { generateMetadata as genMeta, generateStructuredData, createJsonLd } from '@/lib/metadata';
import { ReviewDetailClient } from './ReviewDetailClient';

// 서버사이드에서 데이터 페칭
async function getReviewData(reviewId: string) {
  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/reviews/${reviewId}`, {
      cache: 'no-store', // 항상 최신 데이터
    });

    if (!response.ok) {
      return null;
    }

    const data = await response.json();
    return data.success ? data.data : null;
  } catch (error) {
    console.error('Failed to fetch review:', error);
    return null;
  }
}

// 동적 메타데이터 생성
export async function generateMetadata({ params }: { params: { id: string } }): Promise<Metadata> {
  const review = await getReviewData(params.id);

  if (!review) {
    return genMeta({
      title: '면접 후기를 찾을 수 없습니다',
      description: '요청하신 면접 후기를 찾을 수 없습니다.',
    });
  }

  const title = `${review.company.name} ${review.position} 면접 후기`;
  const description = `${review.company.name} ${review.position} 포지션 면접 후기입니다. 난이도 ${review.difficulty}/5, ${review.result === 'PASS' ? '합격' : review.result === 'FAIL' ? '불합격' : '대기중'} - ${review.content.substring(0, 100)}...`;

  return genMeta({
    title,
    description,
    keywords: [
      review.company.name,
      review.position,
      '면접후기',
      '면접경험',
      '채용정보',
      review.result === 'PASS' ? '합격후기' : '면접경험',
      ...review.interviewTypes
    ],
    type: 'article',
    publishedTime: review.createdAt,
    authors: [review.member.nickname],
    section: '면접후기',
    url: `${process.env.NEXT_PUBLIC_SITE_URL}/reviews/${params.id}`,
  });
}

export default async function ReviewDetailPage({ params }: { params: { id: string } }) {
  // 서버사이드에서 데이터 페칭
  const review = await getReviewData(params.id);

  // 구조화된 데이터 생성
  let reviewStructuredData = null;
  if (review) {
    reviewStructuredData = generateStructuredData('Review', {
      companyName: review.company.name,
      author: review.member.nickname,
      rating: review.difficulty, // 난이도를 평점으로 사용
      content: review.content,
      publishedTime: review.createdAt,
    });
  }

  return (
    <div>
      {/* 클라이언트 컴포넌트에 SSR 데이터 전달 */}
      <ReviewDetailClient
        reviewId={params.id}
        initialReview={review}
      />

      {/* 구조화된 데이터 */}
      {reviewStructuredData && (
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={createJsonLd(reviewStructuredData)}
        />
      )}
    </div>
  );
}