// 공통 타입 정의
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
  };
}

// 페이지네이션 타입
export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// 회원 관련 타입
export interface Member {
  id: number;
  email: string;
  nickname: string;
  role: 'GENERAL' | 'VERIFIED';
  createdAt: string;
}

// 회사 관련 타입
export interface Company {
  id: number;
  name: string;
  industry: string;
  size: string;
}

// 면접 후기 관련 타입
export interface InterviewReview {
  id: number;
  company: Company;
  author: Member;
  position: string;
  experience: number;
  difficulty: number;
  result: 'PASS' | 'FAIL' | 'PENDING';
  interviewDate: string;
  content: string;
  pros: string;
  cons: string;
  tips: string;
  salary?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReviewRequest {
  companyId: number;
  position: string;
  experience: number;
  difficulty: number;
  result: 'PASS' | 'FAIL' | 'PENDING';
  interviewDate: string;
  content: string;
  pros: string;
  cons: string;
  tips: string;
  salary?: number;
}

// Q&A 관련 타입
export interface ReviewQuestion {
  id: number;
  review: {
    id: number;
    company: Company;
  };
  questioner: Member;
  content: string;
  createdAt: string;
}

export interface ReviewAnswer {
  id: number;
  question: ReviewQuestion;
  answerer: Member;
  content: string;
  blurred: boolean;
  preview?: string;
  createdAt: string;
}

// 알림 관련 타입
export interface Notification {
  id: number;
  type: 'QA_NEW_QUESTION' | 'QA_NEW_ANSWER' | 'REVIEW_LIKED' | 'REVIEW_COMMENTED' | 'MESSAGE_RECEIVED' | 'SYSTEM_ANNOUNCEMENT';
  title: string;
  content: string;
  referenceId?: number;
  referenceType?: string;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationSummary {
  unreadCount: number;
  totalCount: number;
}

// 인증 관련 타입
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  passwordConfirm: string;
  nickname: string;
}

export interface AuthResponse {
  member: Member;
  accessToken: string;
}