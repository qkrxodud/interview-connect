package com.ic.api.company.dto;

import com.ic.domain.company.Company;
import lombok.Builder;

/**
 * 회사 검색 응답 DTO (자동완성용)
 * 필요한 정보만 간소화하여 전송
 */
@Builder
public record CompanySearchResponse(
        Long id,
        String name,
        String industry,
        String logoUrl
) {

    public static CompanySearchResponse from(final Company company) {
        return CompanySearchResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry())
                .logoUrl(company.getLogoUrl())
                .build();
    }
}