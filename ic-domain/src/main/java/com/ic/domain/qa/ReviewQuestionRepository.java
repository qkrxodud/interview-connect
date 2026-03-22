package com.ic.domain.qa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewQuestionRepository extends JpaRepository<ReviewQuestion, Long> {

    @Query("SELECT q FROM ReviewQuestion q JOIN FETCH q.questioner WHERE q.interviewReview.id = :reviewId ORDER BY q.createdAt DESC")
    List<ReviewQuestion> findByInterviewReviewIdWithQuestioner(@Param("reviewId") Long reviewId);

    @Query("SELECT q FROM ReviewQuestion q JOIN FETCH q.questioner WHERE q.interviewReview.id = :reviewId ORDER BY q.createdAt DESC")
    Page<ReviewQuestion> findByInterviewReviewIdWithQuestioner(@Param("reviewId") Long reviewId, Pageable pageable);

    Long countByInterviewReviewId(Long reviewId);

    @Query("SELECT q FROM ReviewQuestion q JOIN FETCH q.interviewReview WHERE q.questioner.id = :questionerId ORDER BY q.createdAt DESC")
    Page<ReviewQuestion> findByQuestionerIdWithInterviewReview(@Param("questionerId") Long questionerId, Pageable pageable);
}