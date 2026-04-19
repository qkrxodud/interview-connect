# DB Schema — Interview Connect (Phase 1)

`domain-designer`, `backend-implementer`가 엔티티 설계 시 이 파일을 참조한다.
코드 변경 시 이 파일도 함께 업데이트한다.

---

## Phase 1 테이블 (구현 대상)

### member
```sql
CREATE TABLE member (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,              -- BCrypt 해시
    nickname    VARCHAR(30)  NOT NULL UNIQUE,
    role        VARCHAR(20)  NOT NULL DEFAULT 'GENERAL',  -- GENERAL | VERIFIED | ADMIN
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL
);
```

### company
```sql
CREATE TABLE company (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    industry    VARCHAR(50),                         -- IT, 금융, 제조 등
    logo_url    VARCHAR(500),
    website     VARCHAR(500),
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL
);
```

### interview_review
```sql
CREATE TABLE interview_review (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT NOT NULL,
    company_id       BIGINT NOT NULL,
    interview_date   DATE,
    position         VARCHAR(50) NOT NULL,           -- 백엔드, 프론트엔드 등
    interview_types  TEXT,                           -- JSON 배열: ["코딩테스트","기술면접"]
    questions        TEXT,                           -- JSON 배열: ["질문1","질문2"]
    difficulty       INT NOT NULL,                   -- 1~5
    atmosphere       INT NOT NULL,                   -- 1~5
    result           VARCHAR(20) NOT NULL,           -- PASS | FAIL | PENDING
    content          TEXT,                           -- 자유 후기
    view_count       BIGINT NOT NULL DEFAULT 0,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member(id),
    FOREIGN KEY (company_id) REFERENCES company(id)
);
```

### review_question
```sql
CREATE TABLE review_question (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id    BIGINT NOT NULL,
    asker_id     BIGINT NOT NULL,
    content      TEXT NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    FOREIGN KEY (review_id) REFERENCES interview_review(id) ON DELETE CASCADE,
    FOREIGN KEY (asker_id) REFERENCES member(id)
);
```

### review_answer
```sql
CREATE TABLE review_answer (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id  BIGINT NOT NULL,
    answerer_id  BIGINT NOT NULL,
    content      TEXT NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    FOREIGN KEY (question_id) REFERENCES review_question(id) ON DELETE CASCADE,
    FOREIGN KEY (answerer_id) REFERENCES member(id)
);
```

### notification
```sql
CREATE TABLE notification (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT NOT NULL,
    type         VARCHAR(50) NOT NULL,   -- NEW_QUESTION | NEW_ANSWER | CHAT_REQUEST | VERIFICATION_COMPLETE
    content      VARCHAR(500),
    reference_id BIGINT,                -- 관련 엔티티 ID (questionId, answerId 등)
    is_read      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);
```

---

## Phase 2 테이블 (구현 금지 — Phase 1에서 생성하지 않음)

### verification (면접 인증)
```sql
-- Phase 2 구현
CREATE TABLE verification (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT NOT NULL,
    company_id   BIGINT NOT NULL,
    type         VARCHAR(50),         -- EMAIL | PLATFORM | REVIEW | EMPLOYMENT
    evidence_url VARCHAR(500),        -- S3 URL
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING | APPROVED | REJECTED
    reviewed_at  DATETIME(6),
    reviewer_id  BIGINT,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL
);
```

### chat_room, chat_message (1:1 쪽지)
```sql
-- Phase 2 구현
-- chat_room, chat_message, reviewer_setting 테이블
```

---

## 관계 다이어그램

```
member ──< interview_review >── company
  │
  └──< review_question ──< review_answer
  │
  └──< notification

[Phase 2]
member ──< verification >── company
member ──< chat_room (requester, reviewer)
chat_room ──< chat_message
```

---

## JPA 매핑 시 주의사항

- `interview_types`, `questions` 컬럼: `@Convert(converter = StringListConverter.class)` 사용
- Cascade: `review_question` 삭제 시 `review_answer` cascade delete
- `notification` 삭제: `member` 삭제 시 cascade delete
- FetchType: 모든 @ManyToOne은 `LAZY` (즉시 로딩 금지)
- `view_count`: 조회 시 Redis increment → 주기적 DB sync (단순 구현 시 DB 직접 UPDATE 허용)

---

## 씨드 데이터 (DataInitializer — @Profile("local"))

- company: IT 대기업/스타트업 50개 (카카오, 네이버, 토스, 라인, 쿠팡, 배민 등)
- interview_review: 샘플 후기 10~20건
- review_question: 후기당 3~5개
- review_answer: 질문당 1~2개
