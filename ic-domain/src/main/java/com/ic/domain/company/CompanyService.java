package com.ic.domain.company;

import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 회사 도메인 서비스
 * 회사 관련 비즈니스 로직을 처리한다
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * 회사 등록
     * 관리자만 회사를 등록할 수 있다
     */
    @Transactional
    public Company createCompany(final String name, final String industry,
                                final String logoUrl, final String website) {
        validateCompanyName(name);
        validateDuplicateName(name);

        final Company company = Company.from(name, industry, logoUrl, website);
        return companyRepository.save(company);
    }

    /**
     * 회사명으로 검색 (자동완성)
     * 최대 10건까지 반환
     */
    public List<Company> searchCompaniesByName(final String keyword) {
        if (Objects.isNull(keyword) || keyword.trim().isEmpty()) {
            return List.of();
        }

        return companyRepository.findTop10ByNameContainingIgnoreCase(keyword.trim());
    }

    /**
     * 회사 상세 조회
     */
    public Company getCompanyById(final Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.COMPANY_NOT_FOUND));
    }

    /**
     * 모든 회사 조회
     */
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    /**
     * 회사 정보 수정
     * 관리자만 회사 정보를 수정할 수 있다
     */
    @Transactional
    public Company updateCompany(final Long companyId, final String name,
                                final String industry, final String logoUrl, final String website) {
        final Company company = getCompanyById(companyId);

        if (Objects.nonNull(name) && !name.equals(company.getName())) {
            validateCompanyName(name);
            validateDuplicateName(name);
            company.changeName(name);
        }

        if (Objects.nonNull(industry)) {
            company.changeIndustry(industry);
        }

        if (Objects.nonNull(logoUrl)) {
            company.changeLogoUrl(logoUrl);
        }

        if (Objects.nonNull(website)) {
            company.changeWebsite(website);
        }

        return company;
    }

    /**
     * 회사 삭제
     * 관리자만 회사를 삭제할 수 있다
     */
    @Transactional
    public void deleteCompany(final Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw BusinessException.from(ErrorCode.COMPANY_NOT_FOUND);
        }

        companyRepository.deleteById(companyId);
    }

    /**
     * 회사명 유효성 검증
     */
    private void validateCompanyName(final String name) {
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 회사명 중복 검증
     */
    private void validateDuplicateName(final String name) {
        if (companyRepository.existsByName(name)) {
            throw BusinessException.from(ErrorCode.DUPLICATE_COMPANY_NAME);
        }
    }
}