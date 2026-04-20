package com.ic.domain.fake;

import com.ic.domain.qa.ReviewAnswer;
import com.ic.domain.qa.ReviewAnswerRepository;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FakeReviewAnswerRepository implements ReviewAnswerRepository {

    private final Map<Long, ReviewAnswer> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public ReviewAnswer save(ReviewAnswer answer) {
        if (answer.getId() == null) {
            setId(answer, sequence.getAndIncrement());
        }
        store.put(answer.getId(), answer);
        return answer;
    }

    @Override
    public Optional<ReviewAnswer> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public List<ReviewAnswer> findAll() {
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
    public void delete(ReviewAnswer entity) {
        store.remove(entity.getId());
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    @Override
    public List<ReviewAnswer> findByReviewQuestionIdWithAnswerer(Long questionId) {
        return store.values().stream()
                .filter(a -> Objects.equals(a.getReviewQuestion().getId(), questionId))
                .sorted(Comparator.comparing(ReviewAnswer::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByReviewQuestionIdAndAnswererId(Long questionId, Long answererId) {
        return store.values().stream()
                .anyMatch(a -> Objects.equals(a.getReviewQuestion().getId(), questionId)
                        && Objects.equals(a.getAnswerer().getId(), answererId));
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
    @Override public <S extends ReviewAnswer> S saveAndFlush(S e) { return (S) save(e); }
    @Override public <S extends ReviewAnswer> List<S> saveAllAndFlush(Iterable<S> e) { return saveAll(e); }
    @Override public void deleteAllInBatch(Iterable<ReviewAnswer> e) { deleteAll(e); }
    @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
    @Override public void deleteAllInBatch() { deleteAll(); }
    @Override public ReviewAnswer getOne(Long id) { return findById(id).orElse(null); }
    @Override public ReviewAnswer getById(Long id) { return findById(id).orElse(null); }
    @Override public ReviewAnswer getReferenceById(Long id) { return findById(id).orElse(null); }
    @Override public List<ReviewAnswer> findAllById(Iterable<Long> ids) {
        final List<ReviewAnswer> result = new ArrayList<>();
        ids.forEach(id -> findById(id).ifPresent(result::add));
        return result;
    }
    @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(store::remove); }
    @Override public void deleteAll(Iterable<? extends ReviewAnswer> entities) { entities.forEach(e -> store.remove(e.getId())); }
    @Override public List<ReviewAnswer> findAll(Sort sort) { return findAll(); }
    @Override public Page<ReviewAnswer> findAll(Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewAnswer> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add((S) save(e)));
        return result;
    }
    @Override public <S extends ReviewAnswer> Optional<S> findOne(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewAnswer> List<S> findAll(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewAnswer> List<S> findAll(Example<S> example, Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewAnswer> Page<S> findAll(Example<S> example, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewAnswer> long count(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewAnswer> boolean exists(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ReviewAnswer, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> q) { throw new UnsupportedOperationException(); }

    private void setId(ReviewAnswer answer, Long id) {
        try {
            final var field = ReviewAnswer.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(answer, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }
}
