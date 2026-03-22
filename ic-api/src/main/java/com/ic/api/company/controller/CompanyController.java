package com.ic.api.company.controller;

import com.ic.api.company.dto.*;
import com.ic.api.config.security.AuthMember;
import com.ic.api.config.security.CustomUserDetails;
import com.ic.common.response.ApiResponse;
import com.ic.domain.company.Company;
import com.ic.domain.company.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 회사 관련 API 컨트롤러
 * 회사 조회, 검색, 등록, 수정, 삭제 기능을 제공한다
 */
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * 회사 검색 (자동완성)
     * 비로그인 사용자도 접근 가능
     */
    @GetMapping
    public ApiResponse<List<CompanySearchResponse>> searchCompanies(
            @RequestParam(name = "q", required = false, defaultValue = "") String keyword
    ) {
        final List<Company> companies = companyService.searchCompaniesByName(keyword);
        final List<CompanySearchResponse> responses = companies.stream()
                .map(CompanySearchResponse::from)
                .toList();

        return ApiResponse.ok(responses);
    }

    /**
     * 회사 상세 조회
     * 비로그인 사용자도 접근 가능
     */
    @GetMapping("/{companyId}")
    public ApiResponse<CompanyResponse> getCompany(@PathVariable final Long companyId) {
        final Company company = companyService.getCompanyById(companyId);
        final CompanyResponse response = CompanyResponse.from(company);

        return ApiResponse.ok(response);
    }

    /**
     * 모든 회사 목록 조회
     * 비로그인 사용자도 접근 가능
     */
    @GetMapping("/all")
    public ApiResponse<List<CompanyResponse>> getAllCompanies() {
        final List<Company> companies = companyService.getAllCompanies();
        final List<CompanyResponse> responses = companies.stream()
                .map(CompanyResponse::from)
                .toList();

        return ApiResponse.ok(responses);
    }

    /**
     * 회사 등록
     * 관리자만 접근 가능
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompanyResponse> createCompany(
            @Valid @RequestBody final CompanyCreateRequest request,
            @AuthMember final CustomUserDetails userDetails
    ) {
        final Company company = companyService.createCompany(
                request.name(),
                request.industry(),
                request.logoUrl(),
                request.website()
        );

        final CompanyResponse response = CompanyResponse.from(company);
        return ApiResponse.ok(response);
    }

    /**
     * 회사 정보 수정
     * 관리자만 접근 가능
     */
    @PutMapping("/{companyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompanyResponse> updateCompany(
            @PathVariable final Long companyId,
            @Valid @RequestBody final CompanyUpdateRequest request,
            @AuthMember final CustomUserDetails userDetails
    ) {
        final Company company = companyService.updateCompany(
                companyId,
                request.name(),
                request.industry(),
                request.logoUrl(),
                request.website()
        );

        final CompanyResponse response = CompanyResponse.from(company);
        return ApiResponse.ok(response);
    }

    /**
     * 회사 삭제
     * 관리자만 접근 가능
     */
    @DeleteMapping("/{companyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCompany(
            @PathVariable final Long companyId,
            @AuthMember final CustomUserDetails userDetails
    ) {
        companyService.deleteCompany(companyId);
        return ApiResponse.ok(null);
    }
}