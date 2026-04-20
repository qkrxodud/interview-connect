package com.ic.api.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
        @NotNull(message = "후기 ID는 필수입니다")
        Long reviewId,

        @NotBlank(message = "댓글 내용은 필수입니다")
        String content
) {
}