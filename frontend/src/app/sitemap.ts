import { MetadataRoute } from 'next';

const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL || 'https://interview-connect.com';

// 백엔드에서 후기 ID 목록 가져오기
async function getReviewIds(): Promise<number[]> {
  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/reviews?size=1000`, {
      next: { revalidate: 3600 }, // 1시간마다 재생성
    });

    if (!response.ok) {
      return [];
    }

    const data = await response.json();
    if (data.success && data.data.content) {
      return data.data.content.map((review: any) => review.id);
    }

    return [];
  } catch (error) {
    console.error('Failed to fetch review IDs for sitemap:', error);
    return [];
  }
}

// 회사 ID 목록 가져오기
async function getCompanyIds(): Promise<number[]> {
  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/companies?size=1000`, {
      next: { revalidate: 3600 }, // 1시간마다 재생성
    });

    if (!response.ok) {
      return [];
    }

    const data = await response.json();
    if (data.success && data.data) {
      return data.data.map((company: any) => company.id);
    }

    return [];
  } catch (error) {
    console.error('Failed to fetch company IDs for sitemap:', error);
    return [];
  }
}

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const reviewIds = await getReviewIds();
  const companyIds = await getCompanyIds();

  // 정적 페이지들
  const staticPages: MetadataRoute.Sitemap = [
    {
      url: SITE_URL,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 1,
    },
    {
      url: `${SITE_URL}/reviews`,
      lastModified: new Date(),
      changeFrequency: 'hourly',
      priority: 0.9,
    },
    {
      url: `${SITE_URL}/companies`,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 0.8,
    },
    {
      url: `${SITE_URL}/auth/login`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.3,
    },
    {
      url: `${SITE_URL}/auth/register`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.3,
    },
  ];

  // 동적 후기 페이지들
  const reviewPages: MetadataRoute.Sitemap = reviewIds.map((id) => ({
    url: `${SITE_URL}/reviews/${id}`,
    lastModified: new Date(),
    changeFrequency: 'weekly' as const,
    priority: 0.7,
  }));

  // 동적 회사 페이지들
  const companyPages: MetadataRoute.Sitemap = companyIds.map((id) => ({
    url: `${SITE_URL}/companies/${id}/reviews`,
    lastModified: new Date(),
    changeFrequency: 'daily' as const,
    priority: 0.6,
  }));

  return [...staticPages, ...reviewPages, ...companyPages];
}