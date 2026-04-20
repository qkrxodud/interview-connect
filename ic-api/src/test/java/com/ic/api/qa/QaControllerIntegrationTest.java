package com.ic.api.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ic.api.integration.config.IntegrationTestFakesConfig;
import com.ic.api.integration.config.TestApplicationConfig;
import com.ic.api.integration.config.TestSecurityConfig;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;
import com.ic.domain.qa.ReviewQuestion;
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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({IntegrationTestFakesConfig.class, TestApplicationConfig.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
@DisplayName("Q&A 컨트롤러 통합 테스트")
class QaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationTestFakesConfig fakesConfig;

    private Member 후기_작성자;
    private Member 질문자;
    private Member 답변자;
    private InterviewReview 면접_후기;

    @BeforeEach
    void setUp() {
        fakesConfig.resetAllFakes();

        // 후기 작성자 (ID=1)
        후기_작성자 = Member.builder()
                .id(1L)
                .email("author@example.com")
                .password("password123!")
                .nickname("후기작성자")
                .role(MemberRole.VERIFIED)
                .emailVerified(true)
                .build();
        fakesConfig.getMemberRepository().save(후기_작성자);

        // 질문자 일반 회원 (ID=2)
        질문자 = Member.builder()
                .id(2L)
                .email("questioner@example.com")
                .password("password123!")
                .nickname("질문자")
                .role(MemberRole.GENERAL)
                .emailVerified(true)
                .build();
        fakesConfig.getMemberRepository().save(질문자);

        // 답변자 인증 회원 (ID=3)
        답변자 = Member.builder()
                .id(3L)
                .email("answerer@example.com")
                .password("password123!")
                .nickname("답변자")
                .role(MemberRole.VERIFIED)
                .emailVerified(true)
                .build();
        fakesConfig.getMemberRepository().save(답변자);

        // 테스트 회사 생성
        final var 회사 = fakesConfig.getCompanyRepository().createTestCompany("카카오", "IT");

        // 테스트 후기 생성 (ID=1)
        면접_후기 = InterviewReview.builder()
                .id(1L)
                .member(후기_작성자)
                .company(회사)
                .interviewDate(LocalDate.of(2024, 1, 15))
                .position("백엔드 개발자")
                .interviewTypes(List.of("기술 면접"))
                .questions(List.of("자기소개를 해주세요"))
                .difficulty(3)
                .atmosphere(4)
                .result(InterviewResult.PASS)
                .content("좋은 면접이었습니다.")
                .viewCount(0L)
                .build();
        fakesConfig.getInterviewReviewRepository().save(면접_후기);
    }

    // ======= Q&A 목록 조회 =======

    @Test
    @DisplayName("비로그인 사용자가 Q&A 목록 조회 시 정상 응답과 함께 답변이 블러 처리된다")
    void 비로그인_QA_조회_성공() throws Exception {
        // given — 질문과 답변 사전 등록
        final ReviewQuestion 질문 = ReviewQuestion.create(면접_후기, 질문자, "면접 준비는 얼마나 하셨나요?");
        fakesConfig.getReviewQuestionRepository().save(질문);

        // when & then
        mockMvc.perform(get("/api/v1/reviews/{reviewId}/qa", 면접_후기.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questions").isArray())
                .andExpect(jsonPath("$.data.totalQuestions").value(1));
    }

    @Test
    @WithMockUser(username = "2")
    @DisplayName("로그인 사용자가 Q&A 목록 조회 시 정상 응답이 반환된다")
    void 로그인_QA_조회_성공() throws Exception {
        // given
        final ReviewQuestion 질문 = ReviewQuestion.create(면접_후기, 질문자, "합격 후 처우 협상은 어떻게 하셨나요?");
        fakesConfig.getReviewQuestionRepository().save(질문);

        // when & then
        mockMvc.perform(get("/api/v1/reviews/{reviewId}/qa", 면접_후기.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questions").isArray())
                .andExpect(jsonPath("$.data.questions[0].content").value("합격 후 처우 협상은 어떻게 하셨나요?"));
    }

    @Test
    @DisplayName("존재하지 않는 후기의 Q&A 조회 시 에러 응답이 반환된다")
    void 존재하지_않는_후기_QA_조회_에러() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/{reviewId}/qa", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ======= 질문 작성 =======

    @Test
    @WithMockUser(username = "2")
    @DisplayName("로그인 사용자가 질문을 작성하면 201 응답과 함께 질문 정보가 반환된다")
    void 질문_작성_성공() throws Exception {
        // given
        final Map<String, Object> 요청_본문 = Map.of(
                "reviewId", 면접_후기.getId(),
                "content", "기술 면접 문제가 어려웠나요?"
        );

        // when & then
        mockMvc.perform(post("/api/v1/reviews/{reviewId}/questions", 면접_후기.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(요청_본문)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("기술 면접 문제가 어려웠나요?"))
                .andExpect(jsonPath("$.data.questionerNickname").value("질문자"));
    }

    @Test
    @DisplayName("비로그인 사용자가 질문 작성 시 401 응답이 반환된다")
    void 비로그인_질문_작성_시_401() throws Exception {
        // given
        final Map<String, Object> 요청_본문 = Map.of(
                "reviewId", 면접_후기.getId(),
                "content", "비로그인 질문 시도"
        );

        // when & then
        mockMvc.perform(post("/api/v1/reviews/{reviewId}/questions", 면접_후기.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(요청_본문)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ======= 답변 작성 =======

    @Test
    @WithMockUser(username = "3")
    @DisplayName("인증 회원이 타인의 질문에 답변을 작성하면 정상 응답이 반환된다")
    void 답변_작성_성공() throws Exception {
        // given — 사전에 질문 등록
        final ReviewQuestion 질문 = ReviewQuestion.create(면접_후기, 질문자, "면접관이 몇 명이었나요?");
        final ReviewQuestion 저장된_질문 = fakesConfig.getReviewQuestionRepository().save(질문);

        final Map<String, Object> 요청_본문 = Map.of(
                "questionId", 저장된_질문.getId(),
                "content", "면접관이 3명이었습니다."
        );

        // when & then
        mockMvc.perform(post("/api/v1/questions/{questionId}/answers", 저장된_질문.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(요청_본문)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("면접관이 3명이었습니다."))
                .andExpect(jsonPath("$.data.answererNickname").value("답변자"));
    }

    @Test
    @DisplayName("비로그인 사용자가 답변 작성 시 401 응답이 반환된다")
    void 비로그인_답변_작성_시_401() throws Exception {
        // given
        final ReviewQuestion 질문 = ReviewQuestion.create(면접_후기, 질문자, "테스트 질문");
        final ReviewQuestion 저장된_질문 = fakesConfig.getReviewQuestionRepository().save(질문);

        final Map<String, Object> 요청_본문 = Map.of(
                "questionId", 저장된_질문.getId(),
                "content", "비로그인 답변 시도"
        );

        // when & then
        mockMvc.perform(post("/api/v1/questions/{questionId}/answers", 저장된_질문.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(요청_본문)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ======= 답변 수정 =======

    @Test
    @WithMockUser(username = "3")
    @DisplayName("답변 작성자가 자신의 답변을 수정하면 정상 응답이 반환된다")
    void 답변_수정_성공() throws Exception {
        // given — 질문과 답변 사전 등록
        final ReviewQuestion 질문 = ReviewQuestion.create(면접_후기, 질문자, "수정 테스트 질문");
        final ReviewQuestion 저장된_질문 = fakesConfig.getReviewQuestionRepository().save(질문);

        // 답변 사전 생성
        final Map<String, Object> 답변_생성_요청 = Map.of(
                "questionId", 저장된_질문.getId(),
                "content", "원본 답변 내용"
        );
        final var 답변_생성_결과 = mockMvc.perform(post("/api/v1/questions/{questionId}/answers", 저장된_질문.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(답변_생성_요청)))
                .andReturn();
        final Long 답변_ID = objectMapper.readTree(
                답변_생성_결과.getResponse().getContentAsString()
        ).get("data").get("id").asLong();

        final Map<String, Object> 수정_요청_본문 = Map.of(
                "answerId", 답변_ID,
                "content", "수정된 답변 내용"
        );

        // when & then
        mockMvc.perform(patch("/api/v1/answers/{answerId}", 답변_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(수정_요청_본문)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("수정된 답변 내용"));
    }
}
