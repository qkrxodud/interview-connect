package com.ic.domain.fixture;

import com.ic.domain.company.Company;
import com.ic.domain.member.Member;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * InterviewReview 엔티티 테스트 픽스처
 */
public class InterviewReviewFixture {

    public static InterviewReview 백엔드_합격_후기() {
        return InterviewReview.create(
                MemberFixture.인증회원(),
                CompanyFixture.카카오(),
                LocalDate.of(2024, 1, 15),
                "백엔드 개발자",
                Arrays.asList("기술 면접", "인성 면접"),
                Arrays.asList("자기소개를 해주세요", "프로젝트 경험을 말해주세요"),
                3,
                4,
                InterviewResult.PASS,
                "전반적으로 좋은 분위기의 면접이었습니다."
        );
    }

    public static InterviewReview 프론트엔드_불합격_후기() {
        return InterviewReview.create(
                MemberFixture.인증회원(),
                CompanyFixture.네이버(),
                LocalDate.of(2024, 1, 20),
                "프론트엔드 개발자",
                Arrays.asList("코딩 테스트", "기술 면접"),
                Arrays.asList("리액트 경험은?", "상태관리는 어떻게?"),
                5,
                3,
                InterviewResult.FAIL,
                "어려운 문제들이 많았습니다."
        );
    }

    public static InterviewReview 대기중_후기() {
        return InterviewReview.create(
                MemberFixture.인증회원(),
                CompanyFixture.삼성전자(),
                LocalDate.of(2024, 1, 25),
                "하드웨어 엔지니어",
                Arrays.asList("기술 면접"),
                Arrays.asList("하드웨어 설계 경험은?"),
                4,
                4,
                InterviewResult.PENDING,
                "결과를 기다리는 중입니다."
        );
    }

    public static InterviewReview 후기_생성(Member 회원, Company 회사, String 포지션,
                                     int 난이도, int 분위기, InterviewResult 결과) {
        return InterviewReview.create(
                회원,
                회사,
                LocalDate.of(2024, 1, 15),
                포지션,
                Arrays.asList("기술 면접"),
                Arrays.asList("기본 질문"),
                난이도,
                분위기,
                결과,
                "테스트용 후기 내용"
        );
    }

    public static InterviewReview 상세_후기_생성(Member 회원, Company 회사, LocalDate 면접날짜,
                                        String 포지션, List<String> 면접유형, List<String> 질문목록,
                                        int 난이도, int 분위기, InterviewResult 결과, String 내용) {
        return InterviewReview.create(
                회원, 회사, 면접날짜, 포지션, 면접유형, 질문목록, 난이도, 분위기, 결과, 내용
        );
    }

    public static InterviewReview createReview() {
        return InterviewReview.create(
                MemberFixture.createVerifiedMember(),
                CompanyFixture.카카오(),
                LocalDate.of(2024, 1, 15),
                "백엔드 개발자",
                Arrays.asList("기술 면접", "인성 면접"),
                Arrays.asList("자기소개를 해주세요", "프로젝트 경험을 말해주세요"),
                3,
                4,
                InterviewResult.PASS,
                "전반적으로 좋은 분위기의 면접이었습니다."
        );
    }
}