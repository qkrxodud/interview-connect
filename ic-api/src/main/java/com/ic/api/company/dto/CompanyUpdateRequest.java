package com.ic.api.company.dto;

import jakarta.validation.constraints.Size;

/**
 * 회사 정보 수정 요청 DTO
 */
public record CompanyUpdateRequest(
        @Size(max = 100, message = "회사명은 100자 이하여야 합니다")
        String name,

        @Size(max = 50, message = "업계는 50자 이하여야 합니다")
        String industry,

        @Size(max = 500, message = "로고 URL은 500자 이하여야 합니다")
        String logoUrl,

        @Size(max = 500, message = "웹사이트 URL은 500자 이하여야 합니다")
        String website
) {
}