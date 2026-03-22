package com.ic.domain.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 회사 Repository 인터페이스
 * 회사 정보 조회 및 검색 기능을 제공한다
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * 회사명으로 회사 조회
     */
    Optional<Company> findByName(String name);

    /**
     * 회사명 포함 검색 (자동완성용)
     * 최대 10건까지만 반환
     */
    @Query("SELECT c FROM Company c WHERE c.name LIKE %:keyword% ORDER BY c.name ASC")
    List<Company> findByNameContaining(@Param("keyword") String keyword);

    /**
     * 회사명 존재 여부 확인
     */
    boolean existsByName(String name);
}