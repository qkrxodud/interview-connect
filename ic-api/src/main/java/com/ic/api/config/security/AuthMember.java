package com.ic.api.config.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 메서드의 파라미터에서 현재 인증된 회원 정보를 주입받기 위한 어노테이션
 *
 * 사용 예시:
 * <pre>
 * {@code
 * @GetMapping("/profile")
 * public ApiResponse<MemberProfile> getProfile(@AuthMember CustomUserDetails userDetails) {
 *     // userDetails를 통해 현재 로그인한 회원 정보에 접근
 *     return ApiResponse.ok(memberService.getProfile(userDetails.getMemberId()));
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthMember {

    /**
     * 인증이 필수인지 여부
     * true: 인증되지 않은 경우 예외 발생
     * false: 인증되지 않은 경우 null 반환
     */
    boolean required() default true;
}