package com.ic.domain.qa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewAnswerRepository extends JpaRepository<ReviewAnswer, Long> {

    @Query("SELECT a FROM ReviewAnswer a JOIN FETCH a.answerer WHERE a.reviewQuestion.id = :questionId ORDER BY a.createdAt ASC")
    List<ReviewAnswer> findByReviewQuestionIdWithAnswerer(@Param("questionId") Long questionId);

    boolean existsByReviewQuestionIdAndAnswererId(Long questionId, Long answererId);
}