package com.ic.api.config.security;

import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @AuthMember 어노테이션이 붙은 파라미터에 현재 인증된 회원 정보를 주입하는 ArgumentResolver
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @AuthMember 어노테이션이 있고 CustomUserDetails 또는 Long 타입인 파라미터를 지원
        return parameter.hasParameterAnnotation(AuthMember.class) &&
               (parameter.getParameterType().equals(CustomUserDetails.class) ||
                parameter.getParameterType().equals(Long.class));
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) throws Exception {

        final AuthMember authMember = parameter.getParameterAnnotation(AuthMember.class);
        if (authMember == null) {
            return null;
        }

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없는 경우
        if (authentication == null || !authentication.isAuthenticated()) {
            if (authMember.required()) {
                log.warn("Authentication required but not found for parameter: {}", parameter.getParameterName());
                throw BusinessException.from(ErrorCode.UNAUTHORIZED);
            }
            return null;
        }

        // Principal이 String인 경우 (JWT에서 설정한 memberId)
        final Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            try {
                final Long memberId = Long.valueOf((String) principal);

                // Long 타입을 요구하는 경우 memberId만 반환
                if (parameter.getParameterType().equals(Long.class)) {
                    log.debug("Resolved authenticated member ID: {}", memberId);
                    return memberId;
                }

                // CustomUserDetails 타입을 요구하는 경우
                final CustomUserDetails userDetails = customUserDetailsService.loadUserById(memberId);
                log.debug("Resolved authenticated member: memberId={}, email={}",
                         userDetails.getMemberId(), userDetails.getEmail());

                return userDetails;

            } catch (Exception e) {
                log.warn("Failed to resolve member info: {}", e.getMessage());
                if (authMember.required()) {
                    throw BusinessException.from(ErrorCode.UNAUTHORIZED);
                }
                return null;
            }
        }

        // Principal이 이미 CustomUserDetails인 경우
        if (principal instanceof CustomUserDetails) {
            final CustomUserDetails userDetails = (CustomUserDetails) principal;

            // Long 타입을 요구하는 경우 memberId만 반환
            if (parameter.getParameterType().equals(Long.class)) {
                return userDetails.getMemberId();
            }

            return userDetails;
        }

        // 기타 경우
        if (authMember.required()) {
            log.warn("Authentication principal is not supported type: {}", principal.getClass().getSimpleName());
            throw BusinessException.from(ErrorCode.UNAUTHORIZED);
        }

        return null;
    }
}