package com.ic.domain.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewReviewRepository extends JpaRepository<InterviewReview, Long> {

    Page<InterviewReview> findByCompanyId(Long companyId, Pageable pageable);

    Page<InterviewReview> findByPosition(String position, Pageable pageable);

    Page<InterviewReview> findByDifficulty(int difficulty, Pageable pageable);

    Page<InterviewReview> findByResult(InterviewResult result, Pageable pageable);

    Page<InterviewReview> findByCompanyIdAndPosition(Long companyId, String position, Pageable pageable);

    Page<InterviewReview> findByCompanyIdAndDifficulty(Long companyId, int difficulty, Pageable pageable);


    Page<InterviewReview> findByPositionAndDifficulty(String position, int difficulty, Pageable pageable);

    @Query("SELECT r FROM InterviewReview r WHERE " +
           "(:companyId IS NULL OR r.company.id = :companyId) AND " +
           "(:position IS NULL OR r.position = :position) AND " +
           "(:difficulty IS NULL OR r.difficulty = :difficulty) AND " +
           "(:result IS NULL OR r.result = :result)")
    Page<InterviewReview> findByAllFilters(@Param("companyId") Long companyId,
                                         @Param("position") String position,
                                         @Param("difficulty") Integer difficulty,
                                         @Param("result") InterviewResult result,
                                         Pageable pageable);

    List<InterviewReview> findByMemberId(Long memberId);
}