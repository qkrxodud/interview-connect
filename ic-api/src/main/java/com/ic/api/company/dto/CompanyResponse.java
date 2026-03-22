package com.ic.api.company.dto;

import com.ic.domain.company.Company;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 회사 정보 응답 DTO
 */
@Builder
public record CompanyResponse(
        Long id,
        String name,
        String industry,
        String logoUrl,
        String website,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CompanyResponse from(final Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry())
                .logoUrl(company.getLogoUrl())
                .website(company.getWebsite())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}