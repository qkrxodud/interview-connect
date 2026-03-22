import Link from 'next/link';
import { Metadata } from 'next';
import { Button } from '@/components/common/Button';
import { generateMetadata as genMeta, generateStructuredData, createJsonLd } from '@/lib/metadata';

export const metadata: Metadata = genMeta({
  title: '면접 경험 공유 플랫폼',
  description: '실제 면접 경험자와 구직자를 연결하는 플랫폼. 회사별 면접 후기, Q&A, 그리고 생생한 면접 경험을 공유하세요.',
  keywords: ['면접후기', '면접경험', '취업준비', '면접질문', '채용정보', '구직정보', '면접팁', '커리어'],
  type: 'website',
});

// 구조화된 데이터
const websiteStructuredData = generateStructuredData('WebSite', {});
const organizationStructuredData = generateStructuredData('Organization', {});

export default function Home() {
  return (
    <div className="bg-white">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-r from-blue-600 to-blue-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold text-white mb-6">
              면접 경험을 공유하고
              <br />
              성공을 함께 만들어가세요
            </h1>
            <p className="text-xl text-blue-100 mb-8 max-w-3xl mx-auto">
              실제 면접 경험자들의 생생한 후기와 Q&A를 통해
              <br />
              면접을 준비하고 취업 성공률을 높이세요
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link href="/reviews">
                <Button size="lg" className="bg-white text-blue-600 hover:bg-gray-100">
                  면접 후기 보기
                </Button>
              </Link>
              <Link href="/auth/register">
                <Button size="lg" variant="outline" className="border-white text-white hover:bg-white hover:text-blue-600">
                  회원가입하기
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              Interview Connect만의 특별함
            </h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              단순한 후기 공유를 넘어 실질적인 도움을 제공합니다
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Feature 1 */}
            <div className="text-center p-6">
              <div className="bg-blue-100 rounded-full p-4 w-16 h-16 mx-auto mb-4 flex items-center justify-center">
                <span className="text-2xl">📝</span>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                실제 면접 후기
              </h3>
              <p className="text-gray-600">
                합격자와 불합격자 모두의 솔직한 면접 경험담을 확인하세요
              </p>
            </div>

            {/* Feature 2 */}
            <div className="text-center p-6">
              <div className="bg-green-100 rounded-full p-4 w-16 h-16 mx-auto mb-4 flex items-center justify-center">
                <span className="text-2xl">💬</span>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                실시간 Q&A
              </h3>
              <p className="text-gray-600">
                궁금한 점을 직접 물어보고 면접 경험자의 답변을 받아보세요
              </p>
            </div>

            {/* Feature 3 */}
            <div className="text-center p-6">
              <div className="bg-purple-100 rounded-full p-4 w-16 h-16 mx-auto mb-4 flex items-center justify-center">
                <span className="text-2xl">🔍</span>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                회사별 정보
              </h3>
              <p className="text-gray-600">
                관심 있는 회사의 면접 트렌드와 난이도를 한눈에 파악하세요
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Recent Reviews Section */}
      <section className="bg-gray-50 py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              최신 면접 후기
            </h2>
            <p className="text-lg text-gray-600">
              다른 구직자들의 최신 면접 경험을 확인해보세요
            </p>
          </div>

          <div className="text-center">
            <Link href="/reviews">
              <Button size="lg">
                모든 후기 보기
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-blue-600 py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl font-bold text-white mb-4">
            지금 시작해보세요
          </h2>
          <p className="text-xl text-blue-100 mb-8">
            면접 준비의 새로운 방법을 경험해보세요
          </p>
          <Link href="/auth/register">
            <Button size="lg" className="bg-white text-blue-600 hover:bg-gray-100">
              무료로 시작하기
            </Button>
          </Link>
        </div>
      </section>

      {/* Structured Data */}
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={createJsonLd(websiteStructuredData)}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={createJsonLd(organizationStructuredData)}
      />
    </div>
  );
}