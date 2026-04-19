# IC 도메인 용어 사전

에이전트가 비즈니스 언어를 코드로 정확히 변환하기 위한 용어 정의.

---

## 회원 (Member)

| 용어 | 코드 | 설명 |
|------|------|------|
| 비로그인 | SecurityContext에 인증 없음 | JWT 토큰 없이 접근하는 사용자 |
| 일반 회원 | `MemberRole.GENERAL` | 회원가입 직후 기본 등급 |
| 인증 회원 | `MemberRole.VERIFIED` | 면접 경험 인증 완료 (Phase 2에서 구현) |
| 관리자 | `MemberRole.ADMIN` | 시스템 관리자 |
| 회원가입 | signup | 이메일 + 비밀번호 + 닉네임으로 계정 생성 |
| 로그인 | login | 이메일 + 비밀번호로 JWT 발급 |
| 토큰 갱신 | refresh | Refresh Token으로 새 Access Token 발급 |

## 후기 (InterviewReview)

| 용어 | 코드 | 설명 |
|------|------|------|
| 면접 후기 | `InterviewReview` | 면접 경험을 기록한 콘텐츠 |
| 직무 | `position` | 지원한 포지션 (백엔드, 프론트엔드 등) |
| 면접 유형 | `interviewTypes` | 코딩테스트, 기술면접, 인성면접 등 복수 선택 |
| 난이도 | `difficulty` | 1~5 점수 |
| 분위기 | `atmosphere` | 1~5 점수 |
| 면접 결과 | `result` | PASS / FAIL / PENDING |
| 자유 후기 | `content` | 텍스트 자유 형식 후기 |
| 조회수 | `viewCount` | 상세 페이지 조회 횟수 |

## Q&A

| 용어 | 코드 | 설명 |
|------|------|------|
| 질문 | `ReviewQuestion` | 구직자가 후기 작성자에게 하는 질문 |
| 답변 | `ReviewAnswer` | 인증 회원이 질문에 달아주는 답변 |
| 블러 | `blurred=true` | 비로그인 사용자에게 답변 내용을 숨기는 처리 |
| 미리보기 | `preview` | 블러 상태에서 보이는 답변 앞 10자 |
| 익명 | `isAnonymous=true` | 질문 작성자 닉네임을 숨기는 옵션 |

## 알림 (Notification)

| 용어 | 코드 | 설명 |
|------|------|------|
| 새 질문 알림 | `NotificationType.NEW_QUESTION` | 내 후기에 질문이 달렸을 때 |
| 새 답변 알림 | `NotificationType.NEW_ANSWER` | 내 질문에 답변이 달렸을 때 |
| 대화 요청 알림 | `NotificationType.CHAT_REQUEST` | 1:1 쪽지 요청 (Phase 2) |
| 인증 완료 알림 | `NotificationType.VERIFICATION_COMPLETE` | 면접 인증 승인 (Phase 2) |
| 읽음 처리 | `isRead=true` | 사용자가 알림을 확인했을 때 |
| 참조 ID | `referenceId` | 알림 관련 엔티티의 ID (questionId, answerId 등) |

## 회사 (Company)

| 용어 | 코드 | 설명 |
|------|------|------|
| 회사 검색 | `GET /api/v1/companies?q=` | 이름 부분 일치 검색, 최대 10건 |
| 업종 | `industry` | IT, 금융, 제조 등 |

## 인증 시스템 (Phase 2 전용)

| 용어 | 코드 | 설명 |
|------|------|------|
| 면접 인증 | `Verification` | 면접 경험 증빙 제출 (Phase 2) |
| 인증 상태 | `VerificationStatus` | PENDING / APPROVED / REJECTED |
| 증빙 자료 | `evidenceUrl` | S3에 업로드된 파일 URL |

## API 관련

| 용어 | 코드 | 설명 |
|------|------|------|
| 공개 API | permitAll | 비로그인도 접근 가능 |
| 인증 필요 | authenticated | JWT 토큰 필요 |
| 전환 포인트 | Q&A 블러 발견 지점 | 회원가입 유도하는 핵심 UX |
| Access Token | JWT, 30분 만료 | API 인증에 사용 |
| Refresh Token | Redis 저장, 7일 만료 | Access Token 갱신에 사용 |
