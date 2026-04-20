package com.ic.api.fake;

import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * MemberRepository의 메모리 기반 Fake 구현체
 * - JPA 없이 빠른 테스트 실행
 * - 실제 동작과 유사한 구현
 * - 상태 기반 검증
 * - 여러 테스트에서 재사용 가능
 */
public class FakeMemberRepository implements MemberRepository {

    private final Map<Long, Member> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Member save(Member member) {
        if (member.getId() == null) {
            // 새로운 회원 등록
            final Member newMember = Member.builder()
                    .id(sequence.getAndIncrement())
                    .email(member.getEmail())
                    .password(member.getPassword())
                    .nickname(member.getNickname())
                    .role(member.getRole())
                    .emailVerified(member.isEmailVerified())
                    .verificationCode(member.getVerificationCode())
                    .verificationCodeExpiry(member.getVerificationCodeExpiry())
                    .build();
            store.put(newMember.getId(), newMember);
            return newMember;
        }
        // 기존 회원 업데이트
        store.put(member.getId(), member);
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return store.values()
                .stream()
                .filter(member -> member.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return store.values()
                .stream()
                .anyMatch(member -> member.getEmail().equals(email));
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return store.values()
                .stream()
                .anyMatch(member -> member.getNickname().equals(nickname));
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Member> findAllById(Iterable<Long> ids) {
        final List<Member> result = new ArrayList<>();
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
    public void delete(Member member) {
        store.remove(member.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            store.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Member> entities) {
        for (Member member : entities) {
            store.remove(member.getId());
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
     * 저장된 회원 수 조회
     */
    public int size() {
        return store.size();
    }

    /**
     * 특정 회원의 존재 여부 확인
     */
    public boolean hasMember(Long id) {
        return store.containsKey(id);
    }

    /**
     * 모든 회원 조회 (테스트용)
     */
    public Map<Long, Member> findAllAsMap() {
        return new HashMap<>(store);
    }

    /**
     * 이메일로 회원 존재 여부 확인 (헬퍼)
     */
    public boolean hasMemberWithEmail(String email) {
        return existsByEmail(email);
    }

    /**
     * 닉네임으로 회원 존재 여부 확인 (헬퍼)
     */
    public boolean hasMemberWithNickname(String nickname) {
        return existsByNickname(nickname);
    }

    // === JpaRepository 미사용 메서드들 (테스트에서 사용하지 않음) ===

    @Override
    public void flush() {
        // 인메모리 저장소이므로 flush 불필요
    }

    @Override
    public <S extends Member> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends Member> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Member> entities) {
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
    public Member getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Member getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Member getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends Member> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Member> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Member> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Member> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Member> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Member> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Member, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery는 지원하지 않습니다");
    }

    @Override
    public List<Member> findAll(Sort sort) {
        return findAll(); // 정렬 무시하고 전체 조회
    }

    @Override
    public Page<Member> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Member> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }
}