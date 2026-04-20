package com.ic.api.fake;

import com.ic.domain.qa.ReviewAnswer;
import com.ic.domain.qa.ReviewAnswerRepository;
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

/**
 * ReviewAnswerRepository의 메모리 기반 Fake 구현체
 * - JPA 없이 빠른 테스트 실행
 * - 실제 동작과 유사한 구현
 * - 상태 기반 검증
 */
public class FakeReviewAnswerRepository implements ReviewAnswerRepository {

    private final Map<Long, ReviewAnswer> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public ReviewAnswer save(ReviewAnswer answer) {
        if (answer.getId() == null) {
            // 새로운 답변 등록 — 리플렉션으로 ID 설정
            final Long newId = sequence.getAndIncrement();
            try {
                final java.lang.reflect.Field idField = ReviewAnswer.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(answer, newId);
            } catch (Exception e) {
                throw new RuntimeException("ID 설정 실패", e);
            }
            store.put(newId, answer);
            return answer;
        }
        // 기존 답변 업데이트
        store.put(answer.getId(), answer);
        return answer;
    }

    @Override
    public Optional<ReviewAnswer> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * 질문 ID로 답변 조회 (테스트용)
     */
    public List<ReviewAnswer> findByQuestionId(Long questionId) {
        return store.values()
                .stream()
                .filter(answer -> Objects.equals(answer.getReviewQuestion().getId(), questionId))
                .collect(Collectors.toList());
    }

    /**
     * 작성자 ID로 답변 조회 (테스트용)
     */
    public List<ReviewAnswer> findByMemberId(Long memberId) {
        return store.values()
                .stream()
                .filter(answer -> Objects.equals(answer.getAnswerer().getId(), memberId))
                .collect(Collectors.toList());
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
    public List<ReviewAnswer> findAllById(Iterable<Long> ids) {
        final List<ReviewAnswer> result = new ArrayList<>();
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
    public void delete(ReviewAnswer answer) {
        store.remove(answer.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            store.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends ReviewAnswer> entities) {
        for (ReviewAnswer answer : entities) {
            store.remove(answer.getId());
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
     * 저장된 답변 수 조회
     */
    public int size() {
        return store.size();
    }

    /**
     * 특정 답변의 존재 여부 확인
     */
    public boolean hasAnswer(Long id) {
        return store.containsKey(id);
    }

    /**
     * 특정 질문의 답변 수 조회 (테스트용)
     */
    public long countByQuestionId(Long questionId) {
        return findByQuestionId(questionId).size();
    }

    /**
     * 특정 회원의 답변 수 조회 (테스트용)
     */
    public long countByMemberId(Long memberId) {
        return findByMemberId(memberId).size();
    }

    /**
     * 모든 답변 조회 (테스트용)
     */
    public Map<Long, ReviewAnswer> findAllAsMap() {
        return new HashMap<>(store);
    }

    // === 리포지토리 인터페이스 구현 메서드들 ===

    @Override
    public List<ReviewAnswer> findByReviewQuestionIdWithAnswerer(Long questionId) {
        return store.values()
                .stream()
                .filter(answer -> Objects.equals(answer.getReviewQuestion().getId(), questionId))
                .sorted((a1, a2) -> a1.getCreatedAt().compareTo(a2.getCreatedAt())) // 등록순
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByReviewQuestionIdAndAnswererId(Long questionId, Long answererId) {
        return store.values()
                .stream()
                .anyMatch(answer -> Objects.equals(answer.getReviewQuestion().getId(), questionId)
                        && Objects.equals(answer.getAnswerer().getId(), answererId));
    }

    // === JpaRepository 미사용 메서드들 ===

    @Override
    public void flush() {
        // 인메모리 저장소이므로 flush 불필요
    }

    @Override
    public <S extends ReviewAnswer> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends ReviewAnswer> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<ReviewAnswer> entities) {
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
    public ReviewAnswer getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public ReviewAnswer getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public ReviewAnswer getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends ReviewAnswer> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewAnswer> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewAnswer> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewAnswer> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewAnswer> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewAnswer> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewAnswer, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery는 지원하지 않습니다");
    }

    @Override
    public List<ReviewAnswer> findAll(Sort sort) {
        return findAll(); // 정렬 무시하고 전체 조회
    }

    @Override
    public Page<ReviewAnswer> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends ReviewAnswer> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }
}