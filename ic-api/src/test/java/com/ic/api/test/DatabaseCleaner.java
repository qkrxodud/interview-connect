package com.ic.api.test;

import com.ic.api.fake.*;

/**
 * 테스트 데이터베이스 정리를 담당하는 클래스
 * - 테스트 간 데이터 격리
 * - 일관된 초기화
 * - 성능 최적화된 정리 작업
 */
public class DatabaseCleaner {

    private final FakeMemberRepository fakeMemberRepository;
    private final FakeCompanyRepository fakeCompanyRepository;
    private final FakeInterviewReviewRepository fakeInterviewReviewRepository;
    private final FakeReviewQuestionRepository fakeReviewQuestionRepository;
    private final FakeReviewAnswerRepository fakeReviewAnswerRepository;
    private final FakeNotificationRepository fakeNotificationRepository;

    public DatabaseCleaner(FakeMemberRepository fakeMemberRepository,
                          FakeCompanyRepository fakeCompanyRepository,
                          FakeInterviewReviewRepository fakeInterviewReviewRepository,
                          FakeReviewQuestionRepository fakeReviewQuestionRepository,
                          FakeReviewAnswerRepository fakeReviewAnswerRepository,
                          FakeNotificationRepository fakeNotificationRepository) {
        this.fakeMemberRepository = fakeMemberRepository;
        this.fakeCompanyRepository = fakeCompanyRepository;
        this.fakeInterviewReviewRepository = fakeInterviewReviewRepository;
        this.fakeReviewQuestionRepository = fakeReviewQuestionRepository;
        this.fakeReviewAnswerRepository = fakeReviewAnswerRepository;
        this.fakeNotificationRepository = fakeNotificationRepository;
    }

    /**
     * 모든 테스트 데이터 초기화
     * - 순서를 고려하여 안전하게 정리
     * - 외래키 제약조건을 고려한 순서
     */
    public void cleanAll() {
        // 연관관계 순서를 고려하여 정리
        fakeReviewAnswerRepository.clear();      // 답변 먼저
        fakeReviewQuestionRepository.clear();    // 질문 다음
        fakeNotificationRepository.clear();      // 알림
        fakeInterviewReviewRepository.clear();   // 리뷰
        fakeMemberRepository.clear();            // 회원
        fakeCompanyRepository.clear();           // 회사
    }

    /**
     * 특정 도메인만 정리
     */
    public void cleanMemberData() {
        fakeReviewAnswerRepository.clear();
        fakeReviewQuestionRepository.clear();
        fakeNotificationRepository.clear();
        fakeInterviewReviewRepository.clear();
        fakeMemberRepository.clear();
    }

    /**
     * 회사 관련 데이터만 정리
     */
    public void cleanCompanyData() {
        fakeInterviewReviewRepository.clear();
        fakeCompanyRepository.clear();
    }

    /**
     * Q&A 관련 데이터만 정리
     */
    public void cleanQAData() {
        fakeReviewAnswerRepository.clear();
        fakeReviewQuestionRepository.clear();
    }

    /**
     * 알림 데이터만 정리
     */
    public void cleanNotificationData() {
        fakeNotificationRepository.clear();
    }

    /**
     * 데이터 존재 여부 확인 (테스트 검증용)
     */
    public boolean hasAnyData() {
        return fakeMemberRepository.size() > 0 ||
               fakeCompanyRepository.size() > 0 ||
               fakeInterviewReviewRepository.size() > 0 ||
               fakeReviewQuestionRepository.size() > 0 ||
               fakeReviewAnswerRepository.size() > 0 ||
               fakeNotificationRepository.size() > 0;
    }

    /**
     * 각 저장소의 데이터 수 확인 (디버깅용)
     */
    public String getDataStatus() {
        return String.format(
                "Members: %d, Companies: %d, Reviews: %d, Questions: %d, Answers: %d, Notifications: %d",
                fakeMemberRepository.size(),
                fakeCompanyRepository.size(),
                fakeInterviewReviewRepository.size(),
                fakeReviewQuestionRepository.size(),
                fakeReviewAnswerRepository.size(),
                fakeNotificationRepository.size()
        );
    }

    /**
     * 특정 회원과 관련된 모든 데이터 정리
     */
    public void cleanMemberRelatedData(Long memberId) {
        // 해당 회원의 답변 삭제
        fakeReviewAnswerRepository.findByMemberId(memberId)
                .forEach(answer -> fakeReviewAnswerRepository.delete(answer));

        // 해당 회원의 질문 삭제
        fakeReviewQuestionRepository.findByMemberId(memberId)
                .forEach(question -> fakeReviewQuestionRepository.delete(question));

        // 해당 회원의 알림 삭제
        fakeNotificationRepository.findByMemberId(memberId)
                .forEach(notification -> fakeNotificationRepository.delete(notification));

        // 해당 회원의 후기 삭제
        fakeInterviewReviewRepository.findByMemberId(memberId)
                .forEach(review -> fakeInterviewReviewRepository.delete(review));

        // 회원 삭제
        fakeMemberRepository.deleteById(memberId);
    }

    /**
     * 특정 회사와 관련된 모든 데이터 정리
     */
    public void cleanCompanyRelatedData(Long companyId) {
        // 해당 회사의 후기들과 관련된 Q&A 삭제
        fakeInterviewReviewRepository.findByCompanyIdSimple(companyId)
                .forEach(review -> {
                    // 해당 후기의 질문들의 답변 삭제
                    fakeReviewQuestionRepository.findByReviewId(review.getId())
                            .forEach(question -> {
                                fakeReviewAnswerRepository.findByQuestionId(question.getId())
                                        .forEach(answer -> fakeReviewAnswerRepository.delete(answer));
                                fakeReviewQuestionRepository.delete(question);
                            });
                    fakeInterviewReviewRepository.delete(review);
                });

        // 회사 삭제
        fakeCompanyRepository.deleteById(companyId);
    }
}