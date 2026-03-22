package com.ic.api.config.security;

import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security에서 사용하는 사용자 정보 클래스
 * JWT 토큰에서 추출한 회원 정보를 SecurityContext에 저장하기 위해 사용
 */
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String password; // 비밀번호 필드 추가
    private final String nickname;
    private final MemberRole role;

    private CustomUserDetails(Long memberId, String email, String password, String nickname, MemberRole role) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    /**
     * Member 엔티티로부터 CustomUserDetails 생성
     */
    public static CustomUserDetails from(Member member) {
        return new CustomUserDetails(
            member.getId(),
            member.getEmail(),
            member.getPassword(),
            member.getNickname(),
            member.getRole()
        );
    }

    /**
     * 회원 정보로부터 CustomUserDetails 생성 (JWT에서 추출한 정보, 비밀번호 없음)
     */
    public static CustomUserDetails of(Long memberId, String email, String nickname, MemberRole role) {
        return new CustomUserDetails(memberId, email, null, nickname, role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // 사용자 이름으로 회원 ID를 사용
        return memberId.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Getter 메서드들
    public Long getMemberId() {
        return memberId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public MemberRole getRole() {
        return role;
    }

    /**
     * 인증 회원 여부 확인
     */
    public boolean isVerified() {
        return role.isVerified();
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return role.isAdmin();
    }
}