# 후기 + Q&A 스펙

`product-analyst`, `backend-implementer`가 후기/Q&A 기능 구현 시 이 파일을 참조한다.
블러 상세 스펙은 `docs/product-specs/content-access-control.md` 참조.

---

## 후기 (InterviewReview)

### 필드 정의

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| companyId | Long | 필수 | 면접 본 회사 |
| interviewDate | LocalDate | 선택 | 면접 날짜 |
| position | String | 필수, 50자 | 지원 직무 |
| interviewTypes | List\<String\> | 필수, 최소 1개 | 면접 유형 |
| questions | List\<String\> | 선택 | 면접 질문 목록 |
| difficulty | int | 필수, 1~5 | 난이도 |
| atmosphere | int | 필수, 1~5 | 분위기 |
| result | InterviewResult | 필수 | PASS/FAIL/PENDING |
| content | String | 선택, TEXT | 자유 후기 |

### 유효한 interviewTypes 값

`"코딩테스트"`, `"기술면접"`, `"인성면접"`, `"실무면접"`, `"임원면접"`, `"PT면접"`, `"과제전형"`

클라이언트에서 목록을 제공하지만 서버에서 값 검증은 하지 않는다 (Phase 1 단순화).

### 후기 목록 API

```
GET /api/v1/reviews?companyId=1&position=백엔드&difficulty=4&result=PASS&page=0&size=10&sort=createdAt,desc
```

**응답 예시:**

```json
{
  "success": true,
  "data": {
    "totalPages": 5,
    "totalElements": 48,
    "page": 0,
    "size": 10,
    "content": [
      {
        "id": 1,
        "company": { "id": 1, "name": "카카오", "industry": "IT" },
        "position": "백엔드",
        "interviewDate": "2026-01-15",
        "difficulty": 4,
        "atmosphere": 3,
        "result": "PASS",
        "questionCount": 3,
        "viewCount": 120,
        "authorNickname": "tester01",
        "createdAt": "2026-02-01"
      }
    ]
  },
  "error": null
}
```

**정렬 옵션:** `createdAt,desc` (기본), `viewCount,desc`, `difficulty,asc`

### 후기 상세 API

```
GET /api/v1/reviews/{id}
```

**응답 예시:**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "company": { "id": 1, "name": "카카오", "industry": "IT", "logoUrl": null },
    "position": "백엔드",
    "interviewDate": "2026-01-15",
    "interviewTypes": ["코딩테스트", "기술면접"],
    "questions": ["알고리즘 문제를 풀어보세요", "JVM 메모리 구조를 설명해보세요"],
    "difficulty": 4,
    "atmosphere": 3,
    "result": "PASS",
    "content": "전반적으로 기술 깊이를 많이 보는 것 같았습니다...",
    "viewCount": 121,
    "questionCount": 3,
    "authorNickname": "tester01",
    "createdAt": "2026-02-01T10:00:00"
  },
  "error": null
}
```

### 조회수 처리

- 조회마다 DB 직접 UPDATE (`incrementViewCount()`)
- Redis 최적화는 Phase 2 기술 부채 TD-001에 등록됨

---

## Q&A

### Q&A 목록 조회 API

```
GET /api/v1/reviews/{reviewId}/qa
```

**비로그인 응답:**

```json
{
  "success": true,
  "data": {
    "reviewId": 1,
    "questions": [
      {
        "questionId": 1,
        "content": "면접 분위기가 어떠셨나요?",
        "askerNickname": "익명",
        "isAnonymous": true,
        "createdAt": "2026-02-05T14:00:00",
        "answers": [
          {
            "answerId": 1,
            "content": null,
            "blurred": true,
            "preview": "면접은 매우...",
            "answererNickname": null,
            "createdAt": "2026-02-05T15:00:00"
          }
        ]
      }
    ]
  },
  "error": null
}
```

**로그인 응답:** 동일 구조, `content`=전문, `blurred`=false, `preview`=null, `answererNickname`=닉네임

### 질문 작성 API

```
POST /api/v1/reviews/{reviewId}/qa/questions
Authorization: Bearer {accessToken}

{
  "content": "면접 준비를 어떻게 하셨나요?",
  "isAnonymous": false
}
```

**처리 후:**
- 질문 저장
- 후기 작성자에게 `NEW_QUESTION` 알림 생성:
  ```
  content: "{askerNickname}님이 {reviewTitle}에 질문을 남겼습니다."
  referenceId: questionId
  ```
- `isAnonymous=true`이면 알림의 askerNickname을 "익명"으로 표시

**응답:**

```json
{
  "success": true,
  "data": {
    "questionId": 5,
    "content": "면접 준비를 어떻게 하셨나요?",
    "askerNickname": "user02",
    "isAnonymous": false,
    "createdAt": "2026-02-10T10:00:00"
  },
  "error": null
}
```

### 답변 작성 API

```
POST /api/v1/reviews/{reviewId}/qa/questions/{questionId}/answers
Authorization: Bearer {accessToken}  ← VERIFIED 회원만

{
  "content": "저는 CS 기본기 위주로 준비했습니다. 특히 자료구조와..."
}
```

**VERIFIED 검증:** `currentMember.getRole().isVerified()` — 아니면 `ANSWER_PERMISSION_DENIED`(403)

**처리 후:**
- 답변 저장
- 질문 작성자에게 `NEW_ANSWER` 알림 생성:
  ```
  content: "{answererNickname}님이 회원님의 질문에 답변을 남겼습니다."
  referenceId: answerId
  ```

**응답:**

```json
{
  "success": true,
  "data": {
    "answerId": 3,
    "content": "저는 CS 기본기 위주로 준비했습니다...",
    "blurred": false,
    "preview": null,
    "answererNickname": "tester01",
    "createdAt": "2026-02-10T11:00:00"
  },
  "error": null
}
```

---

## 알림 (Notification)

### 알림 타입 (Phase 1에서 사용하는 것만)

| 타입 | 발생 시점 | 수신자 |
|------|---------|--------|
| `NEW_QUESTION` | 내 후기에 질문 작성 시 | 후기 작성자 |
| `NEW_ANSWER` | 내 질문에 답변 작성 시 | 질문 작성자 |

**Phase 2 예약 (구현 금지):** `CHAT_REQUEST`, `VERIFICATION_COMPLETE`

### 알림 목록 API

```
GET /api/v1/notifications
Authorization: Bearer {accessToken}
```

**응답:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "NEW_QUESTION",
      "content": "user02님이 카카오 백엔드 면접 후기에 질문을 남겼습니다.",
      "referenceId": 5,
      "isRead": false,
      "createdAt": "2026-02-10T10:00:00"
    }
  ],
  "error": null
}
```

- 최신순 정렬
- 읽지 않은 것 먼저 (정렬: `isRead ASC, createdAt DESC`)

### 읽음 처리 API

```
PATCH /api/v1/notifications/{id}/read
Authorization: Bearer {accessToken}
```

- 본인 알림만 처리 가능 (타인 알림 접근 시 → A004 FORBIDDEN)
- 이미 읽은 알림에 요청해도 200 반환 (멱등성)

---

## 테스트 체크리스트 (product-analyst 검증용)

**후기:**
- [ ] VERIFIED로 POST /reviews → 201, reviewId 반환
- [ ] GENERAL로 POST /reviews → 403 R002
- [ ] GET /reviews (비로그인) → 200, content 배열
- [ ] GET /reviews?companyId=1 → 해당 회사 후기만 반환
- [ ] GET /reviews/{id} → 200, viewCount 증가 확인
- [ ] PUT /reviews/{id} 타인 → 403
- [ ] DELETE /reviews/{id} 작성자 → 204

**Q&A:**
- [ ] GET /reviews/{id}/qa 비로그인 → blurred=true, content=null
- [ ] GET /reviews/{id}/qa 로그인 → blurred=false, content 전체
- [ ] POST /questions GENERAL → 201, 알림 생성 확인
- [ ] POST /questions 비로그인 → 401
- [ ] POST /answers VERIFIED → 201, 알림 생성 확인
- [ ] POST /answers GENERAL → 403 Q002

**알림:**
- [ ] 질문 작성 후 후기 작성자 알림 → NEW_QUESTION 1건
- [ ] 답변 작성 후 질문 작성자 알림 → NEW_ANSWER 1건
- [ ] PATCH /notifications/{id}/read → isRead=true
- [ ] 타인 알림 읽음 처리 → 403
