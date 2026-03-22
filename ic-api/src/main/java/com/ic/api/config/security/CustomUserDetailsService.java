package com.ic.api.config.security;

import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security에서 사용자 정보를 로드하는 서비스
 * 주로 JWT 인증에서 회원 정보를 조회할 때 사용
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // 먼저 이메일로 시도
            if (username.contains("@")) {
                return loadUserByEmail(username);
            }

            // 그 다음 회원 ID로 시도
            final Long memberId = Long.valueOf(username);
            final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

            return CustomUserDetails.from(member);

        } catch (NumberFormatException e) {
            // 숫자가 아니면 이메일로 재시도
            return loadUserByEmail(username);
        } catch (BusinessException e) {
            throw new UsernameNotFoundException("Member not found: " + username);
        }
    }

    /**
     * 이메일로 사용자 정보 로드
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        final Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Member not found with email: " + email));

        return CustomUserDetails.from(member);
    }

    /**
     * 회원 ID로 사용자 정보 로드 (내부 사용)
     */
    public CustomUserDetails loadUserById(Long memberId) {
        final Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

        return CustomUserDetails.from(member);
    }
}