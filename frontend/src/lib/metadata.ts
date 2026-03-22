import { Metadata } from 'next';

const DEFAULT_TITLE = '인터뷰 커넥트 - 면접 경험 공유 플랫폼';
const DEFAULT_DESCRIPTION = '실제 면접 경험자와 구직자를 연결하는 플랫폼. 회사별 면접 후기, Q&A, 그리고 생생한 면접 경험을 공유하세요.';
const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL || 'https://interview-connect.com';

export interface SEOProps {
  title?: string;
  description?: string;
  keywords?: string[];
  image?: string;
  url?: string;
  type?: 'website' | 'article';
  publishedTime?: string;
  modifiedTime?: string;
  authors?: string[];
  section?: string;
}

export function generateMetadata(props: SEOProps = {}): Metadata {
  const {
    title,
    description = DEFAULT_DESCRIPTION,
    keywords = ['면접', '면접후기', '취업', '채용', '면접경험', '구직', '커리어'],
    image = `${SITE_URL}/og-image.png`,
    url = SITE_URL,
    type = 'website',
    publishedTime,
    modifiedTime,
    authors,
    section
  } = props;

  const fullTitle = title ? `${title} | ${DEFAULT_TITLE}` : DEFAULT_TITLE;

  return {
    title: fullTitle,
    description,
    keywords: keywords.join(', '),

    // Open Graph
    openGraph: {
      title: fullTitle,
      description,
      url,
      siteName: '인터뷰 커넥트',
      type,
      images: [
        {
          url: image,
          width: 1200,
          height: 630,
          alt: fullTitle,
        }
      ],
      locale: 'ko_KR',
      ...(type === 'article' && {
        publishedTime,
        modifiedTime,
        authors,
        section,
      }),
    },

    // Twitter Card
    twitter: {
      card: 'summary_large_image',
      title: fullTitle,
      description,
      images: [image],
      site: '@interviewconnect',
      creator: '@interviewconnect',
    },

    // Additional SEO
    robots: {
      index: true,
      follow: true,
      googleBot: {
        index: true,
        follow: true,
        'max-video-preview': -1,
        'max-image-preview': 'large',
        'max-snippet': -1,
      },
    },

    // Verification
    verification: {
      google: process.env.GOOGLE_SITE_VERIFICATION,
      other: {
        'naver-site-verification': process.env.NAVER_SITE_VERIFICATION || '',
      },
    },

    // Alternative languages
    alternates: {
      canonical: url,
      languages: {
        'ko-KR': url,
      },
    },

    // Icons
    icons: {
      icon: '/favicon.ico',
      apple: '/apple-touch-icon.png',
    },

    // Manifest
    manifest: '/site.webmanifest',
  };
}

// 구조화된 데이터 생성
export function generateStructuredData(type: 'Organization' | 'WebSite' | 'Article' | 'Review', data: any) {
  const baseStructuredData = {
    '@context': 'https://schema.org',
  };

  switch (type) {
    case 'Organization':
      return {
        ...baseStructuredData,
        '@type': 'Organization',
        name: '인터뷰 커넥트',
        url: SITE_URL,
        logo: `${SITE_URL}/logo.png`,
        description: DEFAULT_DESCRIPTION,
        sameAs: [
          // 소셜 미디어 링크들
        ],
      };

    case 'WebSite':
      return {
        ...baseStructuredData,
        '@type': 'WebSite',
        name: '인터뷰 커넥트',
        url: SITE_URL,
        description: DEFAULT_DESCRIPTION,
        potentialAction: {
          '@type': 'SearchAction',
          target: `${SITE_URL}/search?q={search_term_string}`,
          'query-input': 'required name=search_term_string',
        },
      };

    case 'Article':
      return {
        ...baseStructuredData,
        '@type': 'Article',
        headline: data.title,
        description: data.description,
        author: {
          '@type': 'Person',
          name: data.author,
        },
        datePublished: data.publishedTime,
        dateModified: data.modifiedTime,
        publisher: {
          '@type': 'Organization',
          name: '인터뷰 커넥트',
          logo: {
            '@type': 'ImageObject',
            url: `${SITE_URL}/logo.png`,
          },
        },
        mainEntityOfPage: {
          '@type': 'WebPage',
          '@id': data.url,
        },
        image: data.image,
      };

    case 'Review':
      return {
        ...baseStructuredData,
        '@type': 'Review',
        itemReviewed: {
          '@type': 'Organization',
          name: data.companyName,
        },
        author: {
          '@type': 'Person',
          name: data.author,
        },
        reviewRating: {
          '@type': 'Rating',
          ratingValue: data.rating,
          bestRating: 5,
          worstRating: 1,
        },
        reviewBody: data.content,
        datePublished: data.publishedTime,
        publisher: {
          '@type': 'Organization',
          name: '인터뷰 커넥트',
        },
      };

    default:
      return baseStructuredData;
  }
}

// JSON-LD 스크립트 태그 생성 헬퍼
export function createJsonLd(data: any) {
  return {
    __html: JSON.stringify(data),
  };
}