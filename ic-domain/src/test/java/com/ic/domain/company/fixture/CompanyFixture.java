package com.ic.domain.company.fixture;

import com.ic.domain.company.Company;
import com.github.javafaker.Faker;

import java.util.Locale;

/**
 * Company 테스트 픽스처
 */
public class CompanyFixture {

    private static final Faker faker = new Faker(new Locale("ko"));

    public static Company 카카오() {
        return Company.builder()
                .id(1L)
                .name("카카오")
                .industry("IT서비스")
                .logoUrl("https://logo.kakao.com")
                .website("https://www.kakaocorp.com")
                .build();
    }

    public static Company 네이버() {
        return Company.builder()
                .id(2L)
                .name("네이버")
                .industry("IT서비스")
                .logoUrl("https://logo.naver.com")
                .website("https://www.navercorp.com")
                .build();
    }

    public static Company 쿠팡() {
        return Company.builder()
                .id(3L)
                .name("쿠팡")
                .industry("이커머스")
                .logoUrl("https://logo.coupang.com")
                .website("https://www.coupang.com")
                .build();
    }

    public static Company 기본회사() {
        return Company.from(
                faker.company().name(),
                "IT서비스",
                "https://example.com/logo.png",
                "https://example.com"
        );
    }

    public static CompanyBuilder builder() {
        return new CompanyBuilder();
    }

    public static class CompanyBuilder {
        private String name = faker.company().name();
        private String industry = "IT서비스";
        private String logoUrl = "https://example.com/logo.png";
        private String website = "https://example.com";

        public CompanyBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CompanyBuilder industry(String industry) {
            this.industry = industry;
            return this;
        }

        public CompanyBuilder logoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        public CompanyBuilder website(String website) {
            this.website = website;
            return this;
        }

        public Company build() {
            return Company.from(name, industry, logoUrl, website);
        }
    }
}