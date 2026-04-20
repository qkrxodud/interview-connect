package com.ic.domain.comment.fixture;

import com.ic.domain.comment.Comment;
import com.ic.domain.member.Member;
import com.ic.domain.review.InterviewReview;

public class CommentFixture {

    private static final String REPEATED_CHAR = "a";

    public static Comment createComment(Member author, InterviewReview review, String content) {
        return Comment.create(author, review, content);
    }

    public static String getLongContent() {
        return REPEATED_CHAR.repeat(1001);
    }

    public static String getMaxLengthContent() {
        return REPEATED_CHAR.repeat(1000);
    }
}
