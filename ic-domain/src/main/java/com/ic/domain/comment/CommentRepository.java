package com.ic.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.author " +
            "WHERE c.interviewReview.id = :reviewId AND c.deleted = false " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findByInterviewReviewIdAndDeletedFalseOrderByCreatedAtAsc(@Param("reviewId") Long reviewId);

    @Query(value = "SELECT c FROM Comment c " +
                   "JOIN FETCH c.author " +
                   "WHERE c.interviewReview.id = :reviewId AND c.deleted = false " +
                   "ORDER BY c.createdAt ASC",
           countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.interviewReview.id = :reviewId AND c.deleted = false")
    Page<Comment> findByInterviewReviewIdAndDeletedFalseOrderByCreatedAtAsc(@Param("reviewId") Long reviewId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.interviewReview.id = :reviewId AND c.deleted = false")
    long countByInterviewReviewIdAndDeletedFalse(@Param("reviewId") Long reviewId);

    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.author " +
           "WHERE c.interviewReview.id = :reviewId AND c.deleted = false AND c.parent IS NULL " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByReviewId(@Param("reviewId") Long reviewId);
}