package com.ic.domain.review.service;

import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.domain.company.Company;
import com.ic.domain.company.CompanyRepository;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.InterviewReviewRepository;
import com.ic.domain.review.InterviewResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 면접 후기 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final InterviewReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;

    /**
     * 후기 생성
     */
    @Transactional
    public InterviewReview createReview(Long memberId, CreateReviewRequest request) {
        final Member member = findMemberById(memberId);

        if (!member.canWriteReview()) {
            throw BusinessException.from(ErrorCode.REVIEW_PERMISSION_DENIED);
        }

        final Company company = findCompanyById(request.companyId());

        final InterviewReview review = InterviewReview.create(
                member,
                company,
                request.interviewDate(),
                request.position(),
                request.interviewTypes(),
                request.questions(),
                request.difficulty(),
                request.atmosphere(),
                request.result(),
                request.content()
        );

        return reviewRepository.save(review);
    }

    /**
     * 페이징된 후기 목록 조회
     */
    public Page<InterviewReview> getReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    /**
     * 회사별 후기 목록 조회
     */
    public Page<InterviewReview> getReviewsByCompany(Long companyId, Pageable pageable) {
        return reviewRepository.findByCompanyId(companyId, pageable);
    }

    /**
     * 후기 상세 조회 (조회수 증가)
     */
    @Transactional
    public InterviewReview getReview(Long reviewId) {
        final InterviewReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.REVIEW_NOT_FOUND));

        review.increaseViewCount();
        return reviewRepository.save(review);
    }

    /**
     * 후기 수정
     */
    @Transactional
    public InterviewReview updateReview(Long memberId, Long reviewId, UpdateReviewRequest request) {
        final InterviewReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.isWrittenBy(memberId)) {
            throw BusinessException.from(ErrorCode.REVIEW_AUTHOR_MISMATCH);
        }

        // 필드 업데이트
        if (Objects.nonNull(request.position())) {
            review.changePosition(request.position());
        }
        if (Objects.nonNull(request.interviewTypes())) {
            review.changeInterviewTypes(request.interviewTypes());
        }
        if (Objects.nonNull(request.questions())) {
            review.changeQuestions(request.questions());
        }
        review.changeDifficulty(request.difficulty());
        review.changeAtmosphere(request.atmosphere());
        review.changeResult(request.result());
        if (Objects.nonNull(request.content())) {
            review.changeContent(request.content());
        }

        return reviewRepository.save(review);
    }

    /**
     * 후기 삭제
     */
    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        final InterviewReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.isWrittenBy(memberId)) {
            throw BusinessException.from(ErrorCode.REVIEW_AUTHOR_MISMATCH);
        }

        reviewRepository.deleteById(reviewId);
    }

    /**
     * 사용자별 후기 목록 조회
     */
    public List<InterviewReview> getUserReviews(Long memberId) {
        return reviewRepository.findByMemberId(memberId);
    }

    /**
     * 멤버 조회
     */
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 회사 조회
     */
    private Company findCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.COMPANY_NOT_FOUND));
    }


    // DTO 클래스들
    public record CreateReviewRequest(
            Long companyId,
            LocalDate interviewDate,
            String position,
            List<String> interviewTypes,
            List<String> questions,
            Integer difficulty,
            Integer atmosphere,
            InterviewResult result,
            String content
    ) {}

    public record UpdateReviewRequest(
            String position,
            List<String> interviewTypes,
            List<String> questions,
            Integer difficulty,
            Integer atmosphere,
            InterviewResult result,
            String content
    ) {}
}