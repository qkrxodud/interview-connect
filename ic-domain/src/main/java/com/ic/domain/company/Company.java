package com.ic.domain.company;

import com.ic.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회사 엔티티
 * 면접 후기가 작성될 수 있는 회사 정보를 관리한다
 */
@Entity
@Table(name = "company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseTimeEntity {

    /**
     * 회사 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회사명 (유니크)
     */
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    /**
     * 업계 (IT, 금융, 제조 등)
     */
    @Column(length = 50)
    private String industry;

    /**
     * 로고 URL
     */
    @Column(length = 500)
    private String logoUrl;

    /**
     * 회사 웹사이트 URL
     */
    @Column(length = 500)
    private String website;

    @Builder
    private Company(Long id, String name, String industry, String logoUrl, String website) {
        this.id = id;
        this.name = name;
        this.industry = industry;
        this.logoUrl = logoUrl;
        this.website = website;
    }

    public static Company from(String name, String industry, String logoUrl, String website) {
        return Company.builder()
                .name(name)
                .industry(industry)
                .logoUrl(logoUrl)
                .website(website)
                .build();
    }

    /**
     * 회사명 변경
     */
    public void changeName(final String name) {
        this.name = name;
    }

    /**
     * 업계 변경
     */
    public void changeIndustry(final String industry) {
        this.industry = industry;
    }

    /**
     * 로고 URL 변경
     */
    public void changeLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

    /**
     * 웹사이트 URL 변경
     */
    public void changeWebsite(final String website) {
        this.website = website;
    }
}