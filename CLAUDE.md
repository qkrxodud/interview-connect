# CLAUDE.md — Interview Connect

## 하네스: Interview Connect 개발

**목표:** TDD 기반 설계→테스트→구현→리뷰 파이프라인으로 Interview Connect 기능을 자동 구현

**트리거:** 기능 구현, 태스크 진행, 코드 작성 요청 시 `ic-dev-orchestrator` 스킬을 사용하라. 단순 질문이나 파일 읽기는 직접 응답 가능.

**변경 이력:**
| 날짜 | 변경 내용 | 대상 | 사유 |
|------|----------|------|------|
| 2026-04-19 | 초기 구성 | 전체 | 신규 하네스 구축 |
| 2026-04-19 | product-analyst 에이전트 추가 | agents/product-analyst.md, ic-dev-orchestrator, ic-feature-impl | 비즈니스 룰 검증 역할 부재 — 구현 전 콘텐츠 접근제어·권한 스펙화 필요 |
| 2026-04-19 | 참조 문서 .claude/로 이동 | AGENTS.md, ARCHITECTURE.md, SECURITY.md, QUALITY_SCORE.md, PRODUCT_SENSE.md, TDD_TEST_SUITE_SUMMARY.md | 에이전트 전용 문서를 .claude/ 폴더로 통합 |
| 2026-04-19 | 문서 구조 확장 | docs/exec-plans/active/, docs/exec-plans/tech-debt-tracker.md, docs/product-specs/ | 주차별 실행 계획·기술 부채·기능 스펙 분리로 에이전트 참조 정확도 향상 |
| 2026-04-19 | 파일 경로 오류 수정 | backend-implementer.md, code-reviewer.md | _workspace 파일 번호 불일치(02/03) BLOCKER 수정 |
| 2026-04-19 | ic-weekly-task product-analyst 누락 수정 | ic-weekly-task/SKILL.md | 주간 태스크에서도 비즈니스 룰 검증 필요 |
| 2026-04-19 | tdd-test-generator IC 컨텍스트 추가 | agents/tdd-test-generator.md | 팀 파이프라인 역할 명세 및 model opus로 통일 |
| 2026-04-19 | PROGRESS.md 체크포인트 시스템 추가 | ic-dev-orchestrator, ic-feature-impl, 전체 에이전트 | 토큰 소진으로 중단 시 "이어서 진행해줘"로 재시작 지원 |

---

## 프로젝트 개요

면접 경험자와 구직자를 연결하는 플랫폼. 잡플래닛과 달리 "사람 간 연결"이 핵심 차별점.
비로그인 사용자에게 후기를 공개하여 SEO 유입을 확보하고, Q&A 답변 블러로 회원가입을 유도한다.

## 기획 문서 위치

- `docs/screen-flow.md` — 화면 15개 상세 명세, API 엔드포인트, DB 엔티티, 블러 API 스펙
- `docs/action-plan.md` — 4단계 16주 로드맵, 주간 체크리스트
- `docs/phase1-tasks.md` — Phase 1 (Week 1~4) 세부 구현 태스크

## 현재 진행 상황

- [ ] Phase 1 Week 1 — 프로젝트 초기화 + 회원 시스템
- [ ] Phase 1 Week 2 — 회사 + 후기 CRUD
- [ ] Phase 1 Week 3 — 공개 Q&A + 답변 블러
- [ ] Phase 1 Week 4 — 프론트엔드 + 배포

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot 3.x + Java 17, Gradle |
| ORM | Spring Data JPA + QueryDSL |
| DB | MySQL 8.0 |
| Cache | Redis (세션, 조회수, 알림) |
| Frontend | Next.js 14 (App Router, TypeScript) |
| Infra | AWS EC2 + RDS + S3 |
| CI/CD | GitHub Actions |

## 프로젝트 구조

```
interview-connect/                    ← Spring Boot 멀티모듈 프로젝트 루트
├── CLAUDE.md
├── docs/
│   ├── screen-flow.md
│   ├── action-plan.md
│   └── phase1-tasks.md
├── build.gradle                      ← 멀티모듈 루트 설정
├── settings.gradle                   ← 서브모듈 포함 설정
├── gradlew                          ← Gradle Wrapper
├── ic-api/                          ← REST API 컨트롤러, Security, DTO
│   ├── build.gradle                 ← API 모듈 설정
│   └── src/main/java/com/ic/api/
│       ├── auth/                    ← AuthController, JWT 필터
│       ├── review/                  ← ReviewController
│       ├── qa/                      ← QaController
│       ├── chat/                    ← ChatController (Phase 2)
│       ├── company/                 ← CompanyController
│       └── config/                  ← SecurityConfig, WebConfig
├── ic-domain/                       ← 엔티티, Repository, 도메인 서비스
│   ├── build.gradle                 ← Domain 모듈 설정
│   └── src/main/java/com/ic/domain/
│       ├── member/                  ← Member, MemberRepository
│       ├── review/                  ← InterviewReview, ReviewRepository
│       ├── qa/                      ← ReviewQuestion, ReviewAnswer
│       ├── chat/                    ← ChatRoom, ChatMessage (Phase 2)
│       ├── company/                 ← Company, CompanyRepository
│       ├── verification/            ← Verification (Phase 2)
│       └── notification/            ← Notification
├── ic-infra/                        ← Redis, S3, 외부 연동
│   ├── build.gradle                 ← Infra 모듈 설정
│   └── src/main/java/com/ic/infra/
│       ├── redis/                   ← RedisConfig, TokenRepository
│       ├── s3/                      ← S3Service (Phase 2)
│       └── jwt/                     ← JwtTokenProvider, JwtFilter
├── ic-common/                       ← 공통 예외, 응답 래퍼, 상수
│   ├── build.gradle                 ← Common 모듈 설정
│   └── src/main/java/com/ic/common/
│       ├── exception/               ← BusinessException, ErrorCode
│       ├── response/                ← ApiResponse<T>
│       └── util/                    ← 유틸리티
└── frontend/                        ← Next.js (Phase 1 Week 4)
    ├── package.json
    └── src/app/
```

## 코딩 컨벤션

### Java / Spring Boot

- 패키지: `com.ic.{모듈}.{도메인}`
- 엔티티: `@Entity`, `@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`
- 엔티티에 Setter 금지 — 비즈니스 메서드로 상태 변경
- Repository: Spring Data JPA 인터페이스 + QueryDSL 커스텀
- Service: `@Service`, `@Transactional(readOnly = true)` 기본, 쓰기 메서드만 `@Transactional`
- Controller: `@RestController`, `@RequestMapping("/api/v1/{도메인}")`
- DTO: Request/Response 분리, record 사용 권장
- 응답: `ApiResponse<T>` 래퍼 통일 (`{ "success": true, "data": {...}, "error": null }`)
- 예외: `BusinessException` + `ErrorCode` enum + `@RestControllerAdvice` 전역 핸들러
- Validation: `@Valid` + Bean Validation 어노테이션
- 테스트: 서비스 레이어 단위 테스트 필수, 통합 테스트 선택

### API 설계

- RESTful 원칙 준수
- 비로그인 허용 API: `GET /api/v1/reviews`, `GET /api/v1/reviews/{id}`, `GET /api/v1/reviews/{id}/qa`, `GET /api/v1/companies`
- 인증 필요 API: POST/PATCH/DELETE 전체
- 페이징: `Pageable` 사용, `page`, `size`, `sort` 파라미터
- 필터: 쿼리 파라미터로 전달 (`?company=카카오&position=백엔드&difficulty=4`)

### 콘텐츠 접근 제어 (핵심)

```
비로그인: 후기 전체 공개 + Q&A 질문 공개 + 답변 블러 (content=null, blurred=true, preview=앞10자)
로그인:   후기 전체 + Q&A 전체 공개 + 질문 작성 가능
인증회원: 후기 작성 + Q&A 답변 작성 + 1:1 쪽지 수락
```

### Git

- 브랜치: `main` ← `develop` ← `feature/{기능명}`
- 커밋 메시지: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`
- PR 단위: 기능 단위 (예: `feature/member-auth`, `feature/review-crud`)

---

## 작업 지시 방법

Claude Code에게 작업을 지시할 때 다음과 같이 Phase/Week 단위로 요청:

```bash
# 예시
claude "docs/phase1-tasks.md를 참고해서 Week 1 Task 1 (프로젝트 초기화)를 진행해줘"
claude "Week 1 Task 3 (JWT 인증)을 구현해줘. SecurityConfig에서 GET /api/v1/reviews는 permitAll"
```

## 주의사항

- Phase 2 이후 기능(인증, 쪽지, 멘토링)은 Phase 1 완료 후 진행 — 미리 구현하지 않는다
- 엔티티 설계 시 Phase 2 확장을 고려하되, 코드는 Phase 1 범위만 작성
- 프론트엔드(Next.js)는 Week 4에서 시작 — Week 1~3은 백엔드에 집중
- 씨드 데이터: `data.sql` 또는 `DataInitializer`로 회사 50개 + 샘플 후기 준비