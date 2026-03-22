package com.ic.api.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 홈 페이지 컨트롤러
 */
@Controller
public class HomeController {

    /**
     * 홈페이지 - 후기 목록으로 리다이렉트
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/reviews";
    }
}