---
name: ic-weekly-task
description: "Interview Connect 주간 태스크 실행 스킬. 'Week 1 Task 3 진행해줘', 'Phase 1 Week 2 작업', 'Task 2-1 구현', 'phase1-tasks.md 태스크' 등의 표현 시 반드시 이 스킬을 사용. docs/phase1-tasks.md의 체크리스트 항목을 순서대로 구현한다. 태스크 재실행, 특정 태스크만 다시, 진행 상황 확인도 이 스킬로 처리."
---

# IC Weekly Task Skill

`docs/phase1-tasks.md`의 체크리스트 태스크를 실행하는 스킬.

## 실행 모드: 에이전트 팀 (파이프라인 패턴)

ic-feature-impl 스킬과 동일한 팀 구조를 사용하되, 태스크 범위를 phase1-tasks.md 기준으로 정의한다.

## 워크플로우

### Phase 0: 태스크 확인

1. `docs/phase1-tasks.md` 읽기
2. 사용자가 요청한 Week/Task 번호 확인
3. `_workspace/` 존재 여부 확인 → 이전 진행 상황 파악
4. 해당 태스크의 체크리스트 항목 목록 추출

### Phase 1: 범위 정의

1. `_workspace/00_task_scope.md`에 다음 내용 작성:
   - 태스크 ID (예: Task 1-3)
   - 구현할 체크리스트 항목 전체 목록
   - 관련 모듈 (ic-api, ic-domain, ic-infra, ic-common 중 해당)
   - 의존하는 이전 태스크 완료 여부 확인

### Phase 2: 팀 구성 및 실행

`ic-feature-impl` 스킬과 동일한 팀 구성(analyst → designer → tester → implementer → reviewer).

팀 프롬프트에 태스크 범위 명시:
- `_workspace/00_task_scope.md`와 `docs/exec-plans/active/` 해당 주차 파일, `docs/phase1-tasks.md` 섹션을 참조
- 체크리스트 항목을 모두 구현할 것
- analyst는 `docs/product-specs/`에서 해당 기능 스펙을 먼저 확인한다

### Phase 3: 체크리스트 완료 처리

구현 완료 후:
1. `docs/phase1-tasks.md`의 해당 태스크 체크리스트를 `[x]`로 업데이트
2. 완료된 항목 목록을 사용자에게 보고

## 에러 핸들링

- 이전 태스크 미완료로 의존성 충돌 시: 사용자에게 알리고 선행 태스크 완료 후 진행 권고
- 체크리스트 항목 중 Phase 2 이후 기능 포함 시: 해당 항목 건너뛰고 나머지만 구현

## 테스트 시나리오

### 정상 흐름
1. 사용자: "Task 1-3 (회원 엔티티) 진행해줘"
2. docs/phase1-tasks.md에서 Task 1-3 항목 파악
3. designer → Member 엔티티 설계
4. tester → MemberService 단위 테스트 작성
5. implementer → Member.java, MemberRepository, MemberService 구현
6. reviewer → 컨벤션 검증
7. 체크리스트 [x] 업데이트 + 완료 보고
