package com.ic.api.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ic.api.integration.config.IntegrationTestFakesConfig;
import com.ic.api.integration.config.TestApplicationConfig;
import com.ic.api.integration.config.TestSecurityConfig;
import com.ic.api.review.dto.ReviewCreateRequest;
import com.ic.api.review.dto.ReviewUpdateRequest;
import com.ic.domain.company.Company;
import com.ic.domain.member.Member;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.ic.domain.member.MemberRole.VERIFIED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 면접 후기 컨트롤러 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({IntegrationTestFakesConfig.class, TestApplicationConfig.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
@DisplayName("면접 후기 컨트롤러 통합 테스트")
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationTestFakesConfig fakesConfig;

    @BeforeEach
    void setUpTestData() {
        fakesConfig.resetAllFakes();

        // 테스트용 인증 회원 생성 (ID=1)
        final Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123!")
                .nickname("테스터")
                .role(VERIFIED)
                .emailVerified(true)
                .build();
        fakesConfig.getMemberRepository().save(member);

        // 테스트용 회사 생성 (ID=1)
        final Company company = fakesConfig.getCompanyRepository().createTestCompany("카카오", "IT");

        // 테스트용 후기 생성 (ID=1, 회원=1, 회사=1)
        final InterviewReview review = InterviewReview.builder()
                .id(1L)
                .member(member)
                .company(company)
                .interviewDate(LocalDate.of(2024, 1, 15))
                .position("백엔드 개발자")
                .interviewTypes(List.of("기술 면접", "인성 면접"))
                .questions(List.of("자기소개를 해주세요"))
                .difficulty(3)
                .atmosphere(4)
                .result(InterviewResult.PASS)
                .content("전반적으로 좋은 면접이었습니다.")
                .viewCount(0L)
                .build();
        fakesConfig.getInterviewReviewRepository().save(review);
    }

    @Test
    @DisplayName("비로그인 사용자도 후기 목록을 조회할 수 있다")
    void shouldGetReviewsWithoutLogin() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("비로그인 사용자도 특정 후기를 조회할 수 있다")
    void shouldGetReviewWithoutLogin() throws Exception {
        // given
        final Long reviewId = 1L;

        // when & then
        mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("인증된 사용자는 후기를 생성할 수 있다")
    void shouldCreateReviewWhenAuthenticated() throws Exception {
        // given
        final ReviewCreateRequest request = new ReviewCreateRequest(
                1L,
                LocalDate.now().minusDays(7),
                "백엔드 개발자",
                List.of("화상면접", "코딩테스트"),
                List.of("자기소개를 해주세요", "왜 이 회사를 지원했나요?"),
                3,
                4,
                InterviewResult.PASS,
                "전반적으로 좋은 면접 경험이었습니다."
        );

        // when & then
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.position").value("백엔드 개발자"));
    }

    @Test
    @DisplayName("비인증 사용자는 후기를 생성할 수 없다")
    void shouldNotCreateReviewWhenUnauthenticated() throws Exception {
        // given
        final ReviewCreateRequest request = new ReviewCreateRequest(
                1L,
                LocalDate.now().minusDays(7),
                "백엔드 개발자",
                List.of("화상면접"),
                List.of("질문"),
                3,
                4,
                InterviewResult.PASS,
                "후기 내용"
        );

        // when & then
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("인증된 사용자는 후기를 수정할 수 있다")
    void shouldUpdateReviewWhenAuthenticated() throws Exception {
        // given
        final Long reviewId = 1L;
        final ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정된 포지션",
                List.of("대면면접"),
                List.of("수정된 질문"),
                5,
                2,
                InterviewResult.FAIL,
                "수정된 후기 내용"
        );

        // when & then
        mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("인증된 사용자는 후기를 삭제할 수 있다")
    void shouldDeleteReviewWhenAuthenticated() throws Exception {
        // given
        final Long reviewId = 1L;

        // when & then
        mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("인증된 사용자는 자신의 후기 목록을 조회할 수 있다")
    void shouldGetMyReviewsWhenAuthenticated() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/reviews/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("잘못된 요청 데이터로 후기 생성 시 400 에러가 발생한다")
    @WithMockUser(username = "1")
    void shouldReturn400WhenInvalidRequestData() throws Exception {
        // given - 필수 필드 누락
        final ReviewCreateRequest invalidRequest = new ReviewCreateRequest(
                null, // companyId 누락
                null, // interviewDate 누락
                "", // position 빈 값
                List.of(), // interviewTypes 빈 리스트
                null,
                0, // difficulty 범위 밖
                0, // atmosphere 범위 밖
                null, // result 누락
                "a".repeat(3001) // content 길이 초과
        );

        // when & then
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
