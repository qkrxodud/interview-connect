# 인증 플로우 스펙

`product-analyst`, `backend-implementer`가 인증 관련 기능 구현 시 이 파일을 참조한다.

---

## 회원 등급 정의

| 등급 | 코드 | 가입 시 | 권한 |
|------|------|---------|------|
| 일반 회원 | `GENERAL` | 기본값 | 후기 조회, Q&A 질문 작성 |
| 인증 회원 | `VERIFIED` | 관리자 수동 부여 (Phase 1) | GENERAL + 후기/답변 작성 |
| 관리자 | `ADMIN` | 관리자 수동 부여 | 전체 권한 + 회사 등록 |

**Phase 1 중요:** VERIFIED 자동 인증 시스템은 Phase 2에서 구현. Phase 1에서는 DB 직접 수정 또는 관리자 API로 수동 부여.

---

## 회원가입 플로우

### 요청

```
POST /api/v1/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "나는개발자"
}
```

### 처리 순서

1. Bean Validation (`@Valid`) — email 형식, password 최소 8자, nickname 1~30자
2. `existsByEmail(email)` → 중복이면 `DUPLICATE_EMAIL`(409) 예외
3. `existsByNickname(nickname)` → 중복이면 `DUPLICATE_NICKNAME`(409) 예외
4. `BCryptPasswordEncoder.encode(password)`
5. `Member.createGeneral(email, encodedPassword, nickname)` 저장
6. 응답: 201 Created

### 응답

```json
{
  "success": true,
  "data": {
    "memberId": 1,
    "email": "user@example.com",
    "nickname": "나는개발자"
  },
  "error": null
}
```

### 에러 케이스

| 상황 | 코드 | HTTP |
|------|------|------|
| 이메일 중복 | M001 | 409 |
| 닉네임 중복 | M003 | 409 |
| 이메일 형식 오류 | G001 | 400 |
| 비밀번호 8자 미만 | G001 | 400 |

---

## 로그인 플로우

### 요청

```
POST /api/v1/auth/login

{
  "email": "user@example.com",
  "password": "password123"
}
```

### 처리 순서

1. `findByEmail(email)` → 없으면 `INVALID_CREDENTIALS`(401) — 이메일 존재 여부 노출 금지
2. `BCryptPasswordEncoder.matches(password, member.getPassword())` → 실패 시 `INVALID_CREDENTIALS`(401)
3. `JwtTokenProvider.generateAccessToken(memberId, role)` — 30분 만료
4. `JwtTokenProvider.generateRefreshToken(memberId)` — 7일 만료
5. Redis에 저장: `refresh:{memberId}` = refreshToken, TTL 7일

### 응답

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "member": {
      "memberId": 1,
      "email": "user@example.com",
      "nickname": "나는개발자",
      "role": "GENERAL"
    }
  },
  "error": null
}
```

### 보안 원칙

- 이메일이 없어도 "비밀번호가 틀렸습니다"가 아닌 "이메일 또는 비밀번호가 올바르지 않습니다" 반환
- 로그인 실패 횟수 제한은 Phase 3 (현재 미구현)

---

## 토큰 갱신 플로우

### 요청

```
POST /api/v1/auth/refresh

{
  "refreshToken": "eyJhbGci..."
}
```

### 처리 순서

1. `JwtTokenProvider.validateToken(refreshToken)` → 만료/변조 시 `EXPIRED_TOKEN`(401)
2. `getMemberIdFromToken(refreshToken)` — memberId 추출
3. Redis에서 조회: `refresh:{memberId}` → 없으면 `EXPIRED_TOKEN`(401)
4. 저장된 토큰과 요청 토큰 일치 여부 확인 → 불일치 시 `EXPIRED_TOKEN`(401)
5. 새 Access Token 발급 (Refresh Token은 재발급하지 않음 — 슬라이딩 윈도우 없음)

### 응답

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci..."
  },
  "error": null
}
```

---

## 로그아웃 플로우

### 요청

```
POST /api/v1/auth/logout
Authorization: Bearer {accessToken}

{
  "refreshToken": "eyJhbGci..."
}
```

### 처리 순서

1. AccessToken에서 memberId 추출
2. Redis에서 `refresh:{memberId}` 삭제
3. 응답: 200 OK

**주의:** Access Token은 만료 전까지 유효하다 (블랙리스트 미구현, Phase 3). 클라이언트가 토큰을 로컬에서 삭제해야 한다.

---

## JWT 구조

### Access Token Claims

```json
{
  "sub": "1",           // memberId
  "role": "GENERAL",   // MemberRole
  "iat": 1704067200,   // 발급 시각
  "exp": 1704069000    // 만료 시각 (30분 후)
}
```

### Refresh Token Claims

```json
{
  "sub": "1",           // memberId
  "iat": 1704067200,
  "exp": 1704672000    // 만료 시각 (7일 후)
}
```

### JwtAuthenticationFilter 동작

1. `Authorization: Bearer {token}` 헤더에서 토큰 추출
2. `validateToken(token)` 검증
3. 유효하면 `UsernamePasswordAuthenticationToken`을 SecurityContext에 저장
4. 유효하지 않으면 통과 (다음 필터로) — SecurityContext는 비어있음

---

## 테스트 체크리스트 (product-analyst 검증용)

- [ ] 회원가입 → 201, memberId 반환
- [ ] 회원가입 중복 이메일 → 409 M001
- [ ] 회원가입 중복 닉네임 → 409 M003
- [ ] 로그인 정상 → 200, accessToken + refreshToken 반환
- [ ] 로그인 이메일 없음 → 401 A001 (이메일 존재 여부 노출 금지)
- [ ] 로그인 비밀번호 틀림 → 401 A001
- [ ] 토큰 갱신 정상 → 200, 새 accessToken
- [ ] 토큰 갱신 만료된 refreshToken → 401 A002
- [ ] 로그아웃 후 토큰 갱신 시도 → 401 A002
- [ ] 유효한 accessToken으로 인증 필요 API 접근 → 200
- [ ] 만료된 accessToken으로 접근 → 401 A002
