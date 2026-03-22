'use client';

import Link from 'next/link';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/common/Button';
import { Bell, Menu, User } from 'lucide-react';

export default function Header() {
  const { user, isAuthenticated, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
  };

  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* 로고 */}
          <div className="flex items-center">
            <Link href="/" className="flex items-center space-x-2">
              <div className="bg-blue-600 text-white rounded-lg p-2">
                <span className="text-lg font-bold">IC</span>
              </div>
              <span className="text-xl font-bold text-gray-900">Interview Connect</span>
            </Link>
          </div>

          {/* 네비게이션 */}
          <nav className="hidden md:flex items-center space-x-8">
            <Link
              href="/reviews"
              className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium"
            >
              면접 후기
            </Link>
            <Link
              href="/companies"
              className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium"
            >
              회사 정보
            </Link>
            {isAuthenticated() && (
              <Link
                href="/qa"
                className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium"
              >
                Q&A
              </Link>
            )}
          </nav>

          {/* 사용자 메뉴 */}
          <div className="flex items-center space-x-4">
            {isAuthenticated() ? (
              <>
                {/* 알림 버튼 */}
                <Link
                  href="/notifications"
                  className="p-2 text-gray-400 hover:text-gray-600"
                >
                  <Bell className="h-5 w-5" />
                </Link>

                {/* 사용자 메뉴 */}
                <div className="relative">
                  <div className="flex items-center space-x-2">
                    <User className="h-5 w-5 text-gray-400" />
                    <span className="text-sm font-medium text-gray-700">
                      {user?.nickname}
                    </span>
                  </div>
                </div>

                {/* 로그아웃 버튼 */}
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleLogout}
                >
                  로그아웃
                </Button>
              </>
            ) : (
              <div className="flex items-center space-x-2">
                <Link href="/auth/login">
                  <Button variant="outline" size="sm">
                    로그인
                  </Button>
                </Link>
                <Link href="/auth/register">
                  <Button size="sm">
                    회원가입
                  </Button>
                </Link>
              </div>
            )}

            {/* 모바일 메뉴 버튼 */}
            <button className="md:hidden p-2 text-gray-400 hover:text-gray-600">
              <Menu className="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}