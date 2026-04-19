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

- [ ] `Company` 엔티티
  - `name` (unique, not null), `industry`, `logoUrl`, `website`
  - 정적 팩터리: `Company.of(String name, String industry)`
- [ ] `CompanyRepository`
  - `List<Company> findTop10ByNameContainingIgnoreCase(String keyword)`
  - `Optional<Company> findByName(String name)`
- [ ] `CompanyService`
  - `search(String keyword)` — 이름 부분 일치, 최대 10건
  - `findById(Long id)` — 없으면 COMPANY_NOT_FOUND

### API

- [ ] `GET /api/v1/companies?q={keyword}` — 자동완성 (permitAll)
  - 빈 keyword → 빈 배열 반환 (에러 아님)
  - Response: `List<CompanyResponse>` (id, name, industry, logoUrl)
- [ ] `POST /api/v1/companies` — 회사 등록 (ADMIN만)
  - Request: `{ name, industry, logoUrl, website }`

### 씨드 데이터

- [ ] `DataInitializer` (`@Profile("local")`) — IT 회사 50개
  - 카카오, 네이버, 토스, 라인, 쿠팡, 배민, 카카오뱅크, 하이퍼커넥트, 당근마켓, 직방 등
  - 각 회사: name, industry("IT"), logoUrl(null 허용)

### 테스트

```
FakeCompanyRepository 사용:
- 키워드 "카카오" → 카카오, 카카오뱅크 2건 반환
- 빈 키워드 → 빈 배열
- 존재하지 않는 회사 조회 → COMPANY_NOT_FOUND
```

---

## Task 2-2: 면접 후기 엔티티

**담당:** backend-implementer  
**선결 조건:** Task 2-1

### 엔티티

- [ ] `InterviewResult` enum: `PASS`, `FAIL`, `PENDING`
- [ ] `StringListConverter` — `List<String>` ↔ JSON 문자열 변환
  ```java
  @Converter
  public class StringListConverter implements AttributeConverter<List<String>, String> {
      // ObjectMapper로 직렬화/역직렬화
  }
  ```
- [ ] `InterviewReview` 엔티티
  - `@ManyToOne(fetch = LAZY)` member, company
  - `interviewDate` (LocalDate, nullable)
  - `position` (not null, 50자)
  - `interviewTypes` (`@Convert(StringListConverter)`)
  - `questions` (`@Convert(StringListConverter)`)
  - `difficulty`, `atmosphere` (1~5, int)
  - `result` (`@Enumerated(STRING)`)
  - `content` (TEXT, nullable)
  - `viewCount` (long, default 0)
  - 정적 팩터리: `InterviewReview.create(Member, Company, InterviewReviewRequest)`
  - 변경 메서드: `changeContent(...)`, `incrementViewCount()`
- [ ] `InterviewReviewRepository`
  - `Page<InterviewReview> findAllWithFilters(...)` — QueryDSL 필요 시 추가

### 테스트

```
InterviewReview 도메인 단위 테스트:
- difficulty 범위 1~5 외 값 → 예외 (도메인에서 검증)
- atmosphere 범위 1~5 외 값 → 예외
- StringListConverter 직렬화/역직렬화 정확성
```

---

## Task 2-3: 후기 CRUD API

**담당:** backend-implementer  
**선결 조건:** Task 2-2

### API

- [ ] `POST /api/v1/reviews` — 후기 작성 (VERIFIED 회원만)
  - `@AuthMember`로 현재 회원 주입
  - role.isVerified() 체크 → 아니면 REVIEW_PERMISSION_DENIED(403)
  - companyId로 Company 조회 → 없으면 COMPANY_NOT_FOUND(404)
  - Request: `{ companyId, interviewDate, position, interviewTypes, questions, difficulty, atmosphere, result, content }`
  - Response: `ApiResponse<ReviewCreateResponse>` (reviewId)

- [ ] `GET /api/v1/reviews` — 후기 목록 (permitAll)
  - 쿼리 파라미터: `companyId`, `position`, `difficulty`, `result`, `page`(0-based), `size`(기본 10), `sort`(기본 createdAt,desc)
  - Response: Page 래핑
    ```json
    {
      "totalPages": 5,
      "totalElements": 48,
      "content": [
        { "id": 1, "company": {"id": 1, "name": "카카오"}, "position": "백엔드",
          "difficulty": 4, "atmosphere": 3, "result": "PASS",
          "questionCount": 3, "viewCount": 120, "createdAt": "2026-02-01" }
      ]
    }
    ```

- [ ] `GET /api/v1/reviews/{id}` — 후기 상세 (permitAll)
  - 조회 시 viewCount 증가 (DB 직접 UPDATE, Redis 최적화는 Phase 2)
  - 작성자 닉네임 포함, Q&A 개수 포함
  - 없으면 REVIEW_NOT_FOUND(404)

- [ ] `PUT /api/v1/reviews/{id}` — 후기 수정 (작성자만)
  - 본인 확인: `review.getMember().getId() != currentMember.getId()` → FORBIDDEN(403)

- [ ] `DELETE /api/v1/reviews/{id}` — 후기 삭제 (작성자만)

### 통합 테스트

```
- POST /reviews (VERIFIED 회원) → 201 성공
- POST /reviews (GENERAL 회원) → 403 REVIEW_PERMISSION_DENIED
- POST /reviews (비로그인) → 401
- GET /reviews (비로그인) → 200 공개
- GET /reviews/{id} (비로그인) → 200 공개, viewCount+1
- PUT /reviews/{id} (타인) → 403
- DELETE /reviews/{id} (작성자) → 204
```

---

## Week 2 완료 기준

- [ ] 회사 검색 API 동작 확인 (curl or Postman)
- [ ] 후기 작성 → 목록 → 상세 조회 플로우 동작
- [ ] 비로그인 GET 요청 정상 응답 확인
- [ ] `./gradlew test` 전체 통과, 커버리지 90%+
