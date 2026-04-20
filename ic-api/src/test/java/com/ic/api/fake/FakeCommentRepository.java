//package com.ic.api.fake;
//
//import com.ic.domain.comment.Comment;
//import com.ic.domain.comment.CommentRepository;
//import org.springframework.data.domain.Pageable;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class FakeCommentRepository  {
//
//    private final Map<Long, Comment> store = new HashMap<>();
//    private Long sequence = 1L;
//
//    @Override
//    public Comment save(Comment comment) {
//        if (Objects.isNull(comment.getId())) {
//            setId(comment, sequence++);
//        }
//        store.put(comment.getId(), comment);
//        return comment;
//    }
//
//    @Override
//    public Optional<Comment> findById(Long id) {
//        return Optional.ofNullable(store.get(id));
//    }
//
//    @Override
//    public List<Comment> findByInterviewReviewIdAndDeletedFalseOrderByCreatedAtAsc(Long interviewReviewId) {
//        return store.values().stream()
//                .filter(comment -> Objects.equals(comment.getInterviewReview().getId(), interviewReviewId))
//                .filter(Comment::isActive)
//                .sorted(Comparator.comparing(Comment::getCreatedAt))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Comment> findByInterviewReviewIdAndDeletedFalseOrderByCreatedAtAsc(Long interviewReviewId, Pageable pageable) {
//        return findByInterviewReviewIdAndDeletedFalseOrderByCreatedAtAsc(interviewReviewId).stream()
//                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
//                .limit(pageable.getPageSize())
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void deleteById(Long id) {
//        store.remove(id);
//    }
//
//    @Override
//    public long countByInterviewReviewIdAndDeletedFalse(Long interviewReviewId) {
//        return store.values().stream()
//                .filter(comment -> Objects.equals(comment.getInterviewReview().getId(), interviewReviewId))
//                .filter(Comment::isActive)
//                .count();
//    }
//
//    // 테스트 헬퍼 메서드
//    public void clear() {
//        store.clear();
//        sequence = 1L;
//    }
//
//    public int size() {
//        return store.size();
//    }
//
//    // 리플렉션을 사용해서 ID를 설정하는 헬퍼 메서드
//    private void setId(Comment comment, Long id) {
//        try {
//            var field = Comment.class.getDeclaredField("id");
//            field.setAccessible(true);
//            field.set(comment, id);
//        } catch (Exception e) {
//            throw new RuntimeException("ID 설정 중 오류 발생", e);
//        }
//    }
//}