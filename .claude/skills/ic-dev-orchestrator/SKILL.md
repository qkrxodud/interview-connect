---
name: ic-dev-orchestrator
description: "Interview Connect 개발 오케스트레이터. 기능 구현, 주간 태스크 실행, 코드 리뷰, TDD 테스트 작성 요청을 받아 적절한 에이전트 팀으로 조율한다. '구현해줘', 'Task 진행', '만들어줘', '추가해줘', '수정해줘', '보완해줘', '다시 실행', '재실행', '업데이트', '이전 결과 개선', '비즈니스 규칙 확인', '도메인 분석', '이어서', '이어서 진행해줘', '계속', '재개', '끊긴 곳부터', '어디까지 했어' 등 Interview Connect 개발 관련 모든 작업 요청 시 이 스킬을 사용."
---

# IC Dev Orchestrator

Interview Connect 개발 전체를 조율하는 마스터 오케스트레이터.

## 실행 모드: 하이브리드

| 단계 | 모드 | 이유 |
|------|------|------|
| 요청 분류 | 직접 처리 | 단순 라우팅 |
| 기능 구현/태스크 | 에이전트 팀 | 스펙→설계→테스트→구현→리뷰 파이프라인 |
| TDD만 요청 시 | 서브 에이전트 | 단일 역할, 팀 불필요 |
| 스펙 분석만 요청 시 | 서브 에이전트 | product-analyst 단독 |

## 요청 분류 및 라우팅

### 분류 기준

| 요청 유형 | 라우팅 | 담당 |
|----------|--------|------|
| 새 기능 구현 | 에이전트 팀 | 전체 파이프라인 |
| 주간 태스크 진행 | 에이전트 팀 | 전체 파이프라인 |
| 비즈니스 규칙 분석만 | 서브 에이전트 | product-analyst |
| TDD 테스트만 | 서브 에이전트 | tdd-test-generator |
| 코드 리뷰만 | 서브 에이전트 | code-reviewer |
| 도메인 설계만 | 서브 에이전트 | domain-designer |
| 단순 질문/조회 | 직접 응답 | — |

## 에이전트 구성 (전체 파이프라인)

| 팀원 | 에이전트 | 역할 | 출력 |
|------|--------|------|------|
| analyst | product-analyst | 비즈니스 룰 스펙화, Phase 범위 검증 | `_workspace/00_product_spec.md` |
| designer | domain-designer | 엔티티/관계/서비스 기술 설계 | `_workspace/01_domain_design.md` |
| tester | tdd-test-generator | Red phase 테스트 코드 | `_workspace/02_tdd_tests.md` |
| implementer | backend-implementer | Green phase 구현 | 소스 파일 + `_workspace/03_impl_summary.md` |
| reviewer | code-reviewer | 컨벤션 + 보안 + 비즈니스 룰 검증 | `_workspace/04_review_report.md` |

## 워크플로우

### Phase 0: 컨텍스트 확인 + 재시작 지점 판별

1. `_workspace/PROGRESS.md` 존재 여부 확인
   - **존재하면**: 파일을 읽어 마지막 완료 단계 확인 → 다음 단계부터 재시작
   - **없으면**: 신규 실행 또는 새 작업 판단으로 이동

2. 사용자 요청 유형 판단:
   - `"이어서"`, `"계속"`, `"재개"` 포함 → PROGRESS.md 기준으로 중단 지점부터 재시작
   - 후속 수정 요청 → 기존 `_workspace/` 활용, 해당 단계만 재실행
   - 새 작업 → 기존 `_workspace/`를 `_workspace_{YYYYMMDD}/`로 이동 후 진행

3. **신규 실행 시** `_workspace/PROGRESS.md` 초기화:
   ```markdown
   # PROGRESS — {태스크명}
   시작: {날짜}
   태스크: {사용자 요청 내용 요약}

   ## 파이프라인 진행 상황
   - [ ] analyst — 00_product_spec.md
   - [ ] designer — 01_domain_design.md
   - [ ] tester — 02_tdd_tests.md
   - [ ] implementer — 03_impl_summary.md + 소스 파일
   - [ ] reviewer — 04_review_report.md

   ## 재시작 방법
   "이어서 진행해줘" 라고 입력하면 마지막 완료 단계 다음부터 자동 재시작.
   ```

4. `docs/phase1-tasks.md`를 읽어 현재 Phase 진행 상황 파악

### Phase 1: 요청 분석

1. 사용자 요청 분류 (위 분류 기준 참조)
2. 관련 docs 파일 확인:
   - `docs/exec-plans/active/week*.md` — 주차별 상세 태스크 (완료 기준 포함)
   - `docs/product-specs/` — 기능별 비즈니스 스펙 (content-access-control, auth-flow, review-qa)
   - `docs/phase1-tasks.md` — 전체 태스크 체크리스트
   - `docs/screen-flow.md` — 화면/API 명세 (상세 스펙은 product-specs/ 우선)

### Phase 2: 실행

**기능 구현 / 태스크 실행 (에이전트 팀 — 5단계 파이프라인):**

```
TeamCreate(
  team_name: "ic-dev-team",
  members: [
    { name: "analyst", agent_type: "product-analyst", model: "opus",
      prompt: "[구현 요청 내용]. docs/screen-flow.md, docs/phase1-tasks.md를 읽고 비즈니스 스펙을 정의하라.
               콘텐츠 접근 제어, 회원 등급 권한, Phase 범위, 엣지 케이스를 _workspace/00_product_spec.md에 저장.
               완료 후 designer에게 SendMessage로 알려라." },
    { name: "designer", agent_type: "domain-designer", model: "opus",
      prompt: "analyst 알림 수신 후 _workspace/00_product_spec.md를 읽고 엔티티/관계/서비스 기술 설계 시작.
               완료 후 _workspace/01_domain_design.md 저장 후 tester에게 SendMessage." },
    { name: "tester", agent_type: "tdd-test-generator", model: "opus",
      prompt: "designer 알림 수신 후 _workspace/00_product_spec.md + _workspace/01_domain_design.md 읽고
               Red phase 테스트 코드 작성. 비즈니스 룰 검증 테스트 포함.
               완료 후 _workspace/02_tdd_tests.md 저장 후 implementer에게 SendMessage." },
    { name: "implementer", agent_type: "backend-implementer", model: "opus",
      prompt: "tester 알림 수신 후 _workspace/00_product_spec.md + _workspace/01_domain_design.md + _workspace/02_tdd_tests.md 읽고
               Green phase 구현. 소스 파일 직접 작성 + _workspace/03_impl_summary.md 저장 후 reviewer에게 SendMessage." },
    { name: "reviewer", agent_type: "code-reviewer", model: "opus",
      prompt: "implementer 알림 수신 후 _workspace/00_product_spec.md 기준으로 비즈니스 룰 정합성 + 컨벤션 + 보안 검증.
               완료 후 _workspace/04_review_report.md 저장 후 리더에게 SendMessage." }
  ]
)
```

작업 등록:
```
TaskCreate(tasks: [
  { title: "비즈니스 스펙 정의", assignee: "analyst" },
  { title: "도메인 설계", assignee: "designer", depends_on: ["비즈니스 스펙 정의"] },
  { title: "TDD 테스트 작성", assignee: "tester", depends_on: ["도메인 설계"] },
  { title: "구현", assignee: "implementer", depends_on: ["TDD 테스트 작성"] },
  { title: "코드 리뷰", assignee: "reviewer", depends_on: ["구현"] }
])
```

**단일 역할 (서브 에이전트):**

```
Agent(
  subagent_type: "{product-analyst | tdd-test-generator | code-reviewer | domain-designer}",
  model: "opus",
  prompt: "[구체적 작업 지시]"
)
```

### Phase 3: 모니터링

- BLOCKER 발견 시: reviewer → implementer SendMessage로 수정 요청, 최대 2회
- 비즈니스 룰 BLOCKER 발견 시: reviewer → analyst에게도 확인 요청 가능
- 2회 후 미해결: 사용자에게 보고 후 수동 개입 요청
- 태스크 완료 시: `docs/phase1-tasks.md` 체크리스트 업데이트

### Phase 4: 정리

1. TeamDelete (팀 모드 사용 시)
2. `_workspace/` 보존
3. 완료 요약 사용자에게 보고:
   - 비즈니스 스펙 요약 (접근 제어, 권한 규칙)
   - 구현된 파일 목록
   - 리뷰 결과 요약
   - 다음 권장 태스크

## 데이터 흐름

```
사용자 요청
    ↓
[오케스트레이터: 분류]
    ├── 기능/태스크 → 에이전트 팀 (5단계 파이프라인)
    │       analyst → designer → tester → implementer → reviewer
    │         (비즈니스)  (설계)    (테스트)    (구현)       (리뷰)
    │
    └── 단일 작업 → 서브 에이전트
```

## 에러 핸들링

| 상황 | 전략 |
|------|------|
| Phase 2 이후 기능 요청 | analyst가 감지 → 사용자에게 안내 후 중단 |
| 비즈니스 룰 모호성 | analyst가 명세 기준으로 판단, 불명확 시 사용자에게 확인 |
| 팀원 응답 없음 | SendMessage로 상태 확인, 재시작 |
| BLOCKER 2회 이상 | 사용자에게 수동 개입 요청 |
| 태스크 의존성 미충족 | 선행 태스크 완료 권고 |

## 테스트 시나리오

### 정상 흐름 — 비즈니스 룰 포함 구현
1. 사용자: "Task 3-2 Q&A API + 블러 처리 구현해줘"
2. Phase 0: _workspace/ 없음 → 초기 실행
3. Phase 2 팀 구성:
   - analyst → "VERIFIED만 답변 가능, 비로그인 시 content=null/blurred=true/preview=앞10자" 스펙화
   - designer → ReviewQuestion, ReviewAnswer 엔티티 설계
   - tester → 비로그인/로그인/VERIFIED별 응답 테스트
   - implementer → QaController, QaService, AnswerResponse.blurred()/revealed() 구현
   - reviewer → 블러 로직 비즈니스 룰 + 컨벤션 검증
4. Phase 4: 완료 보고 + 체크리스트 업데이트

### 에러 흐름 — 비즈니스 룰 BLOCKER
1. reviewer: "비로그인 시 answererNickname을 반환하고 있음 BLOCKER"
2. implementer에게 SendMessage: null 반환으로 수정
3. implementer 수정 후 재알림
4. reviewer 재검증 → PASS

### 후속 수정 흐름
1. 사용자: "이전 Q&A 구현에서 preview 길이를 10자에서 20자로 바꿔줘"
2. Phase 0: _workspace/ 존재 → 후속 작업
3. analyst에게 SendMessage: 스펙 수정 (preview substring 변경)
4. implementer에게만 SendMessage: AnswerResponse.blurred() 수정
5. reviewer 재검증
