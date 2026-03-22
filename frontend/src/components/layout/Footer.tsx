export default function Footer() {
  return (
    <footer className="bg-white border-t border-gray-200">
      <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* 회사 정보 */}
          <div>
            <div className="flex items-center space-x-2 mb-4">
              <div className="bg-blue-600 text-white rounded-lg p-2">
                <span className="text-lg font-bold">IC</span>
              </div>
              <span className="text-xl font-bold text-gray-900">Interview Connect</span>
            </div>
            <p className="text-gray-600 text-sm">
              면접 경험을 공유하고 서로 도움을 주는 플랫폼입니다.
            </p>
          </div>

          {/* 서비스 */}
          <div>
            <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
              서비스
            </h3>
            <ul className="space-y-2">
              <li>
                <a href="/reviews" className="text-gray-600 hover:text-blue-600 text-sm">
                  면접 후기
                </a>
              </li>
              <li>
                <a href="/companies" className="text-gray-600 hover:text-blue-600 text-sm">
                  회사 정보
                </a>
              </li>
              <li>
                <a href="/qa" className="text-gray-600 hover:text-blue-600 text-sm">
                  Q&A
                </a>
              </li>
            </ul>
          </div>

          {/* 지원 */}
          <div>
            <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
              지원
            </h3>
            <ul className="space-y-2">
              <li>
                <a href="/help" className="text-gray-600 hover:text-blue-600 text-sm">
                  도움말
                </a>
              </li>
              <li>
                <a href="/contact" className="text-gray-600 hover:text-blue-600 text-sm">
                  문의하기
                </a>
              </li>
              <li>
                <a href="/privacy" className="text-gray-600 hover:text-blue-600 text-sm">
                  개인정보처리방침
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div className="mt-8 pt-8 border-t border-gray-200">
          <p className="text-gray-400 text-sm text-center">
            © {new Date().getFullYear()} Interview Connect. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
}