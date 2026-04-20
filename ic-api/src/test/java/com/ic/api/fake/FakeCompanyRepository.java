package com.ic.api.fake;

import com.ic.domain.company.Company;
import com.ic.domain.company.CompanyRepository;
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
 * CompanyRepository의 메모리 기반 Fake 구현체
 * - JPA 없이 빠른 테스트 실행
 * - 실제 동작과 유사한 구현
 * - 검색 기능 포함
 * - 상태 기반 검증
 */
public class FakeCompanyRepository implements CompanyRepository {

    private final Map<Long, Company> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Company save(Company company) {
        if (company.getId() == null) {
            // 새로운 회사 등록
            final Company newCompany = Company.builder()
                    .id(sequence.getAndIncrement())
                    .name(company.getName())
                    .industry(company.getIndustry())
                    .logoUrl(company.getLogoUrl())
                    .website(company.getWebsite())
                    .build();
            store.put(newCompany.getId(), newCompany);
            return newCompany;
        }
        // 기존 회사 업데이트
        store.put(company.getId(), company);
        return company;
    }

    @Override
    public Optional<Company> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Company> findByName(String name) {
        return store.values()
                .stream()
                .filter(company -> company.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Company> findTop10ByNameContainingIgnoreCase(String keyword) {
        return store.values()
                .stream()
                .filter(company -> company.getName().toLowerCase().contains(keyword.toLowerCase()))
                .sorted(Comparator.comparing(Company::getName))
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return store.values()
                .stream()
                .anyMatch(company -> company.getName().equals(name));
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public List<Company> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Company> findAllById(Iterable<Long> ids) {
        final List<Company> result = new ArrayList<>();
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
    public void delete(Company company) {
        store.remove(company.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            store.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Company> entities) {
        for (Company company : entities) {
            store.remove(company.getId());
        }
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    // === 테스트 헬퍼 메서드들 ===

    /**
     * 회사명 포함 검색 헬퍼 (테스트용)
     */
    public List<Company> findByNameContaining(String keyword) {
        return findTop10ByNameContainingIgnoreCase(keyword);
    }

    /**
     * 저장소 초기화
     */
    public void clear() {
        store.clear();
        sequence.set(1L);
    }

    /**
     * 저장된 회사 수 조회
     */
    public int size() {
        return store.size();
    }

    /**
     * 특정 회사의 존재 여부 확인
     */
    public boolean hasCompany(Long id) {
        return store.containsKey(id);
    }

    /**
     * 회사명으로 존재 여부 확인 (헬퍼)
     */
    public boolean hasCompanyWithName(String name) {
        return existsByName(name);
    }

    /**
     * 특정 업계의 회사 수 조회 (테스트용)
     */
    public long countByIndustry(String industry) {
        return store.values()
                .stream()
                .filter(company -> Objects.equals(company.getIndustry(), industry))
                .count();
    }

    /**
     * 모든 회사 조회 (테스트용)
     */
    public Map<Long, Company> findAllAsMap() {
        return new HashMap<>(store);
    }

    /**
     * 테스트 데이터 생성 헬퍼
     */
    public Company createTestCompany(String name, String industry) {
        return save(Company.from(name, industry, null, null));
    }

    // === JpaRepository 미사용 메서드들 ===

    @Override
    public void flush() {
        // 인메모리 저장소이므로 flush 불필요
    }

    @Override
    public <S extends Company> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends Company> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Company> entities) {
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
    public Company getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Company getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Company getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends Company> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Company> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Company> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Company> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Company> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Company> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Company, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery는 지원하지 않습니다");
    }

    @Override
    public List<Company> findAll(Sort sort) {
        return findAll(); // 정렬 무시하고 전체 조회
    }

    @Override
    public Page<Company> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Page 기반 조회는 지원하지 않습니다");
    }

    @Override
    public <S extends Company> List<S> saveAll(Iterable<S> entities) {
        final List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }
}