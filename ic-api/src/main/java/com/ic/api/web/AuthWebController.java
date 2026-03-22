package com.ic.api.web;

import com.ic.api.auth.dto.LoginRequest;
import com.ic.api.auth.dto.SignupRequest;
import com.ic.api.auth.service.AuthService;
import com.ic.common.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 인증 관련 웹 컨트롤러 (로그인/회원가입)
 */
@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthService authService;

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        model.addAttribute("loginRequest", new LoginRequest("", ""));
        return "auth/login";
    }

    /**
     * 로그인 처리 - 실제로는 Spring Security의 formLogin이 가로채서 처리됩니다.
     * 이 메서드는 Spring Security 인증이 실패한 경우에만 호출됩니다.
     */
    @PostMapping("/login")
    public String loginFallback(
            @Valid @ModelAttribute LoginRequest loginRequest,
            BindingResult bindingResult,
            Model model) {

        // Spring Security가 인증을 처리하므로, 여기 도달했다는 것은 인증 실패를 의미
        log.warn("로그인 실패 - Spring Security 인증 실패: {}", loginRequest.email());
        model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        return "auth/login";
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest("", "", ""));
        return "auth/signup";
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute SignupRequest signupRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.debug("회원가입 요청: {}", signupRequest.email());

        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            log.debug("회원가입 유효성 검사 실패: {}", bindingResult.getAllErrors());
            return "auth/signup";
        }

        try {
            authService.signup(signupRequest);
            log.info("회원가입 성공: {}", signupRequest.email());
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/auth/login";

        } catch (BusinessException e) {
            log.warn("회원가입 실패: {} - {}", signupRequest.email(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "auth/signup";

        } catch (Exception e) {
            log.error("회원가입 중 예상치 못한 오류: {}", signupRequest.email(), e);
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "auth/signup";
        }
    }

    /**
     * 로그아웃 성공 페이지
     */
    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/reviews?message=로그아웃되었습니다.";
    }
}