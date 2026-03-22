import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // SEO 최적화 설정
  poweredByHeader: false, // X-Powered-By 헤더 제거 (보안)

  // 압축 활성화
  compress: true,

  // 이미지 최적화
  images: {
    formats: ['image/webp', 'image/avif'],
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
  },

  // 정적 파일 최적화
  assetPrefix: process.env.NODE_ENV === 'production' ? process.env.NEXT_PUBLIC_CDN_URL : '',

  // 헤더 설정
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'Referrer-Policy',
            value: 'strict-origin-when-cross-origin',
          },
          {
            key: 'Permissions-Policy',
            value: 'camera=(), microphone=(), geolocation=()',
          }
        ],
      },
      {
        source: '/reviews/:path*',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, s-maxage=3600, stale-while-revalidate=86400',
          },
        ],
      },
      {
        source: '/companies/:path*',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, s-maxage=7200, stale-while-revalidate=86400',
          },
        ],
      },
    ];
  },

  // 리다이렉트 설정 (SEO 친화적)
  async redirects() {
    return [
      {
        source: '/review/:id',
        destination: '/reviews/:id',
        permanent: true,
      },
      {
        source: '/company/:id',
        destination: '/companies/:id/reviews',
        permanent: true,
      },
    ];
  },

  // 실험적 기능
  experimental: {
    optimizeCss: true,
    optimizePackageImports: ['@/components', '@/lib'],
  },
};

export default nextConfig;
