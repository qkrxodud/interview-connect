import { Metadata } from 'next';
import Link from 'next/link';
import { Button } from '@/components/common/Button';
import { generateMetadata as genMeta, generateStructuredData, createJsonLd } from '@/lib/metadata';
import { ReviewsClient } from './ReviewsClient';

export const metadata: Metadata = genMeta({
  title: '면접 후기 목록',
  description: '다양한 회사의 실제 면접 후기와 경험담을 확인해보세요. IT, 금융, 제조업 등 업종별 면접 정보와 Q&A를 통해 면접을 준비하세요.',
  keywords: ['면접후기목록', '회사별면접후기', '면접경험담', '면접질문', '면접준비', '취업정보'],
  type: 'website',
  url: `${process.env.NEXT_PUBLIC_SITE_URL}/reviews`,
});

// 서버사이드에서 초기 데이터 페칭
async function getReviewsData() {
  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/reviews?size=10`, {
      cache: 'no-store',
    });

    if (!response.ok) {
      return [];
    }

    const data = await response.json();
    return data.success ? (data.data.content || []) : [];
  } catch (error) {
    console.error('Failed to fetch reviews:', error);
    return [];
  }
}

export default async function ReviewsPage() {
  // 서버사이드에서 초기 데이터 페칭
  const reviews = await getReviewsData();

  return (
    <div>
      {/* 클라이언트 컴포넌트에 SSR 데이터 전달 */}
      <ReviewsClient initialReviews={reviews} />
    </div>
  );
}