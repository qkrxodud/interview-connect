package com.ic.api.fake;

import com.ic.domain.notification.Notification;
import com.ic.domain.notification.NotificationRepository;
import com.ic.domain.notification.NotificationType;
import com.ic.domain.member.Member;
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
 * NotificationRepository의 메모리 기반 Fake 구현체
 * - JPA 없이 빠른 테스트 실행
 * - 실제 동작과 유사한 구현
 * - 상태 기반 검증
 */
public class FakeNotificationRepository implements NotificationRepository {

    @Override
    public void deleteByReferenceIdAndReferenceType(Long referenceId, String referenceType) {
        store.entrySet().removeIf(entry -> {
            Notification notification = entry.getValue();
            return Objects.equals(notification.getReferenceId(), referenceId) &&
                   Objects.equals(notification.getReferenceType(), referenceType);
        });
    }

    @Override
    public boolean existsByRecipientAndReferenceIdAndReferenceType(Member recipient, Long referenceId, String referenceType) {
        return store.values().stream()
                .anyMatch(notification ->
                    Objects.equals(notification.getRecipient(), recipient) &&
                    Objects.equals(notification.getReferenceId(), referenceId) &&
                    Objects.equals(notification.getReferenceType(), referenceType)
                );
    }

    private final Map<Long, Notification> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            // 새로운 알림 등록
            final Long newId = sequence.getAndIncrement();
            store.put(newId, notification);
            return notification; // 실제로는 ID가 설정된 새 객체를 반환해야 함
        }
        // 기존 알림 업데이트
        store.put(notification.getId(), notification);
        return notification;
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * 회원 ID로 알림 조회 (테스트용)
     */
    public List<Notification> findByMemberId(Long memberId) {
        return store.values()
                .stream()
                .filter(notification -> Objects.equals(notification.getRecipient().getId(), memberId))
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림 조회 (테스트용)
     */
    public List<Notification> findUnreadByMemberId(Long memberId) {
        return store.values()
                .stream()
                .filter(notification -> Objects.equals(notification.getRecipient().getId(), memberId))
                .filter(notification -> !notification.isRead())
                .collect(Collectors.toList());
    }

    /**
     * 알림 타입으로 조회 (테스트용)
     */
    public List<Notification> findByType(NotificationType type) {
        return store.values()
                .stream()
                .filter(notification -> Objects.equals(notification.getType(), type))
                .collect(Collectors.toList());
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
    public List<Notification> findAllById(Iterable<Long> ids) {
        final List<Notification> result = new ArrayList<>();
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
    public void delete(Notification notification) {
        store.remove(notification.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            store.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Notification> entities) {
        for (Notification notification : entities) {
            store.remove(notification.getId());
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
     * 저장된 알림 수 조회
     */
    public int size() {
        return store.size();
    }

    /**
     * 특정 알림의 존재 여부 확인
     */
    public boolean hasNotification(Long id) {
        return store.containsKey(id);
    }

    /**
     * 특정 회원의 알림 수 조회 (테스트용)
     */
    public long countByMemberId(Long memberId) {
        return findByMemberId(memberId).size();
    }

    /**
     * 특정 회원의 읽지 않은 알림 수 조회 (테스트용)
     */
    public long countUnreadByMemberId(Long memberId) {
        return findUnreadByMemberId(memberId).size();
    }

    /**
     * 특정 타입의 알림 수 조회 (테스트용)
     */
    public long countByType(NotificationType type) {
        return findByType(type).size();
    }

    /**
     * 모든 알림 조회 (테스트용)
     */
    public Map<Long, Notification> findAllAsMap() {
        return new HashMap<>(store);
    }

    /**
     * 읽음 상태별 알림 수 조회 (테스트용)
     */
    public long countByReadStatus(boolean isRead) {
        return store.values()
                .stream()
                .filter(notification -> notification.isRead() == isRead)
                .count();
    }

    // === NotificationRepository 인터페이스 추가 메서드들 ===

    @Override
    public Page<Notification> findByRecipientOrderByCreatedAtDesc(Member recipient, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 지원하지 않습니다");
    }

    @Override
    public Page<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(Member recipient, Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 지원하지 않습니다");
    }

    @Override
    public long countByRecipientAndIsReadFalse(Member recipient) {
        return store.values().stream()
                .filter(notification -> Objects.equals(notification.getRecipient(), recipient))
                .filter(notification -> !notification.isRead())
                .count();
    }

    @Override
    public void markAllAsReadByRecipient(Member recipient) {
        store.values().stream()
                .filter(notification -> Objects.equals(notification.getRecipient(), recipient))
                .filter(notification -> !notification.isRead())
                .forEach(Notification::markAsRead);
    }

    @Override
    public List<Notification> findByRecipientAndTypeOrderByCreatedAtDesc(Member recipient, NotificationType type) {
        return store.values().stream()
                .filter(notification -> Objects.equals(notification.getRecipient(), recipient))
                .filter(notification -> Objects.equals(notification.getType(), type))
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // === JpaRepository 미사용 메서드들 ===

    @Override
    public void flush() {
        // 인메모리 저장소이므로 flush 불필요
    }

    @Override
    public <S extends Notification> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends Notification> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Notification> entities) {
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
    public Notification getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Notification getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Notification getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends Notification> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Notification> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Notification> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Notification> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Notification> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Notification> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Notification, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery는 지원하지 않습니다");
    }

    @Override
    public List<Notification> findAll(Sort sort) {
        return findAll(); // 정렬 무시하고 전체 조회
    }

    @Override
    public Page<Notification> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Notification> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }
}