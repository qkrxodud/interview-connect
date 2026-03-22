package com.ic.domain.member;

import com.ic.common.test.UnitTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemberRole enum 테스트
 */
@DisplayName("MemberRole 테스트")
class MemberRoleTest extends UnitTestBase {

    @Nested
    @DisplayName("권한 확인")
    class 권한_확인 {

        @Test
        @DisplayName("GENERAL 역할 권한 확인")
        void GENERAL_역할_권한_확인() {
            // given
            final MemberRole role = MemberRole.GENERAL;

            // when & then
            assertThat(role.canAskQuestion()).isTrue();
            assertThat(role.canWriteReview()).isFalse();
            assertThat(role.canAnswerQuestion()).isFalse();
        }

        @Test
        @DisplayName("VERIFIED 역할 권한 확인")
        void VERIFIED_역할_권한_확인() {
            // given
            final MemberRole role = MemberRole.VERIFIED;

            // when & then
            assertThat(role.canAskQuestion()).isTrue();
            assertThat(role.canWriteReview()).isTrue();
            assertThat(role.canAnswerQuestion()).isTrue();
        }

        @Test
        @DisplayName("ADMIN 역할 권한 확인")
        void ADMIN_역할_권한_확인() {
            // given
            final MemberRole role = MemberRole.ADMIN;

            // when & then
            assertThat(role.canAskQuestion()).isTrue();
            assertThat(role.canWriteReview()).isTrue();
            assertThat(role.canAnswerQuestion()).isTrue();
        }
    }

    @Nested
    @DisplayName("역할 비교")
    class 역할_비교 {

        @Test
        @DisplayName("GENERAL이 가장 낮은 권한")
        void GENERAL이_가장_낮은_권한() {
            // when & then
            assertThat(MemberRole.GENERAL.ordinal())
                    .isLessThan(MemberRole.VERIFIED.ordinal());
            assertThat(MemberRole.GENERAL.ordinal())
                    .isLessThan(MemberRole.ADMIN.ordinal());
        }

        @Test
        @DisplayName("ADMIN이 가장 높은 권한")
        void ADMIN이_가장_높은_권한() {
            // when & then
            assertThat(MemberRole.ADMIN.ordinal())
                    .isGreaterThan(MemberRole.GENERAL.ordinal());
            assertThat(MemberRole.ADMIN.ordinal())
                    .isGreaterThan(MemberRole.VERIFIED.ordinal());
        }
    }
}