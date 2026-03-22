# Interview Connect - Frontend

면접 경험 공유 플랫폼의 프론트엔드 애플리케이션입니다.

## 🛠 기술 스택

- **Next.js 16** - App Router 사용
- **TypeScript** - 타입 안정성
- **Tailwind CSS** - 스타일링
- **Zustand** - 상태 관리
- **TanStack Query** - 서버 상태 관리
- **Axios** - HTTP 클라이언트
- **Lucide React** - 아이콘

## 📁 프로젝트 구조

```
src/
├── app/                    # Next.js App Router 페이지
├── components/             # React 컴포넌트
│   ├── common/            # 공통 컴포넌트
│   ├── layout/            # 레이아웃 컴포넌트
│   ├── auth/              # 인증 관련 컴포넌트
│   ├── review/            # 후기 관련 컴포넌트
│   ├── qa/                # Q&A 관련 컴포넌트
│   └── notification/      # 알림 관련 컴포넌트
├── lib/                   # 유틸리티 및 설정
│   ├── api/               # API 클라이언트
│   ├── axios.ts           # Axios 설정
│   ├── queryClient.ts     # React Query 설정
│   └── utils.ts           # 유틸리티 함수
├── store/                 # Zustand 스토어
├── types/                 # TypeScript 타입 정의
└── hooks/                 # Custom React Hooks
```

## 🚀 시작하기

### 환경 설정

1. 환경 변수 파일 생성:
```bash
cp .env.example .env.local
```

2. 환경 변수 설정:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your-nextauth-secret-here
```

### 개발 서버 실행

```bash
# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

브라우저에서 [http://localhost:3000](http://localhost:3000)을 열어 결과를 확인하세요.

### 빌드 및 배포

```bash
# 프로덕션 빌드
npm run build

# 프로덕션 서버 실행
npm run start

# 린팅
npm run lint
```

## 🔗 백엔드 연동

이 프론트엔드 애플리케이션은 Spring Boot 백엔드와 연동됩니다:

- 백엔드 서버: `http://localhost:8080`
- API 엔드포인트: `/api/v1/*`
- 인증: JWT 토큰 기반

백엔드 서버가 실행 중이어야 완전한 기능을 사용할 수 있습니다.

## 📋 주요 기능

### Phase 1 기능
- ✅ 회원가입/로그인
- ✅ 면접 후기 작성/조회
- ✅ Q&A 기능 (블러 처리)
- ✅ 알림 시스템
- ✅ 회사 정보 조회

### Phase 2 예정 기능
- 🔄 실시간 알림 (WebSocket)
- 🔄 1:1 쪽지 기능
- 🔄 멘토링 매칭
- 🔄 회원 인증 시스템

## 🎨 디자인 시스템

- **색상**: Blue 계열 (Primary: #2563eb)
- **폰트**: Geist Sans
- **반응형**: Mobile First 접근
- **컴포넌트**: 재사용 가능한 UI 컴포넌트

## 📱 반응형 지원

- **Mobile**: 320px~
- **Tablet**: 768px~
- **Desktop**: 1024px~
