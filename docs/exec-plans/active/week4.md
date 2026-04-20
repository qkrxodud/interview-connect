# Week 4 실행 계획 — 프론트엔드 + 씨드 데이터

`backend-implementer`가 이 파일을 참조한다.
프론트엔드는 Bootstrap 기반으로 사용자가 직접 관리한다.
완료 항목은 `[x]`로 표시한다.

---

## 목표

Bootstrap 기반 서버사이드 렌더링 프론트엔드와 씨드 데이터를 완성한다.
SEO 최적화를 위해 Thymeleaf + Bootstrap으로 서버에서 HTML을 렌더링한다.
배포는 사용자가 직접 수행하므로, 이 파일에서는 배포 관련 태스크를 다루지 않는다.

---

## Task 4-1: Bootstrap 기반 프론트엔드 (Thymeleaf)

**담당:** backend-implementer  
**선결 조건:** Week 3 완료

**선택 근거:** Next.js가 아닌 Thymeleaf + Bootstrap을 사용한다. SSR로 SEO가 자연스럽게 해결되고, 추가 빌드 파이프라인 없이 Spring Boot에 통합된다.

### 주요 화면

- [x] `/` — 랜딩 페이지
  - 서비스 소개, 최신 후기 5건 미리보기
  - 회원가입/로그인 CTA

- [x] `/reviews` — 후기 목록 페이지
  - 필터 폼: 회사 자동완성, 직무, 난이도, 결과
  - 페이징 (서버사이드)
  - 각 카드: 회사명, 직무, 별점(난이도/분위기), 결과 배지, 조회수

- [x] `/reviews/{id}` — 후기 상세 페이지 (SEO 핵심)
  - `<title>`: `{회사명} {직무} 면접 후기 — 난이도 {N}/5 | Interview Connect`
  - `<meta description>`: content 앞 150자
  - `<og:title>`, `<og:description>` 포함
  - Q&A 섹션: 비로그인 시 답변 블러 처리

- [x] `/auth/login` — 로그인 폼
- [x] `/auth/signup` — 회원가입 폼
- [x] `/my` — 마이페이지 (내 후기, 내 질문, 알림)

### 답변 블러 UI (핵심)

비로그인 시 답변에 블러 오버레이를 적용한다.

```html
<!-- blurred=true일 때 -->
<div class="answer-content blurred-answer">
  <p class="preview-text text-muted">{{ preview }}...</p>
  <div class="blur-overlay">
    <div class="blur-cta">
      <i class="bi bi-lock-fill"></i>
      <p>로그인하면 답변을 볼 수 있어요</p>
      <a href="/auth/signup" class="btn btn-primary">무료로 시작하기</a>
    </div>
  </div>
</div>
```

```css
.blurred-answer {
  position: relative;
  filter: blur(4px);
  user-select: none;
}
.blur-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255,255,255,0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  filter: none; /* 오버레이 자체는 블러 적용 안 함 */
}
```

---

## Task 4-2: 씨드 데이터

**담당:** backend-implementer  
**선결 조건:** Task 4-1

- [x] `DataInitializer` — `@Component` + `ApplicationRunner` + `@Profile("local")`
  - 이미 데이터가 있으면 건너뜀 (`companyRepository.count() > 0` 체크)

### 씨드 회사 목록 (50개)

```
IT 대기업: 카카오, 네이버, 삼성SDS, LG CNS, SK C&C, 현대오토에버, KT DS, 롯데정보통신
핀테크:    토스, 카카오뱅크, 카카오페이, 네이버파이낸셜, 뱅크샐러드, 핀다, NHN페이코
스타트업:  쿠팡, 배달의민족, 당근마켓, 직방, 야놀자, 무신사, 오늘의집, 마켓컬리, 지그재그
글로벌:    라인, 크래프톤, 넥슨, 엔씨소프트, 넷마블
... (나머지 채움)
```

### 씨드 후기 (20건)

```
- VERIFIED 테스트 계정으로 작성 (없으면 DataInitializer에서 생성)
- 각 후기: 회사, 직무, 면접유형, 질문 3개, 난이도 3~5, 분위기 3~5, 결과 혼합
```

### 씨드 Q&A (후기당 3~5개)

```
- 각 질문: 다른 테스트 계정이 작성, isAnonymous 혼합
- 각 답변: VERIFIED 계정이 작성
```

---

## Week 4 완료 기준

- [x] `/reviews` 페이지 브라우저에서 정상 렌더링 (후기 20건 노출)
- [x] `/reviews/{id}` 비로그인 시 답변 블러 UI 확인
- [x] `/reviews/{id}` source 보기 → `<title>`, `<meta>`, `<og:*>` SEO 태그 확인 (2026-04-20)
- [x] 회원가입/로그인 폼 동작
- [x] DataInitializer: `@Profile("local")`에서 회사 52개 + 후기 20건 + Q&A 정상 삽입
- [x] `./gradlew test` 전체 통과 (BUILD SUCCESSFUL)

---

## 배포 (사용자 직접 수행)

배포는 사용자가 직접 진행한다. backend-implementer는 다음 파일만 준비한다:

- [x] `Dockerfile` — Spring Boot 애플리케이션 컨테이너화
- [x] `docker-compose.yml` — app + mysql + redis 로컬 실행용
- [ ] `application-prod.yml` — 환경변수 기반 프로덕션 설정 (DB URL, Redis, JWT Secret)
- [x] `.env.example` — 필요한 환경변수 목록 (실제 값 없음)
