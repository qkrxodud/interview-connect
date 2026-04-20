# Week 2 실행 계획 — 회사 + 후기 CRUD

`backend-implementer`, `tdd-test-agent`가 이 파일을 참조한다.
완료 항목은 `[x]`로 표시한다.

---

## 목표

Company + InterviewReview 엔티티를 완성하고, 비로그인도 접근 가능한 후기 목록/상세 API를 구현한다.
이 주차가 끝나면 "후기 목록 → 상세 → Q&A 블러 페이지"의 백엔드 기반이 완성되어야 한다.

---

## Task 2-1: 회사 엔티티 + API

**담당:** backend-implementer  
**선결 조건:** Week 1 완료

### 엔티티

- [x] `Company` 엔티티 — `Company.from(name, industry, logoUrl, website)`
- [x] `CompanyRepository` — `findTop10ByNameContainingIgnoreCase`, `findByName`, `existsByName`
- [x] `CompanyService` — `searchCompaniesByName`, `getCompanyById`, `createCompany`, `updateCompany`, `deleteCompany`
  - 수정 (2026-04-19): `validateDuplicateName` → `DUPLICATE_COMPANY_NAME` 에러코드, `deleteCompany` → `existsById` 사용

### API

- [x] `GET /api/v1/companies?q={keyword}` — 자동완성 (permitAll), 빈 keyword → 빈 배열
- [x] `POST /api/v1/companies` — 회사 등록 (ADMIN만, `@PreAuthorize`)
- [x] `PUT /api/v1/companies/{id}` — 회사 수정 (ADMIN만)
- [x] `DELETE /api/v1/companies/{id}` — 회사 삭제 (ADMIN만)

### 씨드 데이터

- [x] `DataInitializer` (`@Profile("local", "dev")`) — IT 회사 50개, ic-infra 모듈

### 테스트

- [x] `FakeCompanyRepository` — ic-api/test/fake/에 존재, Top10 제한 포함

---

## Task 2-2: 면접 후기 엔티티

**담당:** backend-implementer  
**선결 조건:** Task 2-1

### 엔티티

- [x] `InterviewResult` enum: `PASS`, `FAIL`, `PENDING`
- [x] `StringListConverter` — `List<String>` ↔ JSON 문자열 변환
- [x] `InterviewReview` 엔티티 — member/company ManyToOne LAZY, difficulty/atmosphere 1~5 검증, viewCount
- [x] `InterviewReviewRepository` — findByCompanyId, findByMemberId, findByAllFilters(JPQL)

### 테스트

- [x] `RefactoredInterviewReviewTest` — difficulty/atmosphere 범위 검증, changeContent, increaseViewCount
- [x] `StringListConverterTest` — 직렬화/역직렬화 검증
- [x] `FakeInterviewReviewRepository` — ic-domain/test/fake/에 존재

---

## Task 2-3: 후기 CRUD API

**담당:** backend-implementer  
**선결 조건:** Task 2-2

### API

- [x] `POST /api/v1/reviews` — 후기 작성 (인증 필요)
  - 수정 (2026-04-19): `member.canWriteReview()` 체크 → `REVIEW_PERMISSION_DENIED(403)` BusinessException
- [x] `GET /api/v1/reviews` — 후기 목록 (permitAll), companyId 필터 지원
- [x] `GET /api/v1/reviews/{id}` — 후기 상세 (permitAll), viewCount 증가
  - 수정 (2026-04-19): `REVIEW_NOT_FOUND` BusinessException
- [x] `PUT /api/v1/reviews/{id}` — 후기 수정, 작성자 확인 → `REVIEW_AUTHOR_MISMATCH(403)`
- [x] `DELETE /api/v1/reviews/{id}` — 후기 삭제, 204 반환
- [x] `GET /api/v1/reviews/me` — 내 후기 목록

### 통합 테스트

- [x] `ReviewControllerIntegrationTest` — ic-api/test/review/에 존재

---

## Week 2 완료 기준

- [x] 회사 검색 API 구현 (GET /api/v1/companies)
- [x] 후기 CRUD API 구현 (POST/GET/PUT/DELETE /api/v1/reviews)
- [x] ReviewService 권한 체크 businessException으로 수정 완료
- [ ] `./gradlew test` 전체 통과 확인 필요
- [ ] 비로그인 GET 요청 실제 동작 확인 필요
