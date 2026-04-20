package com.ic.domain.fake;

import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.InterviewReviewRepository;
import com.ic.domain.review.InterviewResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * InterviewReviewRepository의 메모리 기반 Fake 구현체
 * - JPA 없이 빠른 테스트 실행
 * - 실제 동작과 유사한 구현
 * - 필터링 및 검색 기능 포함
 * - 상태 기반 검증
 */
public class FakeInterviewReviewRepository implements InterviewReviewRepository {

    private final Map<Long, InterviewReview> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public InterviewReview save(InterviewReview review) {
        if (review.getId() == null) {
            // 새로운 후기 등록 - ID 설정
            final InterviewReview newReview = createReviewWithId(review, sequence.getAndIncrement());
            store.put(newReview.getId(), newReview);
            return newReview;
        }
        // 기존 후기 업데이트
        store.put(review.getId(), review);
        return review;
    }

    @Override
    public Optional<InterviewReview> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<InterviewReview> findByMemberId(Long memberId) {
        return store.values()
                .stream()
                .filter(review -> Objects.equals(review.getMember().getId(), memberId))
                .collect(Collectors.toList());
    }

    // Page 관련 메서드들은 간단한 리스트 반환으로 구현 (테스트용)
    @Override
    public Page<InterviewReview> findByCompanyId(Long companyId, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 단순 리스트로 대체하여 사용하세요");
    }

    @Override
    public Page<InterviewReview> findByPosition(String position, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 단순 리스트로 대체하여 사용하세요");
    }

    @Override
    public Page<InterviewReview> findByDifficulty(int difficulty, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 단순 리스트로 대체하여 사용하세요");
    }

    @Override
    public Page<InterviewReview> findByResult(InterviewResult result, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 단순 리스트로 대체하여 사용하세요");
    }

    @Override
    public Page<InterviewReview> findByCompanyIdAndPosition(Long companyId, String position, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 단순 리스트로 대체하여 사용하세요");
    }

    @Override
    public Page<InterviewReview> findByCompanyIdAndDifficulty(Long companyId, int difficulty, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 단순 리스트로 대체하여 사용하세요");
    }

    @Override
    public Page<InterviewReview> findByPositionAndDifficulty(String position, int difficulty, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 단순 리스트로 대체하여 사용하세요");
    }

    @Override
    public Page<InterviewReview> findByAllFilters(Long companyId, String position, Integer difficulty, InterviewResult result, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 단순 리스트로 대체하여 사용하세요");
    }

    // === 테스트용 간단한 필터링 메서드들 ===

    /**
     * 회사 ID로 후기 조회 (테스트용 - List 반환)
     */
    public List<InterviewReview> findByCompanyIdSimple(Long companyId) {
        return store.values()
                .stream()
                .filter(review -> Objects.equals(review.getCompany().getId(), companyId))
                .collect(Collectors.toList());
    }

    /**
     * 포지션으로 후기 조회 (테스트용)
     */
    public List<InterviewReview> findByPositionSimple(String position) {
        return store.values()
                .stream()
                .filter(review -> Objects.equals(review.getPosition(), position))
                .collect(Collectors.toList());
    }

    /**
     * 난이도로 후기 조회 (테스트용)
     */
    public List<InterviewReview> findByDifficultySimple(int difficulty) {
        return store.values()
                .stream()
                .filter(review -> review.getDifficulty() == difficulty)
                .collect(Collectors.toList());
    }

    /**
     * 결과로 후기 조회 (테스트용)
     */
    public List<InterviewReview> findByResultSimple(InterviewResult result) {
        return store.values()
                .stream()
                .filter(review -> review.getResult() == result)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public List<InterviewReview> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<InterviewReview> findAllById(Iterable<Long> ids) {
        final List<InterviewReview> result = new ArrayList<>();
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
    public void delete(InterviewReview review) {
        store.remove(review.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            store.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends InterviewReview> entities) {
        for (InterviewReview review : entities) {
            store.remove(review.getId());
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
     * 저장된 후기 수 조회
     */
    public int size() {
        return store.size();
    }

    /**
     * 특정 후기의 존재 여부 확인
     */
    public boolean hasReview(Long id) {
        return store.containsKey(id);
    }

    /**
     * 특정 회원의 후기 수 조회 (테스트용)
     */
    public long countByMemberId(Long memberId) {
        return findByMemberId(memberId).size();
    }

    /**
     * 특정 회사의 후기 수 조회 (테스트용)
     */
    public long countByCompanyId(Long companyId) {
        return findByCompanyIdSimple(companyId).size();
    }

    /**
     * 모든 후기 조회 (테스트용)
     */
    public Map<Long, InterviewReview> findAllAsMap() {
        return new HashMap<>(store);
    }

    // === 리플렉션을 사용한 ID 설정 헬퍼 메서드 ===

    private InterviewReview createReviewWithId(InterviewReview review, Long id) {
        return InterviewReview.builder()
                .id(id)
                .member(review.getMember())
                .company(review.getCompany())
                .interviewDate(review.getInterviewDate())
                .position(review.getPosition())
                .interviewTypes(review.getInterviewTypes())
                .questions(review.getQuestions())
                .difficulty(review.getDifficulty())
                .atmosphere(review.getAtmosphere())
                .result(review.getResult())
                .content(review.getContent())
                .viewCount(review.getViewCount())
                .build();
    }

    // === JpaRepository 미사용 메서드들 ===

    @Override
    public void flush() {}

    @Override
    public <S extends InterviewReview> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends InterviewReview> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException("saveAllAndFlush는 지원하지 않습니다");
    }

    @Override
    public void deleteAllInBatch(Iterable<InterviewReview> entities) {
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
    public InterviewReview getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public InterviewReview getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public InterviewReview getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends InterviewReview> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }

    // Spring Data 기본 메서드들 추가 구현
    @Override
    public org.springframework.data.domain.Page<InterviewReview> findAll(org.springframework.data.domain.Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 지원하지 않습니다");
    }

    @Override
    public java.util.List<InterviewReview> findAll(org.springframework.data.domain.Sort sort) {
        return findAll(); // 정렬 무시하고 전체 조회
    }

    @Override
    public <S extends InterviewReview> java.util.Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends InterviewReview> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends InterviewReview> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends InterviewReview> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends InterviewReview> long count(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends InterviewReview> boolean exists(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends InterviewReview, R> R findBy(org.springframework.data.domain.Example<S> example,
                                                  java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery는 지원하지 않습니다");
    }
}