# Week 1 실행 계획 — 프로젝트 초기화 + 회원 시스템

`backend-implementer`, `tdd-test-agent`가 이 파일을 참조한다.
완료 항목은 `[x]`로 표시한다.

---

## 목표

Spring Boot 멀티모듈 프로젝트를 세팅하고, JWT 기반 회원 인증을 완성한다.
이 주차가 끝나면 회원가입/로그인/토큰 갱신/로그아웃 API가 동작해야 한다.

---

## Task 1-1: 프로젝트 초기화

**담당:** backend-implementer
**선결 조건:** 없음

- [x] Spring Boot 3.x + Java 17 + Gradle 멀티모듈 프로젝트 생성
- [x] 멀티모듈: `ic-api`, `ic-domain`, `ic-infra`, `ic-common`
- [x] `settings.gradle` 모듈 등록
- [x] 루트 `build.gradle` 공통 의존성 (Spring Web, Security, JPA, MySQL, Lombok, Validation)
- [x] `application.yml` 프로필 분리 (local, dev, prod)
- [x] MySQL 연결 설정 (`jdbc:mysql://localhost:3306/interview_connect`)
- [x] Redis 연결 설정 (`localhost:6379`)

**모듈 의존성 방향 (불변):**
```
ic-api     → ic-domain, ic-infra, ic-common
ic-domain  → ic-common
ic-infra   → ic-domain, ic-common
ic-common  → (의존 없음)
```

---

## Task 1-2: 공통 모듈 (ic-common)

**담당:** backend-implementer
**선결 조건:** Task 1-1

- [x] `ApiResponse<T>` — 통일 응답 래퍼
- [x] `ErrorCode` enum — A001~G002 전체 에러 코드
- [x] `BusinessException` — `from(ErrorCode)`, `of(ErrorCode, message)`
- [x] `GlobalExceptionHandler` — `@RestControllerAdvice` 전역 핸들러
- [x] `BaseTimeEntity` — `createdAt`, `updatedAt` 자동 관리

**테스트:** ic-common은 단위 테스트로 ApiResponse, BusinessException 동작 확인.

---

## Task 1-3: 회원 엔티티 (ic-domain)

**담당:** backend-implementer  
**선결 조건:** Task 1-2

- [x] `Member` 엔티티 (email unique, password BCrypt, nickname unique, role enum)
- [x] `MemberRole` enum: `GENERAL`, `VERIFIED`, `ADMIN`
  - `isVerified()`, `isAdmin()` 메서드 포함 (Enum에 위임)
- [x] `MemberRepository` (JpaRepository 확장)
  - `findByEmail`, `existsByEmail`, `existsByNickname`
- [x] `MemberService` — 회원 조회/등급 변경 도메인 로직

**테스트 (tdd-test-agent):**
```
FakeMemberRepository를 사용한 MemberService 단위 테스트:
- 이메일 중복 시 DUPLICATE_EMAIL 예외
- 닉네임 중복 시 DUPLICATE_NICKNAME 예외
- 존재하지 않는 회원 조회 시 MEMBER_NOT_FOUND 예외
- 등급 변경 (changeRole) 정상 동작
```

---

## Task 1-4: JWT 인증 (ic-infra)

**담당:** backend-implementer  
**선결 조건:** Task 1-3

- [x] `JwtTokenProvider`
  - `generateAccessToken(Long memberId, MemberRole role)` — 30분 만료
  - `generateRefreshToken(Long memberId)` — 7일 만료
  - `validateToken(String token)` — 만료/변조 검증
  - `getMemberIdFromToken(String token)` — claim 추출
- [x] `JwtAuthenticationFilter` — `OncePerRequestFilter`
  - Authorization 헤더에서 Bearer 토큰 추출
  - 유효하면 SecurityContext에 Authentication 저장
- [x] `RefreshTokenRepository` — Redis 저장/조회/삭제
  - Key: `refresh:{memberId}`, TTL: 7일

**application.yml 설정:**
```yaml
jwt:
  secret: ${JWT_SECRET}
  access-token-expiry: 1800000   # 30분 (ms)
  refresh-token-expiry: 604800000 # 7일 (ms)
```

---

## Task 1-5: Security 설정 (ic-api)

**담당:** backend-implementer  
**선결 조건:** Task 1-4

- [x] `SecurityConfig` — `SecurityFilterChain`
  - CSRF disabled, Stateless 세션
  - `GET /api/v1/reviews/**` → permitAll
  - `GET /api/v1/companies/**` → permitAll
  - `/api/v1/auth/**` → permitAll
  - 나머지 → authenticated
- [x] `AuthenticationArgumentResolver` — `@AuthMember` 어노테이션으로 현재 회원 주입

---

## Task 1-6: 인증 API (ic-api)

**담당:** backend-implementer  
**선결 조건:** Task 1-5

- [x] `POST /api/v1/auth/signup`
  - 이메일/닉네임 중복 체크 → BCrypt 해싱 → 저장 → `SignupResponse` 반환
- [x] `POST /api/v1/auth/login`
  - 이메일 조회 → BCrypt 비교 → Access/Refresh Token 발급 → Redis 저장
- [x] `POST /api/v1/auth/refresh`
  - Redis에서 Refresh Token 검증 → 새 Access Token 발급
- [x] `POST /api/v1/auth/logout`
  - Redis에서 Refresh Token 삭제

**통합 테스트 (BaseApiWebClientTest):**
```
- POST /auth/signup → 201, memberId 반환
- POST /auth/signup 중복 이메일 → 409 DUPLICATE_EMAIL
- POST /auth/login 정상 → 200, accessToken 반환
- POST /auth/login 잘못된 비밀번호 → 401 INVALID_CREDENTIALS
- POST /auth/refresh 정상 → 200, 새 accessToken
- POST /auth/logout → 200, 이후 refresh 시 실패
```

---

## Week 1 완료 기준

- [ ] `./gradlew :ic-api:test` 전체 통과
- [ ] 회원가입/로그인/로그아웃 curl 테스트 성공
- [ ] 테스트 커버리지 90% 이상 (JaCoCo)
