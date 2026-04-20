# Interview Connect TDD Test Guide

본 가이드는 Interview Connect 프로젝트에서 TDD 기반 테스트 작성을 위한 종합적인 안내서입니다.

## 📋 목차

1. [테스트 구조 개요](#테스트-구조-개요)
2. [Fake vs Mock 가이드라인](#fake-vs-mock-가이드라인)
3. [단위 테스트 작성법](#단위-테스트-작성법)
4. [통합 테스트 작성법](#통합-테스트-작성법)
5. [테스트 데이터 관리](#테스트-데이터-관리)
6. [TDD 워크플로우](#tdd-워크플로우)
7. [Best Practices](#best-practices)

## 🏗 테스트 구조 개요

### 핵심 구성 요소

```
ic-api/src/test/java/com/ic/api/
├── fake/                           # Fake 구현체들
│   ├── FakeMemberRepository.java
│   ├── FakeCompanyRepository.java
│   ├── FakePasswordEncoder.java
│   └── ...
├── integration/                    # 통합 테스트
│   ├── BaseApiWebClientTest.java   # 기반 클래스
│   └── config/
│       └── IntegrationTestFakesConfig.java
├── test/                          # 테스트 유틸리티
│   ├── TestDataFactory.java       # 테스트 데이터 생성
│   ├── DatabaseCleaner.java       # 데이터 정리
│   └── TestFixture.java           # 고정 데이터 설정
└── {domain}/                      # 도메인별 테스트
    ├── service/                   # 서비스 계층 테스트
    └── controller/                # 컨트롤러 통합 테스트
```

## 🎯 Fake vs Mock 가이드라인

### ✅ Fake를 사용해야 하는 경우

- **Repository**: 모든 데이터 저장소 인터페이스
- **PasswordEncoder**: 빠른 테스트를 위한 인코딩
- **JwtTokenProvider**: 예측 가능한 토큰 생성
- **외부 API 클라이언트**: 제어 가능한 응답

### ⚠️ Mock을 사용하는 경우 (제한적)

- **복잡한 외부 시스템**: 실제 구현이 불가능한 경우
- **행위 검증이 핵심**: 메서드 호출 순서나 횟수가 중요한 경우

### Fake 구현 예시

```java
public class FakeMemberRepository implements MemberRepository {
    private final Map<Long, Member> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Member save(Member member) {
        if (member.getId() == null) {
            final Member newMember = Member.builder()
                    .id(sequence.getAndIncrement())
                    .email(member.getEmail())
                    .password(member.getPassword())
                    .nickname(member.getNickname())
                    .role(member.getRole())
                    .build();
            store.put(newMember.getId(), newMember);
            return newMember;
        }
        store.put(member.getId(), member);
        return member;
    }

    // 테스트 헬퍼 메서드
    public void clear() {
        store.clear();
        sequence.set(1L);
    }

    public boolean hasMemberWithEmail(String email) {
        return existsByEmail(email);
    }
}
```

## 🧪 단위 테스트 작성법

### 기본 템플릿

```java
@DisplayName("AuthService Fake 기반 테스트")
class AuthServiceFakeTest {

    // === Fake 구현체들 ===
    private FakeMemberRepository fakeMemberRepository;
    private FakePasswordEncoder fakePasswordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // given: Fake 구현체들 초기화
        fakeMemberRepository = new FakeMemberRepository();
        fakePasswordEncoder = new FakePasswordEncoder();

        authService = new AuthService(
                fakeMemberRepository,
                fakePasswordEncoder
        );
    }

    @Nested
    @DisplayName("회원가입")
    class 회원가입_테스트 {

        @Test
        @DisplayName("회원가입 성공 시 회원 정보와 인코딩된 비밀번호가 저장된다")
        void shouldSaveMemberWithEncodedPasswordWhenSignupSuccess() {
            // given
            final SignupRequest request = new SignupRequest("test@example.com", "password123", "테스트");

            // when
            final var response = authService.signup(request);

            // then
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(fakeMemberRepository.size()).isEqualTo(1);
            assertThat(fakePasswordEncoder.isEncoded(
                    fakeMemberRepository.findByEmail("test@example.com").get().getPassword()
            )).isTrue();
        }
    }
}
```

### TDD 사이클 적용

#### 🔴 Red Phase
```java
@Test
@DisplayName("중복된 이메일로 회원가입 시 예외가 발생한다")
void shouldThrowExceptionWhenDuplicateEmail() {
    // given: 아직 구현되지 않은 기능을 테스트
    final SignupRequest request = new SignupRequest("test@example.com", "password123", "테스트");
    authService.signup(request); // 첫 번째 가입

    // when & then: 실패하는 테스트 작성
    assertThatThrownBy(() -> authService.signup(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());
}
```

#### 🟢 Green Phase
```java
// AuthService.java
public SignupResponse signup(SignupRequest request) {
    // 최소한의 구현으로 테스트 통과
    if (memberRepository.existsByEmail(request.email())) {
        throw BusinessException.from(ErrorCode.DUPLICATE_EMAIL);
    }
    // ... 기존 로직
}
```

#### 🔵 Refactor Phase
- 중복 코드 제거
- 메서드 추출
- 테스트 헬퍼 메서드 생성

## 🌐 통합 테스트 작성법

### BaseApiWebClientTest 사용

```java
@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest extends BaseApiWebClientTest {

    private static final String AUTH_BASE_URL = "/api/v1/auth";

    @Test
    @DisplayName("유효한 회원가입 요청 시 201 Created와 회원 정보를 반환한다")
    void shouldReturn201AndMemberInfoWhenValidSignupRequest() {
        // given
        final SignupRequest request = new SignupRequest("test@example.com", "password123", "테스트");

        // when
        final String response = webClient.post()
                .uri(AUTH_BASE_URL + "/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(toJson(request))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // then
        final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
        assertThat(apiResponse.isSuccess()).isTrue();

        // Fake Repository에서 상태 검증
        assertThat(fakesConfig.getMemberRepository().hasMemberWithEmail("test@example.com")).isTrue();
    }
}
```

### 데이터 정리 자동화

```java
@TestConfiguration
@Import(IntegrationTestFakesConfig.class)
public class IntegrationTestConfig {

    @Bean
    public DatabaseCleaner databaseCleaner(IntegrationTestFakesConfig fakesConfig) {
        return new DatabaseCleaner(
                fakesConfig.getMemberRepository(),
                fakesConfig.getCompanyRepository()
                // ... 다른 Fake 구현체들
        );
    }
}
```

## 🗄 테스트 데이터 관리

### TestDataFactory 활용

```java
public final class TestDataFactory {

    public static Member createTestMember() {
        return createTestMember("test@example.com", "테스트유저");
    }

    public static Member createTestMember(String email, String nickname) {
        return Member.createGeneral(email, "FAKE_ENCODED_password123", nickname);
    }

    public static Company createKakaoCompany() {
        return createTestCompany("카카오", "IT", "https://example.com/logo.png", "https://kakao.com");
    }
}
```

### TestFixture로 복잡한 시나리오 구성

```java
@Test
@DisplayName("멀티 유저 환경에서 각 권한에 따른 기능 접근이 올바르게 제한된다")
void shouldRestrictFunctionsBasedOnUserRoles() {
    // given
    final var env = testFixture.setupMultiUserEnvironment();

    // when & then
    assertThat(env.generalMember.canWriteReview()).isFalse();
    assertThat(env.verifiedMember.canWriteReview()).isTrue();
    assertThat(env.adminMember.isAdmin()).isTrue();
}
```

## 🔄 TDD 워크플로우

### 1. 요구사항 분석
```
User Story: 사용자는 이메일 중복 확인 후 회원가입을 할 수 있다.

Acceptance Criteria:
- 유효한 이메일, 비밀번호, 닉네임으로 가입 성공
- 중복된 이메일로는 가입 실패
- 중복된 닉네임으로는 가입 실패
- 비밀번호는 암호화되어 저장
```

### 2. Test Scenarios 도출
```java
// Test Scenarios
class 회원가입_시나리오 {
    // ✅ Success Cases
    void shouldCreateMemberWithValidInformation()
    void shouldEncodePasswordBeforeSaving()

    // ❌ Failure Cases
    void shouldFailWhenEmailAlreadyExists()
    void shouldFailWhenNicknameAlreadyExists()
    void shouldFailWhenEmailFormatInvalid()
    void shouldFailWhenPasswordTooShort()

    // 🔍 Edge Cases
    void shouldTrimWhitespaceFromEmailAndNickname()
    void shouldHandleCaseInsensitiveEmailComparison()
}
```

### 3. Red → Green → Blue 사이클

```java
// 🔴 RED: 실패하는 테스트 작성
@Test
void shouldFailWhenEmailAlreadyExists() {
    // given
    authService.signup(new SignupRequest("test@example.com", "pass123", "user1"));

    // when & then
    assertThatThrownBy(() ->
        authService.signup(new SignupRequest("test@example.com", "pass456", "user2"))
    ).isInstanceOf(BusinessException.class);
}

// 🟢 GREEN: 최소 구현
public SignupResponse signup(SignupRequest request) {
    if (memberRepository.existsByEmail(request.email())) {
        throw new BusinessException("이미 존재하는 이메일입니다");
    }
    // 나머지 로직...
}

// 🔵 BLUE: 리팩터링
private void validateDuplicateEmail(String email) {
    if (memberRepository.existsByEmail(email)) {
        throw BusinessException.from(ErrorCode.DUPLICATE_EMAIL);
    }
}
```

## ⭐ Best Practices

### 1. 테스트 네이밍 규칙

```java
@DisplayName("회원가입 성공 시 회원 정보와 인코딩된 비밀번호가 저장된다")
void shouldSaveMemberWithEncodedPasswordWhenSignupSuccess()

// 패턴: should{ExpectedResult}When{Condition}
// 또는: should{ExpectedResult}Given{Condition}
```

### 2. Assertion 우선순위

```java
// ✅ 좋은 예: 상태 기반 검증
assertThat(response.email()).isEqualTo("test@example.com");
assertThat(fakeMemberRepository.size()).isEqualTo(1);
assertThat(fakePasswordEncoder.isEncoded(savedMember.getPassword())).isTrue();

// ❌ 나쁜 예: 행위 기반 검증 (Mock 사용)
verify(memberRepository).save(any(Member.class));
verify(passwordEncoder).encode("password123");
```

### 3. 테스트 격리

```java
@BeforeEach
void setUp() {
    // 각 테스트마다 새로운 Fake 인스턴스
    fakeMemberRepository = new FakeMemberRepository();
}

@AfterEach
void cleanUp() {
    // 통합 테스트에서 데이터 정리
    fakesConfig.resetAllFakes();
}
```

### 4. 테스트 데이터 관리

```java
// ✅ 좋은 예: 명시적 데이터 생성
final Member member = TestDataFactory.createTestMember("specific@test.com", "특정유저");

// ❌ 나쁜 예: 하드코딩된 데이터
final Member member = new Member("test@test.com", "password", "user");
```

### 5. 복잡한 시나리오 테스트

```java
@Test
@DisplayName("회원가입 → 로그인 → 토큰갱신 → 로그아웃 전체 플로우가 정상 동작한다")
void shouldHandleCompleteAuthFlowSuccessfully() {
    // given: 각 단계별 명확한 검증

    // 1. 회원가입
    final var signupResponse = authService.signup(signupRequest);
    assertThat(fakeMemberRepository.size()).isEqualTo(1);

    // 2. 로그인
    final var loginResponse = authService.login(loginRequest);
    assertThat(fakeRefreshTokenRepository.hasToken(loginResponse.memberInfo().id())).isTrue();

    // 3. 토큰 갱신
    final var refreshResponse = authService.refreshAccessToken(refreshRequest);
    assertThat(refreshResponse.accessToken()).isNotEmpty();

    // 4. 로그아웃
    authService.logout(memberId);
    assertThat(fakeRefreshTokenRepository.hasToken(memberId)).isFalse();
}
```

## 🚀 실제 적용 예시

### 새로운 기능 개발 시 TDD 적용

```java
// 1. 요구사항: "인증된 사용자만 후기를 작성할 수 있다"

// 2. 실패 테스트 작성
@Test
@DisplayName("인증되지 않은 사용자의 후기 작성 시도 시 예외가 발생한다")
void shouldThrowExceptionWhenUnauthorizedUserWritesReview() {
    // given
    final Member generalMember = TestDataFactory.createTestMember();
    final Company company = TestDataFactory.createKakaoCompany();
    final ReviewRequest request = new ReviewRequest(company.getId(), "백엔드", 3, "좋았음");

    // when & then
    assertThatThrownBy(() -> reviewService.writeReview(generalMember.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.INSUFFICIENT_PERMISSION.getMessage());
}

// 3. 최소 구현
public ReviewResponse writeReview(Long memberId, ReviewRequest request) {
    final Member member = findMemberById(memberId);
    if (!member.canWriteReview()) {
        throw BusinessException.from(ErrorCode.INSUFFICIENT_PERMISSION);
    }
    // ... 나머지 로직
}

// 4. 리팩터링 및 추가 테스트
```

이 가이드를 통해 Interview Connect 프로젝트에서 일관된 TDD 기반 개발을 수행할 수 있습니다. 항상 **Red → Green → Blue** 사이클을 준수하고, **Fake 우선, Mock 최소** 원칙을 따르시기 바랍니다.