package com.ic.domain.member;

import com.ic.common.exception.BusinessException;
import com.ic.common.test.UnitTestBase;
import com.ic.domain.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Member 도메인 엔티티 테스트
 */
@DisplayName("Member 도메인 테스트")
class MemberTest extends UnitTestBase {

    @Nested
    @DisplayName("회원 생성")
    class 회원_생성 {

        @Test
        @DisplayName("일반 회원 생성 성공")
        void 일반_회원_생성_성공() {
            // given
            final String email = "test@example.com";
            final String password = "password123";
            final String nickname = "테스트";

            // when
            final Member member = Member.createGeneral(email, password, nickname);

            // then
            assertThat(member.getEmail()).isEqualTo(email);
            assertThat(member.getPassword()).isEqualTo(password);
            assertThat(member.getNickname()).isEqualTo(nickname);
            assertThat(member.getRole()).isEqualTo(MemberRole.GENERAL);
            assertThat(member.getId()).isNull(); // 아직 저장되지 않음
        }

        @Test
        @DisplayName("Builder 패턴으로 회원 생성")
        void Builder_패턴으로_회원_생성() {
            // given & when
            final Member member = Member.builder()
                    .email("test@example.com")
                    .password("password123")
                    .nickname("테스트")
                    .role(MemberRole.VERIFIED)
                    .build();

            // then
            assertThat(member.getEmail()).isEqualTo("test@example.com");
            assertThat(member.getRole()).isEqualTo(MemberRole.VERIFIED);
        }
    }

    @Nested
    @DisplayName("데이터 검증")
    class 데이터_검증 {

        @Test
        @DisplayName("이메일이 null인 경우 예외 발생")
        void 이메일이_null인_경우_예외_발생() {
            // when & then
            assertThatThrownBy(() -> Member.createGeneral(null, "password123", "테스트"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("이메일이 빈 문자열인 경우 예외 발생")
        void 이메일이_빈_문자열인_경우_예외_발생() {
            // when & then
            assertThatThrownBy(() -> Member.createGeneral("", "password123", "테스트"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("너무 긴 이메일인 경우 예외 발생")
        void 너무_긴_이메일인_경우_예외_발생() {
            // given
            final String longEmail = MemberFixture.잘못된_데이터.너무_긴_이메일();

            // when & then
            assertThatThrownBy(() -> Member.createGeneral(longEmail, "password123", "테스트"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("비밀번호가 null인 경우 예외 발생")
        void 비밀번호가_null인_경우_예외_발생() {
            // when & then
            assertThatThrownBy(() -> Member.createGeneral("test@example.com", null, "테스트"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("비밀번호가 빈 문자열인 경우 예외 발생")
        void 비밀번호가_빈_문자열인_경우_예외_발생() {
            // when & then
            assertThatThrownBy(() -> Member.createGeneral("test@example.com", "", "테스트"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("너무 긴 비밀번호인 경우 예외 발생")
        void 너무_긴_비밀번호인_경우_예외_발생() {
            // given
            final String longPassword = MemberFixture.잘못된_데이터.너무_긴_비밀번호();

            // when & then
            assertThatThrownBy(() -> Member.createGeneral("test@example.com", longPassword, "테스트"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("닉네임이 null인 경우 예외 발생")
        void 닉네임이_null인_경우_예외_발생() {
            // when & then
            assertThatThrownBy(() -> Member.createGeneral("test@example.com", "password123", null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("닉네임이 빈 문자열인 경우 예외 발생")
        void 닉네임이_빈_문자열인_경우_예외_발생() {
            // when & then
            assertThatThrownBy(() -> Member.createGeneral("test@example.com", "password123", ""))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("너무 긴 닉네임인 경우 예외 발생")
        void 너무_긴_닉네임인_경우_예외_발생() {
            // given
            final String longNickname = MemberFixture.잘못된_데이터.너무_긴_닉네임();

            // when & then
            assertThatThrownBy(() -> Member.createGeneral("test@example.com", "password123", longNickname))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("짧은 비밀번호인 경우 예외 발생")
        void 짧은_비밀번호인_경우_예외_발생() {
            // given
            final String shortPassword = MemberFixture.잘못된_데이터.짧은_비밀번호();

            // when & then
            assertThatThrownBy(() -> Member.createGeneral("test@example.com", shortPassword, "테스트"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("잘못된 이메일 형식인 경우 예외 발생")
        void 잘못된_이메일_형식인_경우_예외_발생() {
            // given
            final String invalidEmail = MemberFixture.잘못된_데이터.잘못된_이메일_형식();

            // when & then
            assertThatThrownBy(() -> Member.createGeneral(invalidEmail, "password123", "테스트"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("짧은 닉네임인 경우 예외 발생")
        void 짧은_닉네임인_경우_예외_발생() {
            // given
            final String shortNickname = MemberFixture.잘못된_데이터.짧은_닉네임();

            // when & then
            assertThatThrownBy(() -> Member.createGeneral("test@example.com", "password123", shortNickname))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class 회원_정보_수정 {

        @Test
        @DisplayName("닉네임 변경 성공")
        void 닉네임_변경_성공() {
            // given
            final Member member = MemberFixture.일반회원();
            final String newNickname = "새로운닉네임";

            // when
            member.changeNickname(newNickname);

            // then
            assertThat(member.getNickname()).isEqualTo(newNickname);
        }

        @Test
        @DisplayName("비밀번호 변경 성공")
        void 비밀번호_변경_성공() {
            // given
            final Member member = MemberFixture.일반회원();
            final String newPassword = "newPassword123";

            // when
            member.changePassword(newPassword);

            // then
            assertThat(member.getPassword()).isEqualTo(newPassword);
        }

        @Test
        @DisplayName("잘못된 닉네임으로 변경 시 예외 발생")
        void 잘못된_닉네임으로_변경_시_예외_발생() {
            // given
            final Member member = MemberFixture.일반회원();
            final String invalidNickname = MemberFixture.잘못된_데이터.너무_긴_닉네임();

            // when & then
            assertThatThrownBy(() -> member.changeNickname(invalidNickname))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("null 닉네임으로 변경 시 예외 발생")
        void null_닉네임으로_변경_시_예외_발생() {
            // given
            final Member member = MemberFixture.일반회원();

            // when & then
            assertThatThrownBy(() -> member.changeNickname(null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("잘못된 비밀번호로 변경 시 예외 발생")
        void 잘못된_비밀번호로_변경_시_예외_발생() {
            // given
            final Member member = MemberFixture.일반회원();
            final String shortPassword = MemberFixture.잘못된_데이터.짧은_비밀번호();

            // when & then
            assertThatThrownBy(() -> member.changePassword(shortPassword))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("null 비밀번호로 변경 시 예외 발생")
        void null_비밀번호로_변경_시_예외_발생() {
            // given
            final Member member = MemberFixture.일반회원();

            // when & then
            assertThatThrownBy(() -> member.changePassword(null))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("역할 관리")
    class 역할_관리 {

        @Test
        @DisplayName("일반회원을 인증회원으로 승급")
        void 일반회원을_인증회원으로_승급() {
            // given
            final Member member = MemberFixture.일반회원();

            // when
            member.changeRole(MemberRole.VERIFIED);

            // then
            assertThat(member.getRole()).isEqualTo(MemberRole.VERIFIED);
        }

        @Test
        @DisplayName("인증회원을 관리자로 승급")
        void 인증회원을_관리자로_승급() {
            // given
            final Member member = MemberFixture.인증회원();

            // when
            member.changeRole(MemberRole.ADMIN);

            // then
            assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN);
        }

        @Test
        @DisplayName("관리자를 일반회원으로 강등")
        void 관리자를_일반회원으로_강등() {
            // given
            final Member member = MemberFixture.관리자();

            // when
            member.changeRole(MemberRole.GENERAL);

            // then
            assertThat(member.getRole()).isEqualTo(MemberRole.GENERAL);
        }

        @Test
        @DisplayName("null 역할로 변경 시 예외 발생")
        void null_역할로_변경_시_예외_발생() {
            // given
            final Member member = MemberFixture.일반회원();

            // when & then
            assertThatThrownBy(() -> member.changeRole(null))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("권한 확인")
    class 권한_확인 {

        @Test
        @DisplayName("일반회원은 질문 작성 가능")
        void 일반회원은_질문_작성_가능() {
            // given
            final Member member = MemberFixture.일반회원();

            // when & then
            assertThat(member.canAskQuestion()).isTrue();
        }

        @Test
        @DisplayName("일반회원은 후기 작성 불가능")
        void 일반회원은_후기_작성_불가능() {
            // given
            final Member member = MemberFixture.일반회원();

            // when & then
            assertThat(member.canWriteReview()).isFalse();
        }

        @Test
        @DisplayName("인증회원은 후기 작성 가능")
        void 인증회원은_후기_작성_가능() {
            // given
            final Member member = MemberFixture.인증회원();

            // when & then
            assertThat(member.canWriteReview()).isTrue();
        }

        @Test
        @DisplayName("인증회원은 답변 작성 가능")
        void 인증회원은_답변_작성_가능() {
            // given
            final Member member = MemberFixture.인증회원();

            // when & then
            assertThat(member.canAnswerQuestion()).isTrue();
        }

        @Test
        @DisplayName("일반회원은 답변 작성 불가능")
        void 일반회원은_답변_작성_불가능() {
            // given
            final Member member = MemberFixture.일반회원();

            // when & then
            assertThat(member.canAnswerQuestion()).isFalse();
        }

        @Test
        @DisplayName("관리자는 모든 권한 보유")
        void 관리자는_모든_권한_보유() {
            // given
            final Member member = MemberFixture.관리자();

            // when & then
            assertThat(member.canAskQuestion()).isTrue();
            assertThat(member.canWriteReview()).isTrue();
            assertThat(member.canAnswerQuestion()).isTrue();
        }

        @Test
        @DisplayName("회원 상태 확인 메서드들이 정상 동작")
        void 회원_상태_확인_메서드들이_정상_동작() {
            // given
            final Member 일반회원 = MemberFixture.일반회원();
            final Member 인증회원 = MemberFixture.인증회원();
            final Member 관리자 = MemberFixture.관리자();

            // when & then
            // 일반회원 검증
            assertThat(일반회원.isGeneral()).isTrue();
            assertThat(일반회원.isVerified()).isFalse();
            assertThat(일반회원.isAdmin()).isFalse();

            // 인증회원 검증
            assertThat(인증회원.isGeneral()).isFalse();
            assertThat(인증회원.isVerified()).isTrue();
            assertThat(인증회원.isAdmin()).isFalse();

            // 관리자 검증
            assertThat(관리자.isGeneral()).isFalse();
            assertThat(관리자.isVerified()).isTrue(); // 관리자는 인증회원이기도 함
            assertThat(관리자.isAdmin()).isTrue();
        }
    }
}