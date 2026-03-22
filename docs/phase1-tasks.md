# Phase 1 — 세부 구현 태스크

Claude Code에게 태스크 단위로 지시할 때 사용하는 체크리스트.
각 태스크는 독립적으로 구현 가능하며, 순서대로 진행을 권장한다.

---

## Week 1: 프로젝트 초기화 + 회원 시스템

### Task 1-1: 프로젝트 초기화
```
지시: "Gradle 멀티모듈 Spring Boot 프로젝트를 생성해줘"
```
- [ ] Spring Boot 3.x + Java 17 + Gradle 프로젝트 생성
- [ ] 멀티모듈 설정: `ic-api`, `ic-domain`, `ic-infra`, `ic-common`
- [ ] `settings.gradle`에 모듈 등록
- [ ] 루트 `build.gradle`에 공통 의존성 설정
- [ ] 각 모듈별 `build.gradle` 작성
- [ ] 의존성: Spring Web, Spring Security, Spring Data JPA, MySQL Driver, Lombok, Validation
- [ ] `application.yml` 프로필 분리 (local, dev, prod)
- [ ] MySQL 연결 설정 (로컬: `jdbc:mysql://localhost:3306/interview_connect`)
- [ ] Redis 연결 설정 (로컬: `localhost:6379`)

**모듈 의존성 방향:**
```
ic-api → ic-domain, ic-infra, ic-common
ic-domain → ic-common
ic-infra → ic-domain, ic-common
ic-common → (의존성 없음)
```

### Task 1-2: 공통 모듈 (ic-common)
```
지시: "ic-common 모듈에 공통 예외 처리와 API 응답 래퍼를 만들어줘"
```
- [ ] `ApiResponse<T>` — 통일 응답 래퍼
```java
public record ApiResponse<T>(boolean success, T data, ErrorResponse error) {
    public static <T> ApiResponse<T> ok(T data) { ... }
    public static ApiResponse<Void> error(ErrorCode code, String message) { ... }
}
```
- [ ] `ErrorCode` enum — 에러 코드 정의
```java
public enum ErrorCode {
    // Auth
    INVALID_CREDENTIALS(401, "A001", "이메일 또는 비밀번호가 올바르지 않습니다"),
    EXPIRED_TOKEN(401, "A002", "토큰이 만료되었습니다"),
    UNAUTHORIZED(401, "A003", "로그인이 필요합니다"),
    FORBIDDEN(403, "A004", "권한이 없습니다"),
    
    // Member
    DUPLICATE_EMAIL(409, "M001", "이미 가입된 이메일입니다"),
    MEMBER_NOT_FOUND(404, "M002", "회원을 찾을 수 없습니다"),
    
    // Review
    REVIEW_NOT_FOUND(404, "R001", "후기를 찾을 수 없습니다"),
    REVIEW_PERMISSION_DENIED(403, "R002", "인증 회원만 후기를 작성할 수 있습니다"),
    
    // Company
    COMPANY_NOT_FOUND(404, "C001", "회사를 찾을 수 없습니다"),
    
    // QA
    QUESTION_NOT_FOUND(404, "Q001", "질문을 찾을 수 없습니다"),
    ANSWER_PERMISSION_DENIED(403, "Q002", "인증 회원만 답변할 수 있습니다"),
    
    // Common
    INVALID_INPUT(400, "G001", "입력값이 올바르지 않습니다"),
    INTERNAL_ERROR(500, "G002", "서버 내부 오류가 발생했습니다");
}
```
- [ ] `BusinessException` — 비즈니스 예외 클래스
- [ ] `GlobalExceptionHandler` — `@RestControllerAdvice` 전역 예외 핸들러
- [ ] `BaseTimeEntity` — `createdAt`, `updatedAt` 자동 관리 (`@MappedSuperclass` + `@EntityListeners`)

### Task 1-3: 회원 엔티티 (ic-domain)
```
지시: "Member 엔티티와 Repository를 만들어줘"
```
- [ ] `Member` 엔티티
```java
@Entity
public class Member extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;  // BCrypt 해시
    
    @Column(unique = true, nullable = false, length = 30)
    private String nickname;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;  // GENERAL, VERIFIED, ADMIN
}
```
- [ ] `MemberRole` enum: `GENERAL`, `VERIFIED`, `ADMIN`
- [ ] `MemberRepository` extends `JpaRepository<Member, Long>`
  - `Optional<Member> findByEmail(String email)`
  - `boolean existsByEmail(String email)`
  - `boolean existsByNickname(String nickname)`
- [ ] `MemberService` — 회원 조회, 등급 변경 등 도메인 로직

### Task 1-4: JWT 인증 (ic-infra)
```
지시: "JWT 토큰 발급/검증과 Redis 기반 Refresh Token을 구현해줘"
```
- [ ] `JwtTokenProvider` — Access Token (30분) + Refresh Token (7일) 발급/검증
  - `generateAccessToken(Long memberId, MemberRole role)`
  - `generateRefreshToken(Long memberId)`
  - `validateToken(String token)`
  - `getMemberIdFromToken(String token)`
- [ ] `JwtAuthenticationFilter` — `OncePerRequestFilter`, Authorization 헤더에서 토큰 추출
- [ ] `RefreshTokenRepository` — Redis에 Refresh Token 저장/조회/삭제
  - Key: `refresh:{memberId}`, Value: refreshToken, TTL: 7일
- [ ] application.yml에 JWT secret, 만료 시간 설정

### Task 1-5: Security 설정 (ic-api)
```
지시: "SecurityConfig를 만들어줘. GET /api/v1/reviews와 /api/v1/companies는 permitAll"
```
- [ ] `SecurityConfig` — `SecurityFilterChain` 설정
```java
http
  .csrf(AbstractHttpConfigurer::disable)
  .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
  .authorizeHttpRequests(auth -> auth
      // 비로그인 허용 (핵심!)
      .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
      .requestMatchers(HttpMethod.GET, "/api/v1/companies/**").permitAll()
      .requestMatchers("/api/v1/auth/**").permitAll()
      // 나머지 인증 필요
      .anyRequest().authenticated()
  )
  .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
```
- [ ] `CustomUserDetails` + `CustomUserDetailsService` (선택: SecurityContext에서 회원 정보 접근)
- [ ] `AuthenticationArgumentResolver` — Controller에서 `@AuthMember` 어노테이션으로 현재 회원 주입

### Task 1-6: 인증 API (ic-api)
```
지시: "AuthController에 회원가입, 로그인, 토큰 갱신 API를 만들어줘"
```
- [ ] `POST /api/v1/auth/signup` — 회원가입
  - Request: `{ email, password, nickname }`
  - 이메일 중복 체크, 닉네임 중복 체크
  - 비밀번호 BCrypt 해싱
  - Response: `ApiResponse<SignupResponse>` (memberId, email, nickname)
- [ ] `POST /api/v1/auth/login` — 로그인
  - Request: `{ email, password }`
  - 이메일로 회원 조회 → BCrypt 비교
  - Access Token + Refresh Token 발급
  - Response: `ApiResponse<LoginResponse>` (accessToken, refreshToken, memberInfo)
- [ ] `POST /api/v1/auth/refresh` — 토큰 갱신
  - Request: `{ refreshToken }`
  - Redis에서 Refresh Token 검증
  - 새 Access Token 발급
- [ ] `POST /api/v1/auth/logout` — 로그아웃
  - Redis에서 Refresh Token 삭제

---

## Week 2: 회사 + 후기 CRUD

### Task 2-1: 회사 엔티티 + API
```
지시: "Company 엔티티와 검색/자동완성 API를 만들어줘"
```
- [ ] `Company` 엔티티
```java
@Entity
public class Company extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    private String industry;  // IT, 금융, 제조 등
    private String logoUrl;
    private String website;
}
```
- [ ] `CompanyRepository`
  - `List<Company> findByNameContaining(String keyword)` — 자동완성
- [ ] `GET /api/v1/companies?q={keyword}` — 회사 검색 (permitAll)
  - 이름 부분 일치 검색, 최대 10건 반환
- [ ] `POST /api/v1/companies` — 회사 등록 (ADMIN만)
- [ ] 씨드 데이터: IT 대기업/스타트업 50개 회사

### Task 2-2: 면접 후기 엔티티
```
지시: "InterviewReview 엔티티를 만들어줘"
```
- [ ] `InterviewReview` 엔티티
```java
@Entity
public class InterviewReview extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    private LocalDate interviewDate;
    
    @Column(nullable = false, length = 50)
    private String position;  // 백엔드, 프론트엔드 등
    
    @Convert(converter = StringListConverter.class)
    private List<String> interviewTypes;  // 코딩테스트, 기술면접 등
    
    @Convert(converter = StringListConverter.class)
    private List<String> questions;  // 면접 질문 목록
    
    @Column(nullable = false)
    private int difficulty;  // 1~5
    
    @Column(nullable = false)
    private int atmosphere;  // 1~5
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewResult result;  // PASS, FAIL, PENDING
    
    @Column(columnDefinition = "TEXT")
    private String content;  // 자유 후기
    
    private long viewCount;
}
```
- [ ] `InterviewResult` enum: `PASS`, `FAIL`, `PENDING`
- [ ] `StringListConverter` — `List<String>` ↔ JSON 변환
- [ ] `InterviewReviewRepository`

### Task 2-3: 후기 CRUD API
```
지시: "후기 작성/조회/수정/삭제 API를 만들어줘. 목록/상세 조회는 비로그인 허용"
```
- [ ] `POST /api/v1/reviews` — 후기 작성 (VERIFIED 회원만)
  - `@PreAuthorize("hasRole('VERIFIED')")` 또는 서비스에서 role 체크
  - Request: `{ companyId, interviewDate, position, interviewTypes, questions, difficulty, atmosphere, result, content }`
- [ ] `GET /api/v1/reviews` — 후기 목록 (permitAll, 비로그인 공개)
  - 쿼리 파라미터: `companyId`, `position`, `difficulty`, `result`, `page`, `size`, `sort`
  - Response: Page 형태 (totalPages, totalElements, content[])
  - 각 항목: id, company(name), position, difficulty, atmosphere, result, questionCount(Q&A 수), viewCount, createdAt
- [ ] `GET /api/v1/reviews/{id}` — 후기 상세 (permitAll, 비로그인 공개)
  - 조회수 증가 (Redis increment → 주기적 DB 반영)
  - Response: 전체 필드 + 작성자 닉네임 + Q&A 개수
- [ ] `PUT /api/v1/reviews/{id}` — 후기 수정 (작성자만)
- [ ] `DELETE /api/v1/reviews/{id}` — 후기 삭제 (작성자만)

---

## Week 3: 공개 Q&A + 답변 블러

### Task 3-1: Q&A 엔티티
```
지시: "ReviewQuestion과 ReviewAnswer 엔티티를 만들어줘"
```
- [ ] `ReviewQuestion` 엔티티
```java
@Entity
public class ReviewQuestion extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private InterviewReview review;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "asker_id", nullable = false)
    private Member asker;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    private boolean isAnonymous;
}
```
- [ ] `ReviewAnswer` 엔티티
```java
@Entity
public class ReviewAnswer extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private ReviewQuestion question;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "answerer_id", nullable = false)
    private Member answerer;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
```

### Task 3-2: Q&A API + 블러 처리 (핵심)
```
지시: "Q&A 조회 API를 만들어줘. 비로그인이면 답변을 블러 처리해서 내려줘"
```
- [ ] `GET /api/v1/reviews/{reviewId}/qa` — Q&A 조회 (permitAll)
  - **비로그인 시**: 답변 content=null, blurred=true, preview=앞10자
  - **로그인 시**: 답변 전체 공개
  - 구현: SecurityContext에서 인증 여부 확인 → DTO 변환 시 분기
```java
// QaResponse 내 AnswerResponse
public record AnswerResponse(
    Long answerId,
    String content,      // 비로그인이면 null
    boolean blurred,     // 비로그인이면 true
    String preview,      // 비로그인이면 앞 10자
    String answererNickname,  // 비로그인이면 null
    LocalDateTime createdAt
) {
    public static AnswerResponse blurred(ReviewAnswer answer) {
        String preview = answer.getContent().length() > 10 
            ? answer.getContent().substring(0, 10) + "..." 
            : answer.getContent();
        return new AnswerResponse(answer.getId(), null, true, preview, null, answer.getCreatedAt());
    }
    
    public static AnswerResponse revealed(ReviewAnswer answer) {
        return new AnswerResponse(answer.getId(), answer.getContent(), false, null, 
            answer.getAnswerer().getNickname(), answer.getCreatedAt());
    }
}
```
- [ ] `POST /api/v1/reviews/{reviewId}/questions` — 질문 작성 (로그인 필요)
  - Request: `{ content, isAnonymous }`
  - 작성 후 후기 작성자에게 알림 생성
- [ ] `POST /api/v1/reviews/{reviewId}/questions/{questionId}/answers` — 답변 작성 (VERIFIED만)
  - 후기 작성자 + 같은 회사 인증 회원만 답변 가능
  - Request: `{ content }`

### Task 3-3: 알림 기본
```
지시: "Notification 엔티티와 기본 알림 API를 만들어줘"
```
- [ ] `Notification` 엔티티
```java
@Entity
public class Notification extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;  // NEW_QUESTION, NEW_ANSWER, CHAT_REQUEST, VERIFICATION_COMPLETE
    
    private String content;
    private Long referenceId;  // 관련 엔티티 ID
    private boolean isRead;
}
```
- [ ] `GET /api/v1/notifications` — 내 알림 목록
- [ ] `PATCH /api/v1/notifications/{id}/read` — 읽음 처리
- [ ] 알림 생성 로직: 질문 작성 시 → 후기 작성자에게, 답변 작성 시 → 질문 작성자에게

---

## Week 4: 프론트엔드 + 배포

### Task 4-1: Next.js 프로젝트 초기화
```
지시: "Next.js 14 App Router + TypeScript 프로젝트를 생성해줘"
```
- [ ] `create-next-app` with TypeScript, Tailwind CSS, App Router
- [ ] 프로젝트 구조:
```
frontend/src/
├── app/
│   ├── layout.tsx
│   ├── page.tsx                    ← 랜딩
│   ├── reviews/
│   │   ├── page.tsx                ← 후기 목록
│   │   └── [id]/page.tsx           ← 후기 상세 + Q&A
│   ├── auth/
│   │   ├── login/page.tsx
│   │   └── signup/page.tsx
│   └── my/page.tsx                 ← 마이페이지
├── components/
│   ├── ReviewCard.tsx
│   ├── QaSection.tsx               ← Q&A + 블러 UI
│   ├── BlurredAnswer.tsx           ← 답변 블러 컴포넌트 (핵심)
│   ├── Header.tsx
│   └── BottomNav.tsx
├── lib/
│   ├── api.ts                      ← API 클라이언트 (fetch wrapper)
│   └── auth.ts                     ← 토큰 관리
└── types/
    └── index.ts                    ← 타입 정의
```

### Task 4-2: 블러 UI 구현 (핵심)
```
지시: "비로그인 시 답변을 블러 처리하는 BlurredAnswer 컴포넌트를 만들어줘"
```
- [ ] `BlurredAnswer` 컴포넌트
  - blurred=true일 때: CSS `filter: blur(5px)` + 오버레이
  - CTA 버튼: "🔒 로그인하면 답변을 볼 수 있어요" → /auth/signup 이동
  - blurred=false일 때: 답변 전문 표시
- [ ] 후기 상세 페이지에서 Q&A 섹션에 BlurredAnswer 적용

### Task 4-3: SSR + SEO
```
지시: "후기 상세 페이지를 SSR로 만들어서 SEO에 최적화해줘"
```
- [ ] 후기 상세 (`/reviews/[id]`): `generateMetadata`로 OG 태그 생성
  - `title: "{회사명} {직무} 면접 후기 — 난이도 {N}/5 | Interview Connect"`
  - `description`: 후기 content 앞 150자
- [ ] 후기 목록: 서버 컴포넌트에서 API 호출

### Task 4-4: CI/CD + 배포
```
지시: "GitHub Actions CI/CD와 AWS 배포 설정을 만들어줘"
```
- [ ] `.github/workflows/ci.yml` — PR 시 빌드/테스트
- [ ] `.github/workflows/deploy.yml` — main 머지 시 배포
- [ ] Docker + docker-compose (backend + mysql + redis)
- [ ] AWS 배포 스크립트 또는 설정

### Task 4-5: 씨드 데이터
```
지시: "DataInitializer를 만들어서 회사 50개와 샘플 후기 데이터를 넣어줘"
```
- [ ] `DataInitializer` — `@Component` + `ApplicationRunner`
  - 회사 50개 (카카오, 네이버, 토스, 라인, 쿠팡, 배민 등 실제 IT 기업)
  - 샘플 후기 10~20건
  - 샘플 Q&A 각 후기당 3~5개
- [ ] 로컬 개발 프로필에서만 실행 (`@Profile("local")`)
