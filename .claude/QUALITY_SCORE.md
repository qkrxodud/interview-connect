# QUALITY_SCORE.md — Interview Connect 품질 기준

`tdd-test-generator`, `tdd-test-agent`, `code-reviewer`가 참조한다.

---

## TDD 사이클 (필수)

```
🔴 Red   → 실패하는 테스트 먼저 작성 (구현 없이)
🟢 Green → 테스트 통과하는 최소 구현
🔵 Blue  → 리팩토링 (테스트는 여전히 통과)
```

구현 코드 없이 테스트를 먼저 작성하지 않으면 TDD가 아니다.

---

## 테스트 피라미드 구조

```
     🔺 Acceptance Tests (10%) — 전체 HTTP 플로우
    🔷🔷 Integration Tests (20%) — API 엔드포인트
   🔹🔹🔹 Service Tests (30%) — 서비스 레이어 + Fake
  🔸🔸🔸🔸 Unit Tests (40%) — 도메인 엔티티 순수 로직
```

---

## 테스트 기반 클래스 (재사용 필수)

| 클래스 | 위치 | 용도 | 실행 속도 |
|--------|------|------|----------|
| `UnitTestBase` | ic-common/test/.../UnitTestBase.java | 도메인 순수 로직 | ~1ms |
| `ServiceTestBase` | ic-common/test/.../ServiceTestBase.java | 서비스 + Fake | ~5ms |
| `AcceptanceTestBase` | ic-common/test/.../AcceptanceTestBase.java | HTTP 종단간 | ~8s |

**Spring Boot 컨텍스트는 테스트 전체에서 1개만 사용한다** — `BaseApiWebClientTest` 상속으로 공유.

---

## Fake 사용 원칙

### Fake 사용 (권장)
- Repository → `FakeMemberRepository`, `FakeReviewRepository` 등
- 외부 의존성 있는 Service → Fake 구현체
- Clock, IdGenerator → 결정적(Deterministic) Fake

### Mock 사용 (제한적)
- 외부 SMTP, 결제 API 등 실제 네트워크 호출이 필요한 경우만
- 행위 검증(verify 횟수)이 비즈니스 요구사항인 경우만

### Fake 위치
```
ic-domain/src/test/java/com/ic/domain/{도메인}/fake/
ic-infra/src/test/java/com/ic/infra/{영역}/fake/
ic-api/src/test/java/com/ic/api/fake/
```

### Fake 필수 메서드
```java
public class FakeXxxRepository implements XxxRepository {
    public void clear() { store.clear(); sequence = 1L; }
    public int count() { return store.size(); }
}
```

---

## 테스트 코드 스타일

### @DisplayName — 도메인 언어로 작성
```java
// 나쁜 예
@DisplayName("test1")
@DisplayName("shouldThrowException")

// 좋은 예
@DisplayName("이미 존재하는 이메일로 회원가입하면 예외가 발생한다")
@DisplayName("비로그인 사용자가 Q&A 답변 조회 시 content가 null로 반환된다")
```

### given/when/then 구조 필수
```java
@Test
@DisplayName("...")
void 테스트_메서드명() {
    // given
    final Member member = MemberFixture.일반회원();

    // when
    final SomeResponse result = service.someAction(member.getId());

    // then
    assertThat(result).isNotNull();
    assertThat(result.getField()).isEqualTo(expected);
}
```

### AssertJ 사용 필수 (JUnit assert 금지)
```java
// 값 검증
assertThat(actual).isEqualTo(expected);
assertThat(list).hasSize(2).containsExactly(a, b);

// 예외 검증
assertThatThrownBy(() -> service.invalid())
    .isInstanceOf(BusinessException.class)
    .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());

// null 검증
assertThat(response.content()).isNull();
assertThat(response.answererNickname()).isNull();
```

---

## 테스트 커버리지

- 목표: **90% 이상**
- 측정: JaCoCo
- 미달 시: Gradle 빌드 실패

```gradle
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit { minimum = 0.90 }
        }
    }
}
```

---

## Fixture 클래스 (재사용)

| 클래스 | 위치 | 주요 메서드 |
|--------|------|-----------|
| `MemberFixture` | ic-domain/test | `일반회원()`, `인증회원()`, `관리자()` |
| `AuthFixture` | ic-api/test | `회원가입_요청.정상()`, `로그인_요청.정상()` |
| `InterviewReviewFixture` | ic-domain/test | `일반후기()`, `PASS_후기()` |

새 도메인 구현 시 대응하는 Fixture 클래스를 함께 작성한다.

---

## 테스트 격리

- `@AfterEach`에서 Fake.clear() 또는 repository.deleteAll() 호출
- `@DirtiesContext` 사용 금지 (Spring Boot 재시작 유발)
- 테스트 간 의존성 금지 (실행 순서에 무관해야 함)

---

## 금지 사항

- `verify()` / `times()` 기본 사용 (상호작용 검증은 비즈니스 요구가 있을 때만)
- WebClient Mock 사용
- 테스트에서 `Thread.sleep()` 사용
- 테스트 순서 의존 (`@TestMethodOrder` 신중히 사용)
