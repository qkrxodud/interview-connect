# ARCHITECTURE.md — Interview Connect 시스템 아키텍처

## 멀티모듈 구조

```
interview-connect/
├── ic-api/          REST 컨트롤러, Security, DTO, Filter
├── ic-domain/       엔티티, Repository 인터페이스, 도메인 서비스
├── ic-infra/        JPA 구현, Redis, JWT, 이메일 외부 연동
├── ic-common/       공통 예외, 응답 래퍼, 유틸, BaseTimeEntity
└── frontend/        Bootstrap 기반 프론트 (사용자 관리)
```

## 모듈 의존성 방향 (절대 역방향 금지)

```
ic-api ──────────────→ ic-domain
  │                       ↑
  └──→ ic-infra ──────────┘
  │
  └──→ ic-common ←── ic-domain
                 ←── ic-infra
```

**규칙:**
- `ic-domain`은 `ic-infra`, `ic-api`에 의존하지 않는다
- `ic-common`은 어디에도 의존하지 않는다
- `ic-api`에서 `ic-domain` 엔티티를 직접 반환하지 않는다 (DTO 변환 필수)

## 패키지 구조 상세

### ic-api (`com.ic.api`)
```
auth/
  controller/  AuthController
  dto/         SignupRequest, LoginRequest, SignupResponse, LoginResponse
  service/     AuthService (Transactional 로직)
  filter/      JwtAuthenticationFilter
review/
  controller/  ReviewController
  dto/         ReviewCreateRequest, ReviewResponse, ReviewListResponse
company/
  controller/  CompanyController
  dto/         CompanyResponse, CompanySearchResponse
qa/
  controller/  QaController
  dto/         QuestionResponse, AnswerResponse (blurred/revealed)
notification/
  controller/  NotificationController
config/
  SecurityConfig, WebConfig, ArgumentResolverConfig
```

### ic-domain (`com.ic.domain`)
```
member/
  Member.java              @Entity
  MemberRole.java          enum GENERAL/VERIFIED/ADMIN
  MemberRepository.java    interface
  MemberService.java       도메인 서비스
review/
  InterviewReview.java     @Entity
  InterviewResult.java     enum PASS/FAIL/PENDING
  InterviewReviewRepository.java
company/
  Company.java             @Entity
  CompanyRepository.java
qa/
  ReviewQuestion.java      @Entity
  ReviewAnswer.java        @Entity
  QaRepository.java
notification/
  Notification.java        @Entity
  NotificationType.java    enum
  NotificationRepository.java
comment/                   (현재 미사용, 향후 확장용)
```

### ic-infra (`com.ic.infra`)
```
jwt/
  JwtTokenProvider.java    토큰 생성/검증
  JwtAuthenticationFilter.java
redis/
  RefreshTokenRepository.java   Redis Refresh Token
  RedisConfig.java
email/
  EmailService.java        이메일 발송 (Phase 3)
data/
  StringListConverter.java  List<String> ↔ JSON
config/
  JpaConfig.java
```

### ic-common (`com.ic.common`)
```
exception/
  BusinessException.java
  ErrorCode.java           에러 코드 enum
  GlobalExceptionHandler.java   @RestControllerAdvice
response/
  ApiResponse.java         { success, data, error } 래퍼
entity/
  BaseTimeEntity.java      createdAt, updatedAt
util/
  (유틸리티 클래스)
```

## 기술 스택

| 영역 | 기술 | 버전 |
|------|------|------|
| Backend | Spring Boot | 3.x |
| Language | Java | 17 |
| ORM | Spring Data JPA | - |
| DB | MySQL | 8.0 |
| Cache/Session | Redis | - |
| Auth | JWT (JJWT) | - |
| Build | Gradle | - |
| Test | JUnit5 + AssertJ | - |

## 계층 간 데이터 흐름

```
HTTP 요청
  → JwtAuthenticationFilter (토큰 검증, SecurityContext 설정)
  → Controller (요청 DTO 수신, 응답 DTO 반환)
  → Service (비즈니스 로직, @Transactional)
  → Repository (데이터 접근)
  → DB/Redis

응답: ApiResponse<T> 래퍼로 통일
예외: BusinessException → GlobalExceptionHandler → ApiResponse<Error>
```

## API 응답 래퍼

모든 API 응답은 `ApiResponse<T>` 사용:
```json
// 성공
{ "success": true, "data": {...}, "error": null }

// 실패
{ "success": false, "data": null, "error": { "code": "M001", "message": "이미 가입된 이메일입니다", "status": 409 } }
```

## 현재 구현 완료 상태 (2026-04-19 기준)

- [x] ic-common: ApiResponse, ErrorCode, BusinessException, GlobalExceptionHandler, BaseTimeEntity
- [x] ic-domain: Member, MemberRole, MemberRepository, MemberService
- [x] ic-infra: JwtTokenProvider, JwtAuthenticationFilter, RefreshTokenRepository(Redis)
- [x] ic-api: SecurityConfig, AuthController (회원가입/로그인/갱신/로그아웃)
- [ ] ic-domain: Company, InterviewReview, ReviewQuestion, ReviewAnswer, Notification
- [ ] ic-api: CompanyController, ReviewController, QaController, NotificationController
