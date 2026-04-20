package com.ic.domain.fake;

import com.ic.domain.member.Member;
import com.ic.domain.notification.Notification;
import com.ic.domain.notification.NotificationRepository;
import com.ic.domain.notification.NotificationType;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FakeNotificationRepository implements NotificationRepository {

    private final Map<Long, Notification> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            setId(notification, sequence.getAndIncrement());
        }
        store.put(notification.getId(), notification);
        return notification;
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public List<Notification> findAll() {
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
    public void delete(Notification entity) {
        store.remove(entity.getId());
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    @Override
    public Page<Notification> findByRecipientOrderByCreatedAtDesc(Member recipient, Pageable pageable) {
        final List<Notification> all = store.values().stream()
                .filter(n -> Objects.equals(n.getRecipient().getId(), recipient.getId()))
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsFirst(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        final int start = (int) pageable.getOffset();
        final int end = Math.min(start + pageable.getPageSize(), all.size());
        return pageable.isUnpaged()
                ? new PageImpl<>(all)
                : new PageImpl<>(start >= all.size() ? List.of() : all.subList(start, end), pageable, all.size());
    }

    @Override
    public Page<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(Member recipient, Pageable pageable) {
        final List<Notification> all = store.values().stream()
                .filter(n -> Objects.equals(n.getRecipient().getId(), recipient.getId()) && !n.isRead())
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsFirst(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        final int start = (int) pageable.getOffset();
        final int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(start >= all.size() ? List.of() : all.subList(start, end), pageable, all.size());
    }

    @Override
    public long countByRecipientAndIsReadFalse(Member recipient) {
        return store.values().stream()
                .filter(n -> Objects.equals(n.getRecipient().getId(), recipient.getId()) && !n.isRead())
                .count();
    }

    @Override
    public void markAllAsReadByRecipient(Member recipient) {
        store.values().stream()
                .filter(n -> Objects.equals(n.getRecipient().getId(), recipient.getId()))
                .forEach(Notification::markAsRead);
    }

    @Override
    public List<Notification> findByRecipientAndTypeOrderByCreatedAtDesc(Member recipient, NotificationType type) {
        return store.values().stream()
                .filter(n -> Objects.equals(n.getRecipient().getId(), recipient.getId()) && n.getType() == type)
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsFirst(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByRecipientAndReferenceIdAndReferenceType(Member recipient, Long referenceId, String referenceType) {
        return store.values().stream()
                .anyMatch(n -> Objects.equals(n.getRecipient().getId(), recipient.getId())
                        && Objects.equals(n.getReferenceId(), referenceId)
                        && Objects.equals(n.getReferenceType(), referenceType));
    }

    @Override
    public void deleteByReferenceIdAndReferenceType(Long referenceId, String referenceType) {
        store.entrySet().removeIf(e ->
                Objects.equals(e.getValue().getReferenceId(), referenceId)
                && Objects.equals(e.getValue().getReferenceType(), referenceType));
    }

    // === 테스트 헬퍼 ===

    public void clear() {
        store.clear();
        sequence.set(1L);
    }

    public int size() {
        return store.size();
    }

    public List<Notification> findAllForRecipient(Long recipientId) {
        return store.values().stream()
                .filter(n -> Objects.equals(n.getRecipient().getId(), recipientId))
                .collect(Collectors.toList());
    }

    // === JpaRepository 미사용 ===

    @Override public void flush() {}
    @Override public <S extends Notification> S saveAndFlush(S e) { return (S) save(e); }
    @Override public <S extends Notification> List<S> saveAllAndFlush(Iterable<S> e) { return saveAll(e); }
    @Override public void deleteAllInBatch(Iterable<Notification> e) { deleteAll(e); }
    @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
    @Override public void deleteAllInBatch() { deleteAll(); }
    @Override public Notification getOne(Long id) { return findById(id).orElse(null); }
    @Override public Notification getById(Long id) { return findById(id).orElse(null); }
    @Override public Notification getReferenceById(Long id) { return findById(id).orElse(null); }
    @Override public List<Notification> findAllById(Iterable<Long> ids) {
        final List<Notification> result = new ArrayList<>();
        ids.forEach(id -> findById(id).ifPresent(result::add));
        return result;
    }
    @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(store::remove); }
    @Override public void deleteAll(Iterable<? extends Notification> entities) { entities.forEach(e -> store.remove(e.getId())); }
    @Override public List<Notification> findAll(Sort sort) { return findAll(); }
    @Override public Page<Notification> findAll(Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends Notification> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add((S) save(e)));
        return result;
    }
    @Override public <S extends Notification> Optional<S> findOne(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends Notification> List<S> findAll(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends Notification> List<S> findAll(Example<S> example, Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends Notification> Page<S> findAll(Example<S> example, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends Notification> long count(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends Notification> boolean exists(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends Notification, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> q) { throw new UnsupportedOperationException(); }

    private void setId(Notification notification, Long id) {
        try {
            final var field = Notification.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(notification, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }
}
