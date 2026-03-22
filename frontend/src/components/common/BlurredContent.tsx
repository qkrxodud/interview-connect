import { useState } from 'react';
import Link from 'next/link';
import { Button } from './Button';

interface BlurredContentProps {
  content?: string;
  blurred: boolean;
  preview?: string;
  isLoggedIn: boolean;
  className?: string;
}

export function BlurredContent({
  content,
  blurred,
  preview,
  isLoggedIn,
  className = ""
}: BlurredContentProps) {
  const [showLoginPrompt, setShowLoginPrompt] = useState(false);

  // 로그인 사용자는 전체 내용을 볼 수 있음
  if (isLoggedIn && content) {
    return (
      <div className={className}>
        <div className="text-gray-700">
          {content.split('\n').map((paragraph, index) => (
            <p key={index} className="mb-2">{paragraph}</p>
          ))}
        </div>
      </div>
    );
  }

  // 블러 처리된 콘텐츠
  if (blurred) {
    return (
      <div className={`relative ${className}`}>
        {/* Preview Text */}
        {preview && (
          <div className="text-gray-700 mb-3">
            <p>{preview}...</p>
          </div>
        )}

        {/* Blurred Overlay */}
        <div className="relative">
          <div className="absolute inset-0 bg-gradient-to-b from-transparent via-white/70 to-white z-10 rounded-lg"></div>
          <div className="filter blur-sm text-gray-400 pointer-events-none select-none">
            <p>이 답변의 나머지 내용을 보려면 로그인이 필요합니다.</p>
            <p>면접에 대한 자세한 정보와 경험담을 확인하세요.</p>
            <p className="text-xs mt-2">
              • 실제 면접 과정과 분위기<br/>
              • 구체적인 팁과 조언<br/>
              • 면접관의 반응과 피드백
            </p>
          </div>
        </div>

        {/* Login CTA */}
        <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg text-center">
          <h4 className="font-medium text-gray-900 mb-2">전체 답변 보기</h4>
          <p className="text-sm text-gray-600 mb-3">
            로그인하면 숨겨진 답변을 모두 볼 수 있습니다
          </p>
          <div className="flex justify-center space-x-2">
            <Link href="/auth/login">
              <Button size="sm">로그인</Button>
            </Link>
            <Link href="/api/v1/auth/register">
              <Button variant="outline" size="sm">회원가입</Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // 일반 콘텐츠 (블러 없음)
  if (content) {
    return (
      <div className={className}>
        <div className="text-gray-700">
          {content.split('\n').map((paragraph, index) => (
            <p key={index} className="mb-2">{paragraph}</p>
          ))}
        </div>
      </div>
    );
  }

  // 콘텐츠가 없는 경우
  return (
    <div className={`text-gray-500 italic ${className}`}>
      답변이 없습니다.
    </div>
  );
}

// 블러 효과가 적용된 텍스트 컴포넌트 (재사용 가능)
interface BlurredTextProps {
  children: React.ReactNode;
  isBlurred: boolean;
  onUnblurClick?: () => void;
  className?: string;
}

export function BlurredText({
  children,
  isBlurred,
  onUnblurClick,
  className = ""
}: BlurredTextProps) {
  if (!isBlurred) {
    return <div className={className}>{children}</div>;
  }

  return (
    <div className={`relative ${className}`}>
      <div className="filter blur-sm select-none pointer-events-none">
        {children}
      </div>
      <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/80 to-transparent"></div>
      {onUnblurClick && (
        <button
          onClick={onUnblurClick}
          className="absolute inset-0 flex items-center justify-center bg-blue-600/10 hover:bg-blue-600/20 transition-colors"
        >
          <span className="bg-blue-600 text-white px-3 py-1 rounded text-sm font-medium">
            클릭하여 보기
          </span>
        </button>
      )}
    </div>
  );
}