package com.ic.api.review.dto;

import com.ic.domain.review.InterviewResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 면접 후기 수정 요청 DTO
 */
public record ReviewUpdateRequest(
        @Size(max = 50, message = "포지션은 50자 이하로 입력해주세요")
        String position,

        List<String> interviewTypes,

        List<String> questions,

        @Min(value = 1, message = "난이도는 1~5 사이의 값이어야 합니다")
        @Max(value = 5, message = "난이도는 1~5 사이의 값이어야 합니다")
        Integer difficulty,

        @Min(value = 1, message = "분위기는 1~5 사이의 값이어야 합니다")
        @Max(value = 5, message = "분위기는 1~5 사이의 값이어야 합니다")
        Integer atmosphere,

        InterviewResult result,

        @Size(max = 3000, message = "후기 내용은 3000자 이하로 입력해주세요")
        String content
) {}