# CLAUDE.md — Interview Connect

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