# 기술 부채 추적기

`code-reviewer`, `backend-implementer`가 이 파일을 참조한다.
새 부채 발견 시 이 파일에 추가하고, 해결 시 상태를 `해결됨`으로 변경한다.

---

## 우선순위 분류

| 레벨 | 의미 | 처리 시점 |
|------|------|-----------|
| P0 (긴급) | 보안/데이터 손실 위험 | 즉시 처리 |
| P1 (높음) | 기능 정확성에 영향 | 해당 주차 내 처리 |
| P2 (중간) | 성능/유지보수 영향 | 다음 주차 처리 |
| P3 (낮음) | 코드 품질 개선 | Phase 2 이전 정리 |

---

## 현재 부채 목록

### P1 — 높음

| ID | 발견일 | 내용 | 위치 | 상태 |
|----|--------|------|------|------|
| TD-001 | 2026-04 | viewCount Redis 최적화 미구현. 현재 조회마다 DB UPDATE → 고트래픽 시 병목 | `InterviewReviewService.incrementViewCount()` | 미해결 (Phase 2) |
| TD-002 | 2026-04 | CommentRepository 삭제 후 ic-infra에 AD 상태로 남음 (git status 확인) | `ic-infra/src/main/java/com/ic/infra/comment/` | 미해결 |

### P2 — 중간

| ID | 발견일 | 내용 | 위치 | 상태 |
|----|--------|------|------|------|
| TD-003 | 2026-04 | 후기 목록 조회 시 N+1 문제 가능성. `@ManyToOne(LAZY)` member, company 로딩 | `InterviewReviewRepository` | 미해결 (fetch join 또는 EntityGraph 필요) |
| TD-004 | 2026-04 | Q&A 조회 시 N+1: 질문마다 답변 목록 별도 쿼리 | `QaService.getQaList()` | 미해결 |

### P3 — 낮음

| ID | 발견일 | 내용 | 위치 | 상태 |
|----|--------|------|------|------|
| TD-005 | 2026-04 | DataInitializer가 로컬에서만 동작하지만 테스트 환경과 충돌 가능성 | `DataInitializer.java` | 미해결 |
| TD-006 | 2026-04 | 이메일 인증 코드 관련 DTO, 템플릿이 미완성 상태로 존재 | `auth/dto/EmailVerification*`, `templates/auth/verify-email.html` | 미해결 |

---

## 해결된 부채

| ID | 해결일 | 내용 | 해결 방법 |
|----|--------|------|-----------|
| - | - | - | - |

---

## Phase 2 이전 정리 대상

Phase 1이 완료되기 전, 다음 항목들을 반드시 해결한다:

- TD-002: 삭제된 CommentRepository 파일 완전 제거
- TD-006: 이메일 인증 미완성 파일 정리 (구현하거나 삭제)
- TD-003: 후기 목록 N+1 최소화 (fetch join 적용)

---

## 등록 규칙

새 부채 발견 시:
1. 적절한 우선순위(P0~P3) 결정
2. 간결한 내용 작성 (무엇이 문제인가)
3. 구체적인 파일/메서드 위치 명시
4. 상태: `미해결` / `해결됨` / `수용됨(의도적)`
