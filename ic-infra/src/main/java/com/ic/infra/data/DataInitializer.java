package com.ic.infra.data;

import com.ic.domain.company.Company;
import com.ic.domain.company.CompanyRepository;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import com.ic.domain.member.MemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * 개발용 초기 데이터 생성기
 * 로컬 및 개발 환경에서만 실행되며, IT 대기업과 스타트업 회사 데이터를 생성한다
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile({"local", "dev"})
@EnableAsync
public class DataInitializer {

    private final CompanyRepository companyRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void initData() {
        log.info("Application ready - starting seed data initialization...");

        // 약간의 지연 후 초기화 시도 (스키마 생성 완료 대기)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Data initialization interrupted");
            return;
        }

        int retryCount = 0;
        int maxRetries = 5;

        while (retryCount < maxRetries) {
            try {
                if (companyRepository.count() == 0) {
                    initCompanies();
                    log.info("Seed data initialization completed successfully");
                } else {
                    log.info("Seed data already exists, skipping initialization");
                }
                return; // 성공 시 종료
            } catch (Exception e) {
                retryCount++;
                log.warn("Attempt {} failed to initialize seed data: {}", retryCount, e.getMessage());

                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(1000 * retryCount); // 점진적 지연
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Data initialization interrupted during retry");
                        return;
                    }
                } else {
                    log.error("Failed to initialize seed data after {} attempts. Tables may not be available.", maxRetries);
                }
            }
        }
    }

    private void initCompanies() {
        log.info("Initializing company seed data...");

        final List<CompanyData> companies = List.of(
                // IT 대기업
                new CompanyData("카카오", "IT", "https://logo.clearbit.com/kakao.com", "https://www.kakaocorp.com"),
                new CompanyData("네이버", "IT", "https://logo.clearbit.com/naver.com", "https://www.navercorp.com"),
                new CompanyData("삼성전자", "전자/반도체", "https://logo.clearbit.com/samsung.com", "https://www.samsung.com"),
                new CompanyData("LG전자", "전자", "https://logo.clearbit.com/lge.com", "https://www.lge.co.kr"),
                new CompanyData("SK텔레콤", "통신", "https://logo.clearbit.com/sktelecom.com", "https://www.sktelecom.com"),
                new CompanyData("KT", "통신", "https://logo.clearbit.com/kt.com", "https://www.kt.com"),
                new CompanyData("LG유플러스", "통신", "https://logo.clearbit.com/lguplus.co.kr", "https://www.lguplus.co.kr"),
                new CompanyData("현대자동차", "자동차", "https://logo.clearbit.com/hyundai.com", "https://www.hyundai.com"),
                new CompanyData("기아자동차", "자동차", "https://logo.clearbit.com/kia.com", "https://www.kia.com"),
                new CompanyData("POSCO", "철강", "https://logo.clearbit.com/posco.co.kr", "https://www.posco.co.kr"),

                // IT 스타트업 & 유니콘
                new CompanyData("토스", "핀테크", "https://logo.clearbit.com/toss.im", "https://toss.im"),
                new CompanyData("배달의민족", "푸드테크", "https://logo.clearbit.com/baemin.com", "https://www.baemin.com"),
                new CompanyData("쿠팡", "이커머스", "https://logo.clearbit.com/coupang.com", "https://www.coupang.com"),
                new CompanyData("당근마켓", "플랫폼", "https://logo.clearbit.com/daangn.com", "https://www.daangn.com"),
                new CompanyData("카카오뱅크", "핀테크", "https://logo.clearbit.com/kakaobank.com", "https://www.kakaobank.com"),
                new CompanyData("카카오페이", "핀테크", "https://logo.clearbit.com/kakaopay.com", "https://www.kakaopay.com"),
                new CompanyData("네이버파이낸셜", "핀테크", null, "https://www.naverfin.com"),
                new CompanyData("라인", "메신저/플랫폼", "https://logo.clearbit.com/line.me", "https://line.me"),
                new CompanyData("크래프톤", "게임", "https://logo.clearbit.com/krafton.com", "https://www.krafton.com"),
                new CompanyData("넷마블", "게임", "https://logo.clearbit.com/netmarble.com", "https://www.netmarble.com"),

                // 글로벌 IT 기업 한국지사
                new CompanyData("구글코리아", "IT", "https://logo.clearbit.com/google.com", "https://www.google.co.kr"),
                new CompanyData("마이크로소프트", "IT", "https://logo.clearbit.com/microsoft.com", "https://www.microsoft.com"),
                new CompanyData("메타코리아", "IT", "https://logo.clearbit.com/meta.com", "https://www.meta.com"),
                new CompanyData("아마존코리아", "이커머스/클라우드", "https://logo.clearbit.com/amazon.com", "https://www.amazon.co.kr"),
                new CompanyData("넷플릭스코리아", "OTT", "https://logo.clearbit.com/netflix.com", "https://www.netflix.com"),

                // 금융
                new CompanyData("신한은행", "금융", "https://logo.clearbit.com/shinhan.com", "https://www.shinhan.com"),
                new CompanyData("국민은행", "금융", "https://logo.clearbit.com/kb.co.kr", "https://www.kbstar.com"),
                new CompanyData("우리은행", "금융", "https://logo.clearbit.com/wooribank.com", "https://www.wooribank.com"),
                new CompanyData("하나은행", "금융", "https://logo.clearbit.com/hanabank.com", "https://www.hanabank.com"),
                new CompanyData("NH농협은행", "금융", "https://logo.clearbit.com/nonghyup.com", "https://banking.nonghyup.com"),

                // 스타트업
                new CompanyData("버킷플레이스", "플랫폼", null, "https://www.bucketplace.co.kr"),
                new CompanyData("야놀자", "여행/숙박", "https://logo.clearbit.com/yanolja.com", "https://www.yanolja.com"),
                new CompanyData("직방", "부동산테크", "https://logo.clearbit.com/zigbang.com", "https://www.zigbang.com"),
                new CompanyData("뱅크샐러드", "핀테크", "https://logo.clearbit.com/banksalad.com", "https://www.banksalad.com"),
                new CompanyData("원티드", "HR테크", "https://logo.clearbit.com/wanted.co.kr", "https://www.wanted.co.kr"),
                new CompanyData("로켓펀치", "HR테크", "https://logo.clearbit.com/rocketpunch.com", "https://www.rocketpunch.com"),
                new CompanyData("프로그래머스", "에듀테크", "https://logo.clearbit.com/programmers.co.kr", "https://programmers.co.kr"),
                new CompanyData("패스트캠퍼스", "에듀테크", "https://logo.clearbit.com/fastcampus.co.kr", "https://www.fastcampus.co.kr"),
                new CompanyData("인프런", "에듀테크", "https://logo.clearbit.com/inflearn.com", "https://www.inflearn.com"),
                new CompanyData("29CM", "이커머스/패션", "https://logo.clearbit.com/29cm.co.kr", "https://www.29cm.co.kr"),
                new CompanyData("무신사", "패션", "https://logo.clearbit.com/musinsa.com", "https://www.musinsa.com"),
                new CompanyData("컬리", "이커머스/식품", "https://logo.clearbit.com/kurly.com", "https://www.kurly.com"),
                new CompanyData("번개장터", "중고거래", "https://logo.clearbit.com/bunjang.co.kr", "https://www.bunjang.co.kr"),
                new CompanyData("중고나라", "중고거래", "https://logo.clearbit.com/joongna.com", "https://www.joongna.com"),

                // IT 서비스
                new CompanyData("NHN", "IT서비스", "https://logo.clearbit.com/nhn.com", "https://www.nhn.com"),
                new CompanyData("카카오엔터프라이즈", "IT서비스", null, "https://www.kakaoenterprise.com"),
                new CompanyData("네이버클라우드플랫폼", "클라우드", null, "https://www.ncloud.com"),
                new CompanyData("라인플러스", "IT서비스", null, "https://linepluscorp.com"),
                new CompanyData("데브시스터즈", "게임", "https://logo.clearbit.com/devsisters.com", "https://www.devsisters.com"),
                new CompanyData("스마일게이트", "게임", "https://logo.clearbit.com/smilegate.com", "https://www.smilegate.com"),
                new CompanyData("엔씨소프트", "게임", "https://logo.clearbit.com/ncsoft.com", "https://www.ncsoft.com"),
                new CompanyData("넥슨", "게임", "https://logo.clearbit.com/nexon.com", "https://www.nexon.com")
        );

        companies.forEach(companyData -> {
            if (!companyRepository.existsByName(companyData.name())) {
                final Company company = Company.from(
                        companyData.name(),
                        companyData.industry(),
                        companyData.logoUrl(),
                        companyData.website()
                );
                companyRepository.save(company);
            }
        });

        log.info("Company seed data initialization completed. Total companies: {}", companies.size());
    }

    private record CompanyData(String name, String industry, String logoUrl, String website) {}
}