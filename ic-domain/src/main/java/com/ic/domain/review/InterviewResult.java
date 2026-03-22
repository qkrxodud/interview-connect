package com.ic.domain.review;

public enum InterviewResult {
    PASS("합격"),
    FAIL("불합격"),
    PENDING("결과 대기");

    private final String description;

    InterviewResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isPass() {
        return this == PASS;
    }

    public boolean isFail() {
        return this == FAIL;
    }
}