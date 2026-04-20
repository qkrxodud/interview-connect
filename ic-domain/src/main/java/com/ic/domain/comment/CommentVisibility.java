package com.ic.domain.comment;

/**
 * 댓글 가시성 설정
 */
public enum CommentVisibility {

    PUBLIC("전체 공개"),
    AUTHOR_ONLY("작성자 전용");

    private final String description;

    CommentVisibility(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }

    public boolean isAuthorOnly() {
        return this == AUTHOR_ONLY;
    }
}