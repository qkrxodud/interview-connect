package com.ic.domain.fake;

import com.ic.domain.qa.ReviewQuestion;
import com.ic.domain.qa.ReviewQuestionRepository;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FakeReviewQuestionRepository implements ReviewQuestionRepository {

    private final Map<Long, ReviewQuestion> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public ReviewQuestion save(ReviewQuestion question) {
        if (question.getId() == null) {
            setId(question, sequence.getAndIncrement());
        }
        store.put(question.getId(), question);
        return question;
    }

    @Override
    public Optional<ReviewQuestion> findById(Long id) {
        return Optional.ofNullable(store.get(id));
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
    public long count() {
        return store.size();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public void delete(ReviewQuestion entity) {
        store.remove(entity.getId());
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    @Override
    public List<ReviewQuestion> findByInterviewReviewIdWithQuestioner(Long reviewId) {
        return store.values().stream()
                .filter(q -> Objects.equals(q.getInterviewReview().getId(), reviewId))
                .sorted(Comparator.comparing(ReviewQuestion::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewQuestion> findByInterviewReviewIdWithQuestioner(Long reviewId, Pageable pageable) {
        final List<ReviewQuestion> all = findByInterviewReviewIdWithQuestioner(reviewId);
        final int start = (int) pageable.getOffset();
        final int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(start >= all.size() ? List.of() : all.subList(start, end), pageable, all.size());
    }

    @Override
    public Long countByInterviewReviewId(Long reviewId) {
        return store.values().stream()
                .filter(q -> Objects.equals(q.getInterviewReview().getId(), reviewId))
                .count();
    }

    @Override
    public Page<ReviewQuestion> findByQuestionerIdWithInterviewReview(Long questionerId, Pageable pageable) {
        final List<ReviewQuestion> all = store.values().stream()
                .filter(q -> Objects.equals(q.getQuestioner().getId(), questionerId))
                .sorted(Comparator.comparing(ReviewQuestion::getCreatedAt).reversed())
                .collect(Collectors.toList());
        final int start = (int) pageable.getOffset();
        final int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(start >= all.size() ? List.of() : all.subList(start, end), pageable, all.size());
    }

    // === 테스트 헬퍼 ===

    public void clear() {
        store.clear();
        sequence.set(1L);
    }

    public int size() {
        return store.size();
    }

    // === JpaRepository 미사용 ===

    @Override public void flush() {}
    @Override public <S extends ReviewQuestion> S saveAndFlush(S e) { return (S) save(e); }
    @Override public <S extends ReviewQuestion> List<S> saveAllAndFlush(Iterable<S> e) { return saveAll(e); }
    @Override public void deleteAllInBatch(Iterable<ReviewQuestion> e) { deleteAll(e); }
    @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
    @Override public void deleteAllInBatch() { deleteAll(); }
    @Override public ReviewQuestion getOne(Long id) { return findById(id).orElse(null); }
    @Override public ReviewQuestion getById(Long id) { return findById(id).orElse(null); }
    @Override public ReviewQuestion getReferenceById(Long id) { return findById(id).orElse(null); }
    @Override public List<ReviewQuestion> findAllById(Iterable<Long> ids) {
        final List<ReviewQuestion> result = new ArrayList<>();
        ids.forEach(id -> findById(id).ifPresent(result::add));
        return result;
    }
    @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(store::remove); }
    @Override public void deleteAll(Iterable<? extends ReviewQuestion> entities) { entities.forEach(e -> store.remove(e.getId())); }
    @Override public List<ReviewQuestion> findAll(Sort sort) { return findAll(); }
    @Override public Page<ReviewQuestion> findAll(Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewQuestion> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add((S) save(e)));
        return result;
    }
    @Override public <S extends ReviewQuestion> Optional<S> findOne(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewQuestion> List<S> findAll(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewQuestion> List<S> findAll(Example<S> example, Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewQuestion> Page<S> findAll(Example<S> example, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewQuestion> long count(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewQuestion> boolean exists(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewQuestion, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> q) { throw new UnsupportedOperationException(); }

    private void setId(ReviewQuestion question, Long id) {
        try {
            final var field = ReviewQuestion.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(question, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }
}
