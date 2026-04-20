---
name: ic-feature-impl
description: "Interview Connect 기능 구현 스킬. 새 기능 구현, 엔티티 추가, API 개발, 서비스 구현 요청 시 반드시 이 스킬을 사용. 설계→TDD 테스트→구현→코드리뷰 전체 파이프라인을 실행한다. 예: '회원가입 API 만들어줘', 'Company 엔티티 구현해줘', '후기 CRUD 구현', 기존 기능 수정/보완/업데이트 요청도 이 스킬로 처리."
---

# IC Feature Implementation Skill

Interview Connect 기능 구현을 위한 TDD 기반 전체 파이프라인 스킬.

## 실행 모드: 에이전트 팀 (파이프라인 패턴)

## 에이전트 구성

| 팀원 | 에이전트 파일 | 역할 | 출력 |
|------|-------------|------|------|
| analyst | product-analyst | 비즈니스 룰 스펙화, Phase 범위 검증 | `_workspace/00_product_spec.md` |
| designer | domain-designer | 도메인 기술 설계 | `_workspace/01_domain_design.md` |
| tester | tdd-test-generator | TDD 테스트 코드 작성 | `_workspace/02_tdd_tests.md` |
| implementer | backend-implementer | Spring Boot 구현 | 소스 파일 직접 + `_workspace/03_impl_summary.md` |
| reviewer | code-reviewer | 컨벤션 + 비즈니스 룰 + 보안 검증 | `_workspace/04_review_report.md` |

## 워크플로우

### Phase 0: 컨텍스트 확인 + 재시작 지점 판별

1. `_workspace/PROGRESS.md` 존재 여부 확인:
   - **존재 + "이어서"/"계속"/"재개" 요청** → PROGRESS.md 읽어 마지막 `[x]` 단계 확인 → 다음 단계부터만 팀 구성
   - **존재 + 부분 수정 요청** → 해당 에이전트만 재호출, 기존 파일 덮어쓰기
   - **존재 + 새 기능 요청** → 기존 `_workspace/`를 `_workspace_{YYYYMMDD}/`로 이동 후 초기 실행
   - **미존재** → 초기 실행, PROGRESS.md 새로 생성

2. **신규 실행 시** `_workspace/PROGRESS.md` 생성:
   ```
   # PROGRESS — {태스크명}
   시작: {날짜}
   태스크: {요청 내용 한 줄 요약}

   ## 파이프라인 진행 상황
   - [ ] analyst — 00_product_spec.md
   - [ ] designer — 01_domain_design.md
   - [ ] tester — 02_tdd_tests.md
   - [ ] implementer — 03_impl_summary.md + 소스 파일
   - [ ] reviewer — 04_review_report.md

   ## 재시작 방법
   "이어서 진행해줘" 라고 입력하면 마지막 완료 단계 다음부터 자동 재시작.
   ```

### Phase 1: 요구사항 분석

1. 사용자 요청에서 구현할 기능 식별
2. `docs/phase1-tasks.md`, `docs/screen-flow.md` 참조하여 범위 확인
3. Phase 2 이후 기능인지 확인 (해당 시 사용자에게 알리고 중단)
4. `_workspace/` 생성 (초기 실행 시)
5. `_workspace/00_requirements.md`에 요구사항 정리

### Phase 2: 팀 구성

```
TeamCreate(
  team_name: "ic-feature-team",
  members: [
    { name: "analyst", agent_type: "product-analyst", model: "opus",
      prompt: "_workspace/00_requirements.md를 읽고 docs/screen-flow.md, docs/phase1-tasks.md를 참조하여 비즈니스 스펙을 정의하라.
               콘텐츠 접근 제어 규칙, 회원 등급 권한, Phase 범위, 엣지 케이스를 명확히 정의.
               완료 후 _workspace/00_product_spec.md에 저장하고 designer에게 SendMessage로 알려라." },
    { name: "designer", agent_type: "domain-designer", model: "opus",
      prompt: "analyst 알림을 받으면 _workspace/00_product_spec.md를 읽고 기술 설계를 시작하라.
               완료 후 _workspace/01_domain_design.md에 저장하고 tester에게 SendMessage로 알려라." },
    { name: "tester", agent_type: "tdd-test-generator", model: "opus",
      prompt: "designer 알림을 받으면 _workspace/00_product_spec.md + _workspace/01_domain_design.md를 읽고
               Red phase 테스트 코드를 작성하라. 비즈니스 룰 검증 테스트 반드시 포함.
               완료 후 _workspace/02_tdd_tests.md에 저장하고 implementer에게 SendMessage로 알려라." },
    { name: "implementer", agent_type: "backend-implementer", model: "opus",
      prompt: "tester 알림을 받으면 _workspace/00_product_spec.md + _workspace/01_domain_design.md + _workspace/02_tdd_tests.md를 읽고
               Green phase 구현을 시작하라. 실제 소스 파일 작성 + _workspace/03_impl_summary.md 저장 후 reviewer에게 SendMessage." },
    { name: "reviewer", agent_type: "code-reviewer", model: "opus",
      prompt: "implementer 알림을 받으면 _workspace/00_product_spec.md 기준으로 비즈니스 룰 정합성을 포함하여 리뷰하라.
               완료 후 _workspace/04_review_report.md에 저장하고 리더에게 SendMessage로 알려라." }
  ]
)
```

작업 등록:
```
TaskCreate(tasks: [
  { title: "비즈니스 스펙 정의", description: "콘텐츠 접근 제어, 권한 규칙, 엣지 케이스 명세화", assignee: "analyst" },
  { title: "도메인 설계", description: "엔티티/관계/서비스 기술 설계", assignee: "designer", depends_on: ["비즈니스 스펙 정의"] },
  { title: "TDD 테스트 작성", description: "Red phase: 비즈니스 룰 포함 실패 테스트", assignee: "tester", depends_on: ["도메인 설계"] },
  { title: "구현", description: "Green phase: 테스트 통과하는 최소 구현", assignee: "implementer", depends_on: ["TDD 테스트 작성"] },
  { title: "코드 리뷰", description: "컨벤션 + 비즈니스 룰 + 보안 검증", assignee: "reviewer", depends_on: ["구현"] }
])
```

### Phase 3: 파이프라인 실행

팀원들이 순차적으로 작업을 수행하며 SendMessage로 다음 단계를 트리거.

리더 모니터링:
- BLOCKER 발견 시: `reviewer`가 `implementer`에게 수정 요청 → 수정 후 재리뷰
- 최대 2회 BLOCKER 수정 루프 후에도 해결 안 되면 사용자에게 알림

### Phase 4: 결과 취합

1. 모든 팀원 작업 완료 대기
2. `_workspace/04_review_report.md` 읽어 최종 상태 확인
3. BLOCKER 없으면 완료 보고
4. 구현된 파일 목록 + 리뷰 결과 요약 사용자에게 보고

### Phase 5: 정리

1. TeamDelete로 팀 해체
2. `_workspace/` 보존 (감사 추적용)
3. 사용자에게 구현 완료 요약 보고

## 데이터 흐름

```
요구사항 → [analyst] → 00_product_spec.md
                              ↓
                       [designer] → 01_domain_design.md
                                          ↓
                                   [tester] → 02_tdd_tests.md
                                                    ↓
                                           [implementer] → 소스 파일 + 03_impl_summary.md
                                                                         ↓
                                                                 [reviewer] → 04_review_report.md
```

## 에러 핸들링

| 상황 | 전략 |
|------|------|
| 팀원 응답 없음 | 리더가 SendMessage로 상태 확인, 재시작 시도 |
| BLOCKER 반복 | 2회 후 사용자에게 수동 개입 요청 |
| Phase 범위 초과 | 사용자에게 알리고 Phase 1 범위로 제한 |

## 테스트 시나리오

### 정상 흐름
1. 사용자: "Member 엔티티와 회원가입 API 만들어줘"
2. designer → Member 엔티티 설계 (id, email, password, nickname, MemberRole)
3. tester → 이메일 중복, 닉네임 중복, 정상 가입 테스트 코드
4. implementer → Member.java, MemberRepository, MemberService, AuthController 구현
5. reviewer → 컨벤션 검증, BLOCKER 없음
6. 완료 보고

### 에러 흐름
1. reviewer가 Setter 사용 BLOCKER 발견
2. implementer에게 SendMessage로 수정 요청
3. implementer가 정적 팩터리 메서드로 교체
4. reviewer 재검증 → PASS
