package ru.practicum.comments.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.comments.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEvent_IdOrderByCreatedDesc(Long eventId, PageRequest pageRequest);

    List<Comment> findAllByAuthor_IdOrderByCreatedDesc(Long userId, PageRequest pageRequest);

    @Query("SELECT c " +
            "FROM Comment c " +
            "WHERE (CAST(:rangeStart AS date) IS NULL AND CAST(:rangeEnd AS date) IS NULL) " +
            "OR (CAST(:rangeStart AS date) IS NULL AND CAST(:rangeEnd AS date) >= c.created) " +
            "OR (CAST(:rangeStart AS date) <= c.created AND CAST(:rangeEnd AS date) IS NULL) " +
            "OR c.created BETWEEN CAST(:rangeStart AS date) AND CAST(:rangeEnd AS date) " +
            "ORDER BY c.created DESC")
    List<Comment> getCommentsByAdmin(@Param("rangeStart") LocalDateTime rangeStart,
                                     @Param("rangeEnd") LocalDateTime rangeEnd,
                                     PageRequest pageRequest);
}
