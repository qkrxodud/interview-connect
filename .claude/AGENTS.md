# AGENTS.md — Interview Connect 하네스 에이전트 가이드

새 세션에서 작업할 때 이 파일을 먼저 읽어 어떤 에이전트를 사용할지 결정한다.
모든 개발 작업은 `ic-dev-orchestrator` 스킬을 통해 에이전트 팀이 실행한다.

---

## 에이전트 팀 구성 (파이프라인 순서)

```
product-analyst → domain-designer → tdd-test-generator → backend-implementer → code-reviewer
   [비즈니스 룰]    [기술 설계]         [테스트 작성]          [구현]                [검증]
```

---

## 에이전트별 역할 및 주요 참조 문서

### 1. product-analyst
**역할:** 구현 전 비즈니스 규칙 명세화, Phase 범위 검증  
**참조:** `docs/product-specs/`, `docs/screen-flow.md`, `.claude/PRODUCT_SENSE.md`  
**출력:** `_workspace/00_product_spec.md`  
**핵심 지식:** 콘텐츠 접근 제어 3단계, 블러 처리 스펙, 에러 코드 매핑

### 2. domain-designer
**역할:** 엔티티, 관계, 서비스 인터페이스 기술 설계  
**참조:** `.claude/ARCHITECTURE.md`, `docs/generated/db-schema.md`, `_workspace/00_product_spec.md`  
**출력:** `_workspace/01_domain_design.md`  
**핵심 지식:** DDD 원칙, 멀티모듈 의존성 방향, JPA 관계 매핑

### 3. tdd-test-generator
**역할:** Red phase — 실패하는 테스트 코드 작성  
**참조:** `.claude/QUALITY_SCORE.md`, `docs/references/spring-conventions-llms.txt`, `_workspace/00_product_spec.md`  
**출력:** `_workspace/02_tdd_tests.md`  
**핵심 지식:** Fake-first 전략, given/when/then 구조, @DisplayName 한글

### 4. tdd-test-agent
**역할:** Spring Boot 테스트 전략 (WebClient 통합 테스트)  
**참조:** `.claude/QUALITY_SCORE.md`, `.claude/TDD_TEST_SUITE_SUMMARY.md`  
**출력:** 직접 테스트 파일 작성  
**핵심 지식:** BaseApiWebClientTest, 단일 Spring 컨텍스트, Fake 리셋

### 5. backend-implementer
**역할:** Green phase — 테스트 통과하는 최소 구현  
**참조:** `.claude/ARCHITECTURE.md`, `docs/references/spring-conventions-llms.txt`, `.claude/SECURITY.md`, `docs/generated/db-schema.md`  
**출력:** 실제 Java 소스 파일 + `_workspace/03_impl_summary.md`  
**핵심 지식:** 정적 팩터리 메서드, 얼리 리턴, @Transactional 전략

### 6. code-reviewer
**역할:** 컨벤션 + 비즈니스 룰 + 보안 검증  
**참조:** `.claude/SECURITY.md`, `.claude/QUALITY_SCORE.md`, `docs/references/spring-conventions-llms.txt`, `_workspace/00_product_spec.md`  
**출력:** `_workspace/04_review_report.md`  
**핵심 지식:** BLOCKER vs SUGGESTION 구분, 비즈니스 룰 정합성, N+1 탐지

---

## 스킬 목록

| 스킬 | 트리거 | 실행 모드 |
|------|--------|----------|
| `ic-dev-orchestrator` | 개발 관련 모든 요청 | 하이브리드 |
| `ic-feature-impl` | 기능 구현 요청 | 에이전트 팀 |
| `ic-weekly-task` | Task N-N 진행 요청 | 에이전트 팀 |

---

## 작업 유형별 빠른 가이드

| 요청 예시 | 사용할 스킬 | 에이전트 팀 |
|----------|-----------|-----------|
| "Task 2-1 Company 엔티티 만들어줘" | ic-weekly-task | 전체 팀 |
| "InterviewReview 후기 목록 API 구현" | ic-feature-impl | 전체 팀 |
| "블러 처리 비즈니스 규칙 확인해줘" | ic-dev-orchestrator | product-analyst 단독 |
| "AuthService 테스트 코드 작성해줘" | ic-dev-orchestrator | tdd-test-generator 단독 |
| "Q&A 컨트롤러 코드 리뷰해줘" | ic-dev-orchestrator | code-reviewer 단독 |

---

## 데이터 흐름 (`_workspace/`)

```
00_product_spec.md   ← product-analyst 작성
01_domain_design.md  ← domain-designer 작성 (00 참조)
02_tdd_tests.md      ← tdd-test-generator 작성 (00, 01 참조)
03_impl_summary.md   ← backend-implementer 작성 (01, 02 참조)
04_review_report.md  ← code-reviewer 작성 (00, 03 참조)
```

후속 작업 시 `_workspace/`가 존재하면 해당 에이전트만 재호출한다.
