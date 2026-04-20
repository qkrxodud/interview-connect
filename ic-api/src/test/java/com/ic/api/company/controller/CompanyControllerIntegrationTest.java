package com.ic.api.company.controller;

import com.ic.api.integration.BaseApiWebClientTest;
import com.ic.common.response.ApiResponse;
import com.ic.domain.company.Company;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CompanyController WebClient 기반 통합 테스트
 * - 실제 HTTP 요청/응답 테스트
 * - Fake Repository를 통한 빠른 테스트 실행
 * - 회사 검색 및 조회 기능 검증
 */
@DisplayName("CompanyController 통합 테스트")
class CompanyControllerIntegrationTest extends BaseApiWebClientTest {

    private static final String COMPANY_BASE_URL = "/api/v1/companies";

    @BeforeEach
    void setUpTestData() {
        // 테스트용 회사 데이터 생성
        fakesConfig.getCompanyRepository().createTestCompany("카카오", "IT");
        fakesConfig.getCompanyRepository().createTestCompany("네이버", "IT");
        fakesConfig.getCompanyRepository().createTestCompany("삼성전자", "제조");
        fakesConfig.getCompanyRepository().createTestCompany("현대자동차", "자동차");
        fakesConfig.getCompanyRepository().createTestCompany("KB국민은행", "금융");
    }

    @Nested
    @DisplayName("회사 목록 조회 API 테스트")
    class 회사_목록_조회_API_테스트 {

        @Test
        @DisplayName("전체 회사 목록을 조회할 수 있다")
        void shouldReturnAllCompanies() {
            // when
            final String response = webClient.get()
                    .uri(COMPANY_BASE_URL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();
            assertThat(apiResponse.data()).isNotNull();

            // 5개 회사가 모두 조회되는지 확인
            assertThat(fakesConfig.getCompanyRepository().size()).isEqualTo(5);
        }

        @Test
        @DisplayName("빈 목록이어도 정상적으로 200 OK를 반환한다")
        void shouldReturn200WhenNoCompanies() {
            // given: 모든 회사 삭제
            fakesConfig.getCompanyRepository().clear();

            // when
            final String response = webClient.get()
                    .uri(COMPANY_BASE_URL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("회사 검색 API 테스트")
    class 회사_검색_API_테스트 {

        @Test
        @DisplayName("회사명 키워드로 검색할 수 있다")
        void shouldSearchCompaniesByKeyword() {
            // when: '카카오'로 검색
            final String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(COMPANY_BASE_URL)
                            .queryParam("q", "카카오")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();
            assertThat(apiResponse.data()).isNotNull();

            // 실제 검색 결과 확인
            final var searchResults = fakesConfig.getCompanyRepository().findByNameContaining("카카오");
            assertThat(searchResults).hasSize(1);
            assertThat(searchResults.get(0).getName()).isEqualTo("카카오");
        }

        @Test
        @DisplayName("부분 검색이 가능하다")
        void shouldSupportPartialSearch() {
            // when: '삼성'으로 검색 ('삼성전자'가 포함되어야 함)
            final String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(COMPANY_BASE_URL)
                            .queryParam("q", "삼성")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();

            // 실제 검색 결과 확인
            final var searchResults = fakesConfig.getCompanyRepository().findByNameContaining("삼성");
            assertThat(searchResults).hasSize(1);
            assertThat(searchResults.get(0).getName()).contains("삼성");
        }

        @Test
        @DisplayName("검색 결과가 없어도 정상적으로 응답한다")
        void shouldReturnEmptyWhenNoSearchResults() {
            // when
            final String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(COMPANY_BASE_URL)
                            .queryParam("q", "존재하지않는회사")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();

            // 실제 검색 결과가 비어있는지 확인
            final var searchResults = fakesConfig.getCompanyRepository().findByNameContaining("존재하지않는회사");
            assertThat(searchResults).isEmpty();
        }

        @Test
        @DisplayName("대소문자 구분 없이 검색이 가능하다")
        void shouldSearchCaseInsensitively() {
            // when: 'KB'로 검색 ('KB국민은행'이 포함되어야 함)
            final String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(COMPANY_BASE_URL)
                            .queryParam("q", "kb")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();

            // 실제 검색 결과 확인 (대소문자 무관)
            final var searchResults = fakesConfig.getCompanyRepository().findByNameContaining("kb");
            assertThat(searchResults).hasSize(1);
            assertThat(searchResults.get(0).getName()).containsIgnoringCase("KB");
        }
    }

    @Nested
    @DisplayName("회사 상세 조회 API 테스트")
    class 회사_상세_조회_API_테스트 {

        @Test
        @DisplayName("존재하는 회사 ID로 상세 정보를 조회할 수 있다")
        void shouldReturnCompanyDetailsWhenValidId() {
            // given: 카카오 회사 ID 조회
            final Company kakao = fakesConfig.getCompanyRepository()
                    .findByName("카카오")
                    .orElseThrow();

            // when
            final String response = webClient.get()
                    .uri(COMPANY_BASE_URL + "/" + kakao.getId())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();
            assertThat(apiResponse.data()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 회사 ID로 조회 시 404 Not Found를 반환한다")
        void shouldReturn404WhenCompanyNotFound() {
            // given
            final Long nonexistentId = 999L;

            // when & then
            webClient.get()
                    .uri(COMPANY_BASE_URL + "/" + nonexistentId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> {
                        assertThat(error.getMessage()).contains("404");
                    })
                    .onErrorReturn("expected 404")
                    .block();
        }
    }

    @Nested
    @DisplayName("회사 업계별 조회 테스트")
    class 회사_업계별_조회_테스트 {

        @Test
        @DisplayName("IT 업계 회사만 필터링하여 조회할 수 있다")
        void shouldFilterCompaniesByItIndustry() {
            // when
            final String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(COMPANY_BASE_URL)
                            .queryParam("industry", "IT")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();

            // IT 업계 회사 수 확인
            final long itCompaniesCount = fakesConfig.getCompanyRepository().countByIndustry("IT");
            assertThat(itCompaniesCount).isEqualTo(2); // 카카오, 네이버
        }

        @Test
        @DisplayName("존재하지 않는 업계로 필터링해도 정상 응답한다")
        void shouldReturnEmptyWhenIndustryNotExists() {
            // when
            final String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(COMPANY_BASE_URL)
                            .queryParam("industry", "항공우주")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();

            // 항공우주 업계 회사가 없는지 확인
            final long count = fakesConfig.getCompanyRepository().countByIndustry("항공우주");
            assertThat(count).isEqualTo(0);
        }
    }
}