package com.ic.domain.fixture;

import com.ic.domain.company.Company;

/**
 * Company 엔티티 테스트 픽스처
 */
public class CompanyFixture {

    public static Company 카카오() {
        return Company.builder()
                .name("카카오")
                .industry("IT")
                .logoUrl("https://kakao.com/logo.png")
                .website("https://kakao.com")
                .build();
    }

    public static Company 네이버() {
        return Company.builder()
                .name("네이버")
                .industry("IT")
                .logoUrl("https://naver.com/logo.png")
                .website("https://naver.com")
                .build();
    }

    public static Company 삼성전자() {
        return Company.builder()
                .name("삼성전자")
                .industry("제조")
                .logoUrl("https://samsung.com/logo.png")
                .website("https://samsung.com")
                .build();
    }

    public static Company 회사_생성(String name, String industry) {
        return Company.builder()
                .name(name)
                .industry(industry)
                .build();
    }

    public static Company ID가_있는_카카오(Long id) {
        return Company.builder()
                .id(id)
                .name("카카오")
                .industry("IT")
                .logoUrl("https://kakao.com/logo.png")
                .website("https://kakao.com")
                .build();
    }
}