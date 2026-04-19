# 제품 스펙 인덱스

`product-analyst`가 기능 구현 전 이 인덱스에서 관련 스펙 파일을 찾는다.
`domain-designer`, `backend-implementer`는 설계/구현 전 해당 스펙 파일을 반드시 읽는다.

---

## 스펙 파일 목록

| 파일 | 다루는 기능 | 담당 에이전트 |
|------|-----------|--------------|
| [content-access-control.md](content-access-control.md) | 비로그인/일반/인증 회원의 콘텐츠 접근 규칙, 블러 스펙 | product-analyst, backend-implementer |
| [auth-flow.md](auth-flow.md) | 회원가입, 로그인, 토큰 갱신, 로그아웃 플로우 | product-analyst, backend-implementer |
| [review-qa.md](review-qa.md) | 후기 CRUD, Q&A 작성/조회, 알림 생성 규칙 | product-analyst, backend-implementer |

---

## 핵심 원칙 (모든 스펙에 적용)

1. **비로그인 공개 최대화** — 후기 목록/상세/Q&A 질문은 항상 공개. SEO 유입을 막지 않는다.
2. **블러는 Q&A 답변에만** — 다른 콘텐츠를 블러 처리하는 구현은 전략에 반한다.
3. **VERIFIED만 후기/답변 작성** — GENERAL은 질문만 가능. Phase 1에서는 VERIFIED 수동 부여.
4. **Phase 2 기능 금지** — Verification, ChatRoom, S3, 소셜 로그인은 Phase 1에서 구현하지 않는다.

---

## 에러 코드 빠른 참조

| 코드 | HTTP | 의미 |
|------|------|------|
| A001 | 401 | INVALID_CREDENTIALS — 이메일/비밀번호 불일치 |
| A002 | 401 | EXPIRED_TOKEN — 토큰 만료 |
| A003 | 401 | UNAUTHORIZED — 로그인 필요 |
| A004 | 403 | FORBIDDEN — 권한 없음 (일반적) |
| M001 | 409 | DUPLICATE_EMAIL |
| M002 | 404 | MEMBER_NOT_FOUND |
| M003 | 409 | DUPLICATE_NICKNAME |
| R001 | 404 | REVIEW_NOT_FOUND |
| R002 | 403 | REVIEW_PERMISSION_DENIED — VERIFIED 필요 |
| C001 | 404 | COMPANY_NOT_FOUND |
| Q001 | 404 | QUESTION_NOT_FOUND |
| Q002 | 403 | ANSWER_PERMISSION_DENIED — VERIFIED 필요 |
| G001 | 400 | INVALID_INPUT — Bean Validation 실패 |
| G002 | 500 | INTERNAL_ERROR |
