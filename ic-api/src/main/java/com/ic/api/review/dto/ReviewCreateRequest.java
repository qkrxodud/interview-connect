package com.ic.api.review.dto;

import com.ic.domain.review.InterviewResult;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 면접 후기 생성 요청 DTO
 */
public record ReviewCreateRequest(
        @NotNull(message = "회사 ID는 필수입니다")
        Long companyId,

        @NotNull(message = "면접일은 필수입니다")
        @PastOrPresent(message = "면접일은 현재 또는 과거여야 합니다")
        LocalDate interviewDate,

        @NotBlank(message = "포지션은 필수입니다")
        @Size(max = 50, message = "포지션은 50자 이하로 입력해주세요")
        String position,

        @NotEmpty(message = "면접 유형은 최소 1개 이상 선택해주세요")
        List<String> interviewTypes,

        List<String> questions,

        @NotNull(message = "난이도는 필수입니다")
        @Min(value = 1, message = "난이도는 1~5 사이의 값이어야 합니다")
        @Max(value = 5, message = "난이도는 1~5 사이의 값이어야 합니다")
        Integer difficulty,

        @NotNull(message = "분위기는 필수입니다")
        @Min(value = 1, message = "분위기는 1~5 사이의 값이어야 합니다")
        @Max(value = 5, message = "분위기는 1~5 사이의 값이어야 합니다")
        Integer atmosphere,

        @NotNull(message = "면접 결과는 필수입니다")
        InterviewResult result,

        @Size(max = 3000, message = "후기 내용은 3000자 이하로 입력해주세요")
        String content
) {}