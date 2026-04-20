package com.ic.domain.fake;

import com.ic.domain.comment.Comment;
import com.ic.domain.comment.CommentRepository;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CommentRepository의 메모리 기반 Fake 구현체
 */
public class FakeCommentRepository implements CommentRepository {

    private final Map<Long, Comment> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            final Comment newComment = createCommentWithId(comment, sequence.getAndIncrement());
            store.put(newComment.getId(), newComment);
            return newComment;
        }
        store.put(comment.getId(), comment);
        return comment;
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Comment> findByInterviewReviewIdAndDeletedFalseOrderByCreatedAtAsc(Long reviewId) {
        return store.values().stream()
                .filter(comment -> Objects.equals(comment.getInterviewReview().getId(), reviewId))
                .filter(Comment::isActive)
                .sorted(Comparator.comparing(Comment::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public Page<Comment> findByInterviewReviewIdAndDeletedFalseOrderByCreatedAtAsc(Long reviewId, Pageable pageable) {
        final List<Comment> all = findByInterviewReviewIdAndDeletedFalseOrderByCreatedAtAsc(reviewId);
        final int start = (int) pageable.getOffset();
        final int end = Math.min(start + pageable.getPageSize(), all.size());
        final List<Comment> paged = start >= all.size() ? List.of() : all.subList(start, end);
        return new PageImpl<>(paged, pageable, all.size());
    }

    @Override
    public long countByInterviewReviewIdAndDeletedFalse(Long reviewId) {
        return store.values().stream()
                .filter(comment -> Objects.equals(comment.getInterviewReview().getId(), reviewId))
                .filter(Comment::isActive)
                .count();
    }

    @Override
    public List<Comment> findTopLevelCommentsByReviewId(Long reviewId) {
        return store.values().stream()
                .filter(comment -> Objects.equals(comment.getInterviewReview().getId(), reviewId))
                .filter(Comment::isActive)
                .filter(comment -> comment.getParent() == null)
                .sorted(Comparator.comparing(Comment::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public List<Comment> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Comment> findAllById(Iterable<Long> ids) {
        final List<Comment> result = new ArrayList<>();
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
    public void delete(Comment entity) {
        store.remove(entity.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            store.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Comment> entities) {
        for (Comment c : entities) {
            store.remove(c.getId());
        }
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    // === 테스트 헬퍼 ===

    public void clear() {
        store.clear();
        sequence.set(1L);
    }

    public int size() {
        return store.size();
    }

    public long countActiveComments() {
        return store.values().stream().filter(Comment::isActive).count();
    }

    // === JpaRepository 미사용 메서드 ===

    @Override
    public void flush() {}

    @Override
    public <S extends Comment> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends Comment> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Comment> entities) {
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
    public Comment getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Comment getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Comment getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends Comment> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }

    @Override
    public List<Comment> findAll(Sort sort) {
        return findAll();
    }

    @Override
    public Page<Comment> findAll(Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Comment> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Comment> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Comment> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Comment> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Comment> long count(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Comment> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Comment, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException();
    }

    // === ID 설정 헬퍼 ===

    private Comment createCommentWithId(Comment comment, Long id) {
        try {
            final var idField = Comment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(comment, id);

            // JPA @CreatedDate가 Fake에서 동작하지 않으므로 직접 설정
            if (comment.getCreatedAt() == null) {
                final var createdAtField = comment.getClass().getSuperclass().getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(comment, LocalDateTime.now());
            }
            if (comment.getUpdatedAt() == null) {
                final var updatedAtField = comment.getClass().getSuperclass().getDeclaredField("updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(comment, LocalDateTime.now());
            }

            return comment;
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 중 오류 발생", e);
        }
    }
}
