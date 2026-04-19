# 콘텐츠 접근 제어 스펙

`product-analyst`가 구현 전 이 스펙을 검증 기준으로 사용한다.
이 파일의 규칙을 변경하려면 `docs/design-docs/core-beliefs.md`의 원칙 3과 충돌하지 않는지 먼저 확인한다.

---

## 접근 제어 매트릭스

| 기능 | 비로그인 | GENERAL | VERIFIED | ADMIN |
|------|---------|---------|---------|-------|
| 후기 목록 조회 | ✅ 공개 | ✅ 공개 | ✅ 공개 | ✅ 공개 |
| 후기 상세 조회 | ✅ 공개 | ✅ 공개 | ✅ 공개 | ✅ 공개 |
| 후기 작성 | ❌ | ❌ | ✅ | ✅ |
| 후기 수정/삭제 | ❌ | ❌ (작성 불가) | ✅ (본인만) | ✅ |
| Q&A 질문 조회 | ✅ 공개 | ✅ 공개 | ✅ 공개 | ✅ 공개 |
| Q&A 답변 조회 | ✅ **블러** | ✅ 전체 공개 | ✅ 전체 공개 | ✅ 전체 공개 |
| Q&A 질문 작성 | ❌ | ✅ | ✅ | ✅ |
| Q&A 답변 작성 | ❌ | ❌ | ✅ | ✅ |
| 회사 검색 | ✅ 공개 | ✅ 공개 | ✅ 공개 | ✅ 공개 |
| 회사 등록 | ❌ | ❌ | ❌ | ✅ |
| 알림 조회 | ❌ | ✅ (본인) | ✅ (본인) | ✅ |

---

## 답변 블러 스펙 (핵심 전환 메커니즘)

### 비로그인 응답 (blurred=true)

```json
{
  "answerId": 1,
  "content": null,
  "blurred": true,
  "preview": "면접은 매우...",
  "answererNickname": null,
  "createdAt": "2026-02-01T10:00:00"
}
```

### 로그인 응답 (blurred=false)

```json
{
  "answerId": 1,
  "content": "면접은 매우 친절한 분위기였습니다. 기술 질문은 자료구조와...",
  "blurred": false,
  "preview": null,
  "answererNickname": "tester01",
  "createdAt": "2026-02-01T10:00:00"
}
```

### Preview 계산 규칙

```java
private String calculatePreview(String content) {
    if (content.length() > 10) {
        return content.substring(0, 10) + "...";
    }
    return content;  // 10자 이하: 그대로, "..." 추가 없음
}
```

### 구현 방법

SecurityContext에서 인증 여부를 확인하여 DTO 변환 시 분기한다:

```java
boolean isAuthenticated = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
    .filter(auth -> auth.isAuthenticated())
    .filter(auth -> !(auth instanceof AnonymousAuthenticationToken))
    .isPresent();

AnswerResponse response = isAuthenticated
    ? AnswerResponse.revealed(answer)
    : AnswerResponse.blurred(answer);
```

### 위반 사례 (절대 금지)

- 후기 본문을 블러 처리하는 것 → SEO 유입 차단, 전략에 반함
- 질문 내용을 블러 처리하는 것 → 전략에 반함
- 로그인한 GENERAL 회원에게 답변을 블러 처리하는 것 → 회원 이탈 유발

---

## Q&A 익명 처리 스펙

질문 작성 시 `isAnonymous: true`이면 질문자 닉네임을 "익명"으로 표시한다.

```json
{
  "questionId": 1,
  "content": "면접 분위기가 어떠셨나요?",
  "askerNickname": "익명",
  "isAnonymous": true,
  "createdAt": "2026-02-01T09:00:00",
  "answers": [...]
}
```

- `isAnonymous=false`이면 실제 닉네임 표시
- 익명 여부는 질문 작성 시에만 결정하고, 이후 변경 불가 (Phase 1 범위)

---

## SecurityConfig 매핑

```java
.authorizeHttpRequests(auth -> auth
    // 비로그인 허용 (반드시 유지)
    .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/v1/companies/**").permitAll()
    .requestMatchers("/api/v1/auth/**").permitAll()
    // Q&A 조회도 비로그인 허용 (블러는 서비스 레이어에서 처리)
    .requestMatchers(HttpMethod.GET, "/api/v1/reviews/*/qa").permitAll()
    // 나머지 인증 필요
    .anyRequest().authenticated()
)
```

**중요:** Q&A 조회 URL을 `authenticated()`로 막으면 안 된다. 블러 처리는 서비스 레이어에서 한다.

---

## 테스트 체크리스트 (product-analyst 검증용)

구현 후 다음 시나리오가 모두 통과해야 한다:

- [ ] 비로그인으로 GET /reviews → 200 (후기 목록 공개)
- [ ] 비로그인으로 GET /reviews/{id} → 200 (후기 상세 공개)
- [ ] 비로그인으로 GET /reviews/{id}/qa → 200, answers[0].blurred=true
- [ ] 비로그인으로 GET /reviews/{id}/qa → answers[0].content=null
- [ ] 비로그인으로 GET /reviews/{id}/qa → answers[0].preview 길이 ≤ 13 (10자 + "...")
- [ ] GENERAL으로 GET /reviews/{id}/qa → answers[0].blurred=false
- [ ] GENERAL으로 POST /reviews → 403 REVIEW_PERMISSION_DENIED
- [ ] GENERAL으로 POST /reviews/{id}/questions → 201 성공
- [ ] GENERAL으로 POST /reviews/{id}/questions/{qid}/answers → 403 ANSWER_PERMISSION_DENIED
- [ ] VERIFIED로 POST /reviews → 201 성공
- [ ] VERIFIED로 POST /reviews/{id}/questions/{qid}/answers → 201 성공
