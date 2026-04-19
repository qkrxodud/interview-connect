# SECURITY.md — Interview Connect 보안 기준

`code-reviewer`가 구현 코드 검토 시 이 파일을 참조한다.
모든 BLOCKER 항목은 병합 전 반드시 해결해야 한다.

---

## 인증/인가

### JWT 규칙
- Access Token 만료: **30분**
- Refresh Token 만료: **7일** (Redis 저장)
- 서명 알고리즘: HS256 이상
- 토큰 검증 실패 시: `401 UNAUTHORIZED (A002/A003)`
- 로그아웃 시 Redis에서 Refresh Token 즉시 삭제

### API 보안 분류

**permitAll (인증 없이 접근 가능):**
```
GET  /api/v1/reviews/**
GET  /api/v1/companies/**
POST /api/v1/auth/signup
POST /api/v1/auth/login
POST /api/v1/auth/refresh
```

**authenticated (로그인 필요):**
```
POST /api/v1/reviews/{id}/questions
GET  /api/v1/notifications
PATCH /api/v1/notifications/{id}/read
POST /api/v1/auth/logout
```

**VERIFIED 전용:**
```
POST /api/v1/reviews
POST /api/v1/reviews/{id}/questions/{qid}/answers
```

**ADMIN 전용:**
```
POST /api/v1/companies
```

### 권한 검증 위치
- SecurityConfig의 `authorizeHttpRequests`에서 1차 검증
- 서비스 레이어에서 역할 확인 후 `BusinessException` 던지기 (2차 검증)
- Controller에서 `@PreAuthorize` 사용 금지 (서비스 레이어에서 명시적 처리)

---

## 비밀번호

- BCrypt 해싱 필수 (`PasswordEncoder` 사용)
- 평문 비밀번호 로그 출력 금지
- 비밀번호 응답 DTO에 포함 금지
- 최소 8자 이상 검증

---

## 입력 검증 (BLOCKER)

- 모든 Request DTO에 `@Valid` 적용
- Controller 파라미터에 `@Validated` 또는 `@Valid` 명시
- SQL Injection: JPA Named Query / QueryDSL 사용으로 방지 (문자열 직접 조합 금지)
- XSS: Thymeleaf 사용 시 자동 escape, JSON 응답은 Content-Type: application/json

---

## 데이터 접근 제어 (BLOCKER)

- 타인의 후기 수정/삭제 시도: `FORBIDDEN (403, A004)`
- 타인 데이터 조회 시도: 본인 소유 확인 후 반환
- 비로그인 시 blurred 응답 강제 — SecurityContext 인증 여부로 분기

---

## 민감 정보

- JWT Secret, DB Password: `application.yml` 환경변수로 분리 (`${JWT_SECRET}`)
- Redis Password: 환경변수
- 로그에 토큰, 비밀번호, 이메일 전체 출력 금지

---

## BLOCKER 체크리스트 (code-reviewer 필수 확인)

- [ ] 비밀번호 BCrypt 해싱 적용 여부
- [ ] 평문 비밀번호 로그/응답 노출 여부
- [ ] permitAll이어야 할 엔드포인트에 인증 요구 여부 (SEO 차단 위험)
- [ ] VERIFIED 전용 API에 role 체크 누락 여부
- [ ] 비로그인 시 Q&A 답변 content/answererNickname null 반환 여부
- [ ] Request DTO @Valid 적용 여부
- [ ] 환경변수 없는 하드코딩 시크릿 여부

---

## SUGGESTION (권고 사항)

- Rate Limiting: 회원가입/로그인 API에 적용 권장 (Phase 3)
- HTTPS 강제: 운영 환경에서 HTTP 리다이렉트 설정
- CORS: 프론트 도메인만 허용 (`allowedOrigins` 명시)
