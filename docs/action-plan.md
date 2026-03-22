# Interview Connect — 액션 플랜

## 전체 로드맵

| 단계 | 기간 | 핵심 목표 | 성공 지표 |
|------|------|-----------|-----------|
| Phase 1 | Week 1~4 | MVP — 후기 공개 + Q&A + 블러 전환 | 후기 50건+, 전환율 10%+ |
| Phase 2 | Week 5~8 | 연결 — 1:1 쪽지 + 인증 시스템 | 연결 100건+ |
| Phase 3 | Week 9~12 | 성장 — 검색/SEO/알림/소셜로그인 | MAU 1,000+ |
| Phase 4 | Week 13~16 | 확장 — 멘토링/실시간 채팅 | 멘토 50명+ |

---

## Phase 1 — MVP (Week 1~4)

### Week 1: 프로젝트 초기화 + 회원 시스템
- Spring Boot 3.x + Java 17 + Gradle 멀티모듈 프로젝트 생성
- Spring Security + JWT 인증/인가
- 회원가입/로그인/로그아웃 API
- 회원 등급: GENERAL / VERIFIED / ADMIN
- 비로그인 API permitAll 설정

### Week 2: 회사 + 후기 CRUD
- Company 엔티티 + 검색/자동완성 API
- InterviewReview 엔티티 + CRUD API
- 후기 목록/상세 조회 — 비로그인 공개 (permitAll)
- 필터 + 페이징 + 조회수

### Week 3: 공개 Q&A + 답변 블러
- ReviewQuestion / ReviewAnswer 엔티티
- 질문 작성 (로그인), 답변 작성 (인증 회원)
- Q&A 조회 — 비로그인 시 답변 블러 (content=null, blurred=true, preview)
- Notification 테이블 + 알림

### Week 4: 프론트엔드 + 배포
- Next.js 프론트엔드 (SSR/SSG)
- 답변 블러 UI (CSS blur + CTA 오버레이)
- CI/CD + AWS 배포
- 씨드 콘텐츠 50건+

---

## Phase 2 — 연결 (Week 5~8)

### Week 5~6: 면접 인증 시스템
- Verification 엔티티 + 인증 요청/승인/거절 API
- 이미지 업로드 (AWS S3)
- 관리자 페이지

### Week 7~8: 1:1 쪽지 시스템
- ChatRoom / ChatMessage 엔티티
- 대화 요청/수락/거절/메시지 API
- 작성자 보호: 수락제, DM 토글, 요청 제한, 신고/차단

---

## Phase 3 — 성장 (Week 9~12)

### Week 9~10: 검색 고도화 + SEO
- QueryDSL 복합 필터 검색
- Next.js SSR/SSG SEO 최적화
- Open Graph 메타태그, 사이트맵

### Week 11~12: 알림 + 소셜 로그인
- 이메일 알림 (Spring Mail)
- Kakao/Google OAuth2 소셜 로그인
- 회사별 통계 대시보드

---

## Phase 4 — 확장 (Week 13~16)

### Week 13~14: 멘토링
- 멘토 프로필 등록/검색
- 멘토링 요청 시스템

### Week 15~16: 실시간 채팅
- WebSocket + STOMP 실시간 채팅
- Redis Pub/Sub 다중 서버 지원

---

## 리스크 관리

| 리스크 | 대응 | 시점 |
|--------|------|------|
| 법적 (명예훼손) | 작성 가이드라인 + 이용약관 | Phase 1 전 |
| 전환율 저조 | A/B 테스트 (블러 강도/CTA) | Phase 1 |
| 콘텐츠 부족 | 블로그 이전 + 씨드 후기 | Phase 1 |
| 작성자 이탈 | 수락제 + 요청 제한 | Phase 2 |
| 인증 어뷰징 | 다중 인증 + 신고 시스템 | Phase 2 |
