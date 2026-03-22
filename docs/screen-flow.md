# Interview Connect - 화면 플로우 명세서 v1.1

## 콘텐츠 접근 전략

| 기능 | 비로그인 | 일반 회원 | 인증 회원 |
|------|----------|-----------|-----------|
| 후기 목록/상세 열람 | ✅ 전체 공개 | ✅ | ✅ |
| Q&A 질문 보기 | ✅ 공개 | ✅ | ✅ |
| **Q&A 답변 보기** | **🔒 블러 처리** | ✅ | ✅ |
| Q&A 질문 작성 | 🔒 | ✅ | ✅ |
| Q&A 답변 작성 | 🔒 | 🔒 | ✅ |
| 후기 작성 | 🔒 | 🔒 | ✅ |
| 1:1 쪽지 요청 | 🔒 | ✅ | ✅ |
| 1:1 쪽지 수락/대화 | 🔒 | 🔒 | ✅ |

## 핵심 전환 퍼널

```
검색/블로그 유입
  → 후기 목록 (비로그인, 전체 공개)
  → 후기 상세 (비로그인, 전체 공개)
  → Q&A 질문 확인 (공개)
  → Q&A 답변 블러 발견 ⭐ 전환 포인트
  → "로그인하면 답변을 볼 수 있어요" CTA
  → 회원가입
  → 로그인
  → 답변 확인 + 질문 작성
  → 면접 인증
  → 후기 작성 + 1:1 연결
```

---

## 화면 목록 (총 15개)

### Phase 1 — MVP (비로그인 공개 + 블러 전환)

#### 1. 랜딩 페이지 (`/`)
- 서비스 소개 + "면접 후기 둘러보기" CTA (비로그인 유입 유도)
- 상단: 로그인/회원가입 버튼
- 하단: 핵심 가치 카드 (후기 무료 열람, Q&A 연결, 인증된 답변)
- 이동: → 후기 목록 (비로그인), → 회원가입, → 로그인

#### 2. 후기 목록 - 비로그인 (`/reviews`)
- 비로그인 사용자도 전체 열람 가능 (permitAll)
- 상단: 로그인/회원가입 버튼 (하단 탭바 없음)
- 안내 배너: "📖 로그인 없이 후기를 자유롭게 둘러보세요!"
- 검색바 + 직무 필터 탭 (전체, 백엔드, 프론트엔드, 모바일, 데이터)
- 후기 카드: 회사명, 직무, 난이도, 결과, Q&A 개수, 날짜
- 이동: → 후기 상세 (비로그인)

#### 3. 후기 상세 - 비로그인 (`/reviews/:id` - 미인증)
- **핵심 전환 화면**
- 상단: 로그인 버튼 (1:1 쪽지 버튼 없음)
- 후기 내용 전체 공개: 작성자 정보, 난이도/분위기/결과, 면접 유형, 면접 질문, 자유 후기
- Q&A 섹션:
  - 질문(Q): 전체 공개
  - **답변(A): CSS blur(5px) 처리 + 반투명 오버레이**
  - 오버레이 CTA 버튼: "🔒 로그인하면 답변을 볼 수 있어요"
  - 하단 링크: "회원가입하고 12개 답변 모두 보기 →"
- 이동: → 회원가입 (블러 CTA 클릭), → 로그인

#### 4. 회원가입 (`/signup`)
- 입력: 이메일, 비밀번호, 비밀번호 확인, 닉네임
- 소셜 로그인: Kakao, Google (Phase 3에서 구현)
- 이동: → 로그인

#### 5. 로그인 (`/login`)
- 입력: 이메일, 비밀번호
- 이동: → 후기 목록 (로그인)

#### 6. 후기 목록 - 로그인 (`/reviews` - 인증)
- 로그인 상태 — 전체 기능 접근
- 상단: 알림 벨 + 프로필 아이콘
- 하단 탭바: 홈, 검색, 쪽지, MY
- 플로팅 + 버튼: 후기 작성
- 이동: → 후기 상세 (로그인), → 후기 작성, → 마이페이지, → 쪽지함

#### 7. 후기 상세 - 로그인 (`/reviews/:id` - 인증)
- 상단: 1:1 쪽지 버튼 활성화
- 후기 내용 전체 공개 (비로그인과 동일)
- Q&A 섹션:
  - **답변(A): 블러 없이 전체 공개 ✅**
  - "질문하기" 버튼 활성화
- 이동: → 질문 작성, → 1:1 대화 요청

#### 8. 질문 작성 (`/reviews/:id/questions/new`)
- 로그인 회원만 접근 가능
- 대상 후기 정보 표시
- 질문 입력 텍스트 영역
- 익명 옵션 체크박스
- 안내: "작성자와 같은 회사 인증 회원도 답변할 수 있습니다"
- 이동: → 후기 상세 (로그인)

#### 9. 후기 작성 (`/reviews/new`)
- 인증 회원(VERIFIED)만 작성 가능
- 입력 필드: 회사명(검색), 면접 날짜, 지원 직무, 면접 유형(멀티셀렉트), 난이도(1~5), 분위기(1~5), 면접 질문(동적 추가), 자유 후기, 결과
- 비인증 회원: 경고 배너 "⚠️ 인증 회원만 후기를 작성할 수 있습니다. 면접 인증하기 →"
- 이동: → 후기 목록, → 면접 인증

#### 10. 마이페이지 (`/my`)
- 프로필: 닉네임, 이메일, 회원 등급 배지
- 통계: 작성 후기 수, 받은 질문 수, 응답률
- 메뉴: 내가 쓴 후기, 내 Q&A 활동, 면접 인증 관리, 1:1 쪽지 설정, 알림 설정
- 하단 탭바
- 이동: → 후기 목록, → 면접 인증

### Phase 2 — 연결 (인증 + 1:1 쪽지)

#### 11. 면접 인증 (`/verification`)
- 인증 방식 선택:
  - 📧 면접 확인 이메일 (신뢰도 ★★★)
  - 📋 채용 플랫폼 지원 내역 (신뢰도 ★★★)
  - 📝 상세 후기 검증 (신뢰도 ★★)
  - 🏢 재직/퇴사 인증 (신뢰도 ★★★★)
- 파일 업로드 영역 (드래그 앤 드롭)
- 경고: "⚠️ 이름, 이메일 등 개인정보는 반드시 마스킹 후 업로드하세요"
- 이동: → 후기 작성, → 마이페이지

#### 12. 1:1 대화 요청 (`/chat-rooms/new`)
- 대상 작성자 정보: 프로필, 인증 상태, 응답률
- 메시지 입력: "공개 Q&A에서 물어보기 어려운 내용을 작성해주세요..."
- 안내: "ℹ️ 작성자가 수락해야 대화가 시작됩니다. 하루 최대 3건 요청 가능합니다."
- 이동: → 1:1 대화 (수락 시)

#### 13. 쪽지함 (`/chat-rooms`)
- 탭: 전체, 요청받음, 요청보냄
- 대화방 목록: 상대방 정보, 마지막 메시지, 시간, 읽지 않은 수
- 하단 탭바
- 이동: → 1:1 대화

#### 14. 1:1 대화 (`/chat-rooms/:id`)
- 채팅 UI (비동기 쪽지 형태)
- 상단: 상대방 정보 + 메뉴(신고/차단)
- 메시지: 말풍선 형태 (내 메시지 오른쪽, 상대 왼쪽)
- 대화 수락 안내 메시지
- 하단: 메시지 입력 + 전송 버튼
- 이동: → 쪽지함

---

## 화면 간 연결 (네비게이션 맵)

```
랜딩 ──→ 후기 목록(비로그인) ──→ 후기 상세(비로그인)
  │                                    │
  │                              답변 블러 CTA ⭐
  │                                    │
  ├──→ 회원가입 ←─────────────────────┘
  │       │
  │       ▼
  └──→ 로그인 ──→ 후기 목록(로그인) ──→ 후기 상세(로그인)
                       │                    │       │
                       │                    │       ├──→ 질문 작성
                       │                    │       │
                       │                    │       └──→ 1:1 대화 요청 ──→ 1:1 대화
                       │                    │
                       ├──→ 후기 작성 ──→ 면접 인증
                       │
                       ├──→ 쪽지함 ──→ 1:1 대화
                       │
                       └──→ 마이페이지 ──→ 면접 인증
```

---

## API 엔드포인트 매핑

| 화면 | API | 인증 |
|------|-----|------|
| 후기 목록 (비로그인) | `GET /api/v1/reviews` | permitAll |
| 후기 상세 (비로그인) | `GET /api/v1/reviews/:id` | permitAll |
| Q&A 조회 (비로그인) | `GET /api/v1/reviews/:id/qa` | permitAll (답변 blurred=true) |
| Q&A 조회 (로그인) | `GET /api/v1/reviews/:id/qa` | JWT (답변 전체 공개) |
| 회사 검색 | `GET /api/v1/companies?q=` | permitAll |
| 회원가입 | `POST /api/v1/auth/signup` | - |
| 로그인 | `POST /api/v1/auth/login` | - |
| 토큰 갱신 | `POST /api/v1/auth/refresh` | Refresh Token |
| 후기 작성 | `POST /api/v1/reviews` | VERIFIED |
| 질문 작성 | `POST /api/v1/reviews/:id/questions` | GENERAL+ |
| 답변 작성 | `POST /api/v1/reviews/:id/questions/:qid/answers` | VERIFIED |
| 인증 요청 | `POST /api/v1/verifications` | GENERAL+ |
| 대화 요청 | `POST /api/v1/chat-rooms` | GENERAL+ |
| 대화 수락 | `PATCH /api/v1/chat-rooms/:id/accept` | VERIFIED |
| 대화 거절 | `PATCH /api/v1/chat-rooms/:id/reject` | VERIFIED |
| 메시지 전송 | `POST /api/v1/chat-rooms/:id/messages` | GENERAL+ |
| 메시지 조회 | `GET /api/v1/chat-rooms/:id/messages` | GENERAL+ |
| 대화방 목록 | `GET /api/v1/chat-rooms` | GENERAL+ |
| 알림 조회 | `GET /api/v1/notifications` | GENERAL+ |

## 블러 처리 API 응답 스펙

```json
// GET /api/v1/reviews/:id/qa — 비로그인 응답
{
  "questions": [
    {
      "questionId": 1,
      "content": "코딩테스트 난이도가 백준 기준 몇 티어인가요?",
      "isAnonymous": false,
      "askerNickname": "구직자A",
      "createdAt": "2025-01-15T10:00:00",
      "answers": [
        {
          "answerId": 1,
          "content": null,
          "blurred": true,
          "preview": "골드 3~4 정도...",
          "answererNickname": null,
          "createdAt": "2025-01-15T12:00:00"
        }
      ]
    }
  ]
}

// GET /api/v1/reviews/:id/qa — 로그인 응답
{
  "questions": [
    {
      "questionId": 1,
      "content": "코딩테스트 난이도가 백준 기준 몇 티어인가요?",
      "isAnonymous": false,
      "askerNickname": "구직자A",
      "createdAt": "2025-01-15T10:00:00",
      "answers": [
        {
          "answerId": 1,
          "content": "골드 3~4 정도 느낌이었어요. DP랑 그래프 탐색이 나왔습니다.",
          "blurred": false,
          "preview": null,
          "answererNickname": "작성자",
          "createdAt": "2025-01-15T12:00:00"
        }
      ]
    }
  ]
}
```

## DB 엔티티 요약

```
Member (회원)
├── id, email, password, nickname
├── role (GENERAL / VERIFIED / ADMIN)
├── created_at, updated_at

Company (회사)
├── id, name, industry, logo_url

InterviewReview (면접 후기)
├── id, member_id (FK), company_id (FK)
├── interview_date, position, interview_types (JSON)
├── questions (JSON), difficulty, atmosphere
├── result (PASS/FAIL/PENDING), content
├── view_count, created_at

Verification (면접 인증)
├── id, member_id (FK), company_id (FK)
├── type, evidence_url
├── status (PENDING/APPROVED/REJECTED)
├── reviewed_at, reviewer_id

ReviewQuestion (Q&A 질문)
├── id, review_id (FK), asker_id (FK)
├── content, is_anonymous, created_at

ReviewAnswer (Q&A 답변)
├── id, question_id (FK), answerer_id (FK)
├── content, created_at

ChatRoom (대화방)
├── id, requester_id (FK), reviewer_id (FK), review_id (FK)
├── status (REQUESTED/ACCEPTED/REJECTED/CLOSED)
├── created_at

ChatMessage (메시지)
├── id, chat_room_id (FK), sender_id (FK)
├── content, is_read, created_at

ReviewerSetting (작성자 설정)
├── id, member_id (FK)
├── allow_direct_message (boolean)
├── daily_question_limit

Notification (알림)
├── id, member_id (FK), type, content
├── reference_id, is_read, created_at
```
