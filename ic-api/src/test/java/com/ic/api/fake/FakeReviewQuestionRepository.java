package com.ic.api.fake;

import com.ic.domain.qa.ReviewQuestion;
import com.ic.domain.qa.ReviewQuestionRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

/**
 * ReviewQuestionRepository의 메모리 기반 Fake 구현체
 * - JPA 없이 빠른 테스트 실행
 * - 실제 동작과 유사한 구현
 * - 상태 기반 검증
 */
public class FakeReviewQuestionRepository implements ReviewQuestionRepository {

    private final Map<Long, ReviewQuestion> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public ReviewQuestion save(ReviewQuestion question) {
        if (question.getId() == null) {
            // 새로운 질문 등록 — 리플렉션으로 ID 설정
            final Long newId = sequence.getAndIncrement();
            try {
                final java.lang.reflect.Field idField = ReviewQuestion.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(question, newId);
            } catch (Exception e) {
                throw new RuntimeException("ID 설정 실패", e);
            }
            store.put(newId, question);
            return question;
        }
        // 기존 질문 업데이트
        store.put(question.getId(), question);
        return question;
    }

    @Override
    public Optional<ReviewQuestion> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * 후기 ID로 질문 조회 (테스트용)
     */
    public List<ReviewQuestion> findByReviewId(Long reviewId) {
        return store.values()
                .stream()
                .filter(question -> Objects.equals(question.getReview().getId(), reviewId))
                .collect(Collectors.toList());
    }

    /**
     * 작성자 ID로 질문 조회 (테스트용)
     */
    public List<ReviewQuestion> findByMemberId(Long memberId) {
        return store.values()
                .stream()
                .filter(question -> Objects.equals(question.getMember().getId(), memberId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public List<ReviewQuestion> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<ReviewQuestion> findAllById(Iterable<Long> ids) {
        final List<ReviewQuestion> result = new ArrayList<>();
        for (Long id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public long count() {
        return store.size();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public void delete(ReviewQuestion question) {
        store.remove(question.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            store.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends ReviewQuestion> entities) {
        for (ReviewQuestion question : entities) {
            store.remove(question.getId());
        }
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    // === 테스트 헬퍼 메서드들 ===

    /**
     * 저장소 초기화
     */
    public void clear() {
        store.clear();
        sequence.set(1L);
    }

    /**
     * 저장된 질문 수 조회
     */
    public int size() {
        return store.size();
    }

    /**
     * 특정 질문의 존재 여부 확인
     */
    public boolean hasQuestion(Long id) {
        return store.containsKey(id);
    }

    /**
     * 특정 후기의 질문 수 조회 (테스트용)
     */
    public long countByReviewId(Long reviewId) {
        return findByReviewId(reviewId).size();
    }

    /**
     * 특정 회원의 질문 수 조회 (테스트용)
     */
    public long countByMemberId(Long memberId) {
        return findByMemberId(memberId).size();
    }

    /**
     * 모든 질문 조회 (테스트용)
     */
    public Map<Long, ReviewQuestion> findAllAsMap() {
        return new HashMap<>(store);
    }

    // === 리포지토리 인터페이스 구현 메서드들 ===

    @Override
    public List<ReviewQuestion> findByInterviewReviewIdWithQuestioner(Long reviewId) {
        return store.values()
                .stream()
                .filter(question -> Objects.equals(question.getInterviewReview().getId(), reviewId))
                .sorted((q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt())) // 최신순
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewQuestion> findByInterviewReviewIdWithQuestioner(Long reviewId, Pageable pageable) {
        final List<ReviewQuestion> questions = findByInterviewReviewIdWithQuestioner(reviewId);
        final int start = (int) pageable.getOffset();
        final int end = Math.min(start + pageable.getPageSize(), questions.size());

        if (start > questions.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, questions.size());
        }

        return new PageImpl<>(questions.subList(start, end), pageable, questions.size());
    }

    @Override
    public Long countByInterviewReviewId(Long reviewId) {
        return store.values()
                .stream()
                .mapToLong(question -> Objects.equals(question.getInterviewReview().getId(), reviewId) ? 1L : 0L)
                .sum();
    }

    @Override
    public Page<ReviewQuestion> findByQuestionerIdWithInterviewReview(Long questionerId, Pageable pageable) {
        final List<ReviewQuestion> questions = store.values()
                .stream()
                .filter(question -> Objects.equals(question.getQuestioner().getId(), questionerId))
                .sorted((q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt())) // 최신순
                .collect(Collectors.toList());

        final int start = (int) pageable.getOffset();
        final int end = Math.min(start + pageable.getPageSize(), questions.size());

        if (start > questions.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, questions.size());
        }

        return new PageImpl<>(questions.subList(start, end), pageable, questions.size());
    }

    // === JpaRepository 미사용 메서드들 ===

    @Override
    public void flush() {
        // 인메모리 저장소이므로 flush 불필요
    }

    @Override
    public <S extends ReviewQuestion> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends ReviewQuestion> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<ReviewQuestion> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        deleteAllById(ids);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public ReviewQuestion getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public ReviewQuestion getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public ReviewQuestion getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends ReviewQuestion> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewQuestion> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewQuestion> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewQuestion> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewQuestion> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewQuestion> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewQuestion, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery는 지원하지 않습니다");
    }

    @Override
    public List<ReviewQuestion> findAll(Sort sort) {
        return findAll(); // 정렬 무시하고 전체 조회
    }

    @Override
    public Page<ReviewQuestion> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewQuestion> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }
}