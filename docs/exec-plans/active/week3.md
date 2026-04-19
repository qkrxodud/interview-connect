# Week 3 실행 계획 — 공개 Q&A + 답변 블러

`backend-implementer`, `tdd-test-agent`, `product-analyst`가 이 파일을 참조한다.
완료 항목은 `[x]`로 표시한다.

---

## 목표

Q&A 시스템을 완성한다. **답변 블러는 이 서비스의 핵심 전환 메커니즘**이다.
비로그인 사용자는 Q&A 질문은 볼 수 있으나 답변은 preview만 보고, 로그인하면 전체를 본다.
잘못 구현하면 전환율이 0이 되므로 product-analyst가 스펙을 반드시 검증해야 한다.

---

## Task 3-1: Q&A 엔티티

**담당:** backend-implementer  
**선결 조건:** Week 2 완료

### ReviewQuestion 엔티티

- [ ] `ReviewQuestion`
  - `@ManyToOne(LAZY)` review, asker(Member)
  - `content` (TEXT, not null)
  - `isAnonymous` (boolean, default false)
  - Cascade: `review` 삭제 시 함께 삭제 (`ON DELETE CASCADE`)
  - 정적 팩터리: `ReviewQuestion.of(InterviewReview review, Member asker, String content, boolean isAnonymous)`

### ReviewAnswer 엔티티

- [ ] `ReviewAnswer`
  - `@ManyToOne(LAZY)` question, answerer(Member)
  - `content` (TEXT, not null)
  - Cascade: `question` 삭제 시 함께 삭제
  - 정적 팩터리: `ReviewAnswer.of(ReviewQuestion question, Member answerer, String content)`

### Repository

- [ ] `ReviewQuestionRepository`
  - `List<ReviewQuestion> findByReviewIdOrderByCreatedAtAsc(Long reviewId)`
- [ ] `ReviewAnswerRepository`
  - `List<ReviewAnswer> findByQuestionIdOrderByCreatedAtAsc(Long questionId)`

---

## Task 3-2: Q&A API + 블러 처리 (핵심)

**담당:** backend-implementer  
**선결 조건:** Task 3-1  
**검증:** product-analyst가 블러 스펙 준수 여부 확인

### 블러 스펙 (절대 변경 금지)

```
비로그인 답변 JSON:
{
  "answerId": 1,
  "content": null,
  "blurred": true,
  "preview": "면접은 매우...",   ← content 앞 10자 (10자 초과 시 "..." 추가)
  "answererNickname": null,
  "createdAt": "2026-02-01T10:00:00"
}

로그인 답변 JSON:
{
  "answerId": 1,
  "content": "면접은 매우 친절한 분위기였습니다. 기술 질문은...",
  "blurred": false,
  "preview": null,
  "answererNickname": "tester01",
  "createdAt": "2026-02-01T10:00:00"
}
```

**Preview 계산 규칙:**
- `content.length() > 10` → `content.substring(0, 10) + "..."`
- `content.length() <= 10` → `content` (있는 그대로, "..." 없음)

### API

- [ ] `GET /api/v1/reviews/{reviewId}/qa` — Q&A 조회 (permitAll)
  - SecurityContext 인증 여부 확인:
    ```java
    boolean isAuthenticated = SecurityContextHolder.getContext()
        .getAuthentication() != null
        && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
        && !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    ```
  - 비로그인: `AnswerResponse.blurred(answer)` 호출
  - 로그인: `AnswerResponse.revealed(answer)` 호출
  - Response:
    ```json
    {
      "questions": [
        {
          "questionId": 1,
          "content": "기술 면접 질문이 어떻게 나왔나요?",
          "askerNickname": "익명",   // isAnonymous=true 이면 "익명", false 이면 실제 닉네임
          "isAnonymous": true,
          "createdAt": "...",
          "answers": [ { ...AnswerResponse... } ]
        }
      ]
    }
    ```

- [ ] `POST /api/v1/reviews/{reviewId}/questions` — 질문 작성 (로그인 필요)
  - Request: `{ content, isAnonymous }`
  - 작성 후 → 후기 작성자에게 `NEW_QUESTION` 알림 생성
  - Response: `QuestionResponse` (questionId, content, ...)

- [ ] `POST /api/v1/reviews/{reviewId}/questions/{questionId}/answers` — 답변 작성 (VERIFIED만)
  - role.isVerified() 체크 → 아니면 ANSWER_PERMISSION_DENIED(403)
  - 작성 후 → 질문 작성자에게 `NEW_ANSWER` 알림 생성
  - Response: `AnswerResponse` (revealed 형태)

### 테스트

```
FakeRepository 사용 단위 테스트:
- 비로그인 조회 → answers[].blurred=true, content=null
- 비로그인 조회 → preview = content 앞 10자 + "..."
- 로그인 조회 → answers[].blurred=false, content 전체
- isAnonymous=true → askerNickname="익명"
- isAnonymous=false → askerNickname=실제 닉네임

통합 테스트:
- GET /reviews/{id}/qa (비로그인) → blurred=true 확인
- GET /reviews/{id}/qa (로그인) → blurred=false 확인
- POST /questions (비로그인) → 401
- POST /answers (GENERAL 회원) → 403 ANSWER_PERMISSION_DENIED
- POST /answers (VERIFIED 회원) → 201 성공
```

---

## Task 3-3: 알림 기본

**담당:** backend-implementer  
**선결 조건:** Task 3-2

### Notification 엔티티

- [ ] `NotificationType` enum: `NEW_QUESTION`, `NEW_ANSWER`, `CHAT_REQUEST`, `VERIFICATION_COMPLETE`
  - `CHAT_REQUEST`, `VERIFICATION_COMPLETE`는 Phase 2용 — 코드만 선언, 사용하지 않음
- [ ] `Notification` 엔티티
  - `@ManyToOne(LAZY)` member
  - `type` (enum)
  - `content` (VARCHAR 500)
  - `referenceId` (Long, 관련 엔티티 ID)
  - `isRead` (boolean, default false)
  - `ON DELETE CASCADE` (member 삭제 시)
  - 정적 팩터리: `Notification.newQuestion(Member receiver, Long questionId, String reviewTitle)`
  - 변경 메서드: `markAsRead()`
- [ ] `NotificationRepository`

### API

- [ ] `GET /api/v1/notifications` — 내 알림 목록 (로그인 필요)
  - 최신순 정렬, `isRead=false` 먼저 표시
  - Response: `List<NotificationResponse>` (id, type, content, referenceId, isRead, createdAt)
- [ ] `PATCH /api/v1/notifications/{id}/read` — 읽음 처리
  - 본인 알림만 처리 가능

---

## Week 3 완료 기준

- [ ] Q&A 블러 API: 비로그인 → blurred=true, 로그인 → blurred=false 확인
- [ ] 질문 작성 → 알림 생성 → 알림 조회 플로우 동작
- [ ] 답변 작성 → 알림 생성 → 알림 조회 플로우 동작
- [ ] `./gradlew test` 전체 통과, 커버리지 90%+
- [ ] product-analyst 블러 스펙 검증 통과
