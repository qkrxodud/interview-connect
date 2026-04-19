# PLANS.md — Interview Connect 실행 계획

현재 진행 상황과 다음 우선순위를 추적한다.
상세 태스크는 `docs/exec-plans/active/`를 참조한다.

---

## 현재 상태 (2026-04-19)

**Phase 1 Week 1 진행 중**

| 모듈 | 완료 | 미완료 |
|------|------|-------|
| ic-common (공통) | ApiResponse, ErrorCode, BusinessException, BaseTimeEntity | - |
| ic-domain (Member) | Member, MemberRole, MemberRepository, MemberService | - |
| ic-infra (JWT/Redis) | JwtTokenProvider, JwtFilter, RefreshTokenRepository | - |
| ic-api (Auth) | SecurityConfig, AuthController | - |
| ic-domain (Review/Company/QA) | - | 전체 미구현 |
| ic-api (Review/QA/Notification) | - | 전체 미구현 |

---

## Phase 1 로드맵

### ✅ Week 1 — 프로젝트 초기화 + 회원 시스템 (진행 중)
- [x] ic-common 공통 모듈
- [x] Member 엔티티 + JWT 인증
- [x] AuthController (회원가입/로그인/갱신/로그아웃)
- 상세: `docs/exec-plans/active/week1.md`

### ⬜ Week 2 — 회사 + 후기 CRUD
- [ ] Company 엔티티 + 검색 API
- [ ] InterviewReview 엔티티 + CRUD
- 상세: `docs/exec-plans/active/week2.md`

### ⬜ Week 3 — 공개 Q&A + 답변 블러
- [ ] ReviewQuestion, ReviewAnswer 엔티티
- [ ] Q&A 조회 (블러 처리)
- [ ] Notification 기본
- 상세: `docs/exec-plans/active/week3.md`

### ⬜ Week 4 — 프론트엔드 + 씨드 데이터
- [ ] Bootstrap 기반 화면
- [ ] DataInitializer (회사 50개, 후기 20건)
- 상세: `docs/exec-plans/active/week4.md`

---

## 다음 구현 우선순위

1. **Task 2-1**: Company 엔티티 + `GET /api/v1/companies?q=` API
2. **Task 2-2**: InterviewReview 엔티티 + StringListConverter
3. **Task 2-3**: 후기 CRUD API (목록/상세 permitAll)

---

## 기술 부채

`docs/exec-plans/tech-debt-tracker.md` 참조
