package ru.practicum.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    List<Event> findAllByCategoryId(Long categoryId);

    Set<Event> findByIdIn(Set<Long> eventIds);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND ((CAST(:rangeStart AS date) IS NULL AND CAST(:rangeEnd AS date) IS NULL) " +
            "OR (CAST(:rangeStart AS date) IS NULL AND CAST(:rangeEnd AS date) >= e.eventDate) " +
            "OR (CAST(:rangeStart AS date) <= e.eventDate AND CAST(:rangeEnd AS date) IS NULL) " +
            "OR e.eventDate BETWEEN CAST(:rangeStart AS date) AND CAST(:rangeEnd AS date)) " +
            "GROUP BY e.id " +
            "ORDER BY e.id ASC")
    List<Event> getEventsByAdmin(@Param("users") List<Long> users,
                                 @Param("states") List<EventState> states,
                                 @Param("categories") List<Long> categories,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 PageRequest pageRequest);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE (e.state = 'PUBLISHED') " +
            "AND ((:text IS NULL) " +
            "OR (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "OR (LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "OR (LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%')))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND ((CAST(:rangeStart AS date) IS NULL AND CAST(:rangeEnd AS date) IS NULL) " +
            "OR (CAST(:rangeStart AS date) IS NULL AND CAST(:rangeEnd AS date) >= e.eventDate) " +
            "OR (CAST(:rangeStart AS date) <= e.eventDate AND CAST(:rangeEnd AS date) IS NULL) " +
            "OR e.eventDate BETWEEN CAST(:rangeStart AS date) AND CAST(:rangeEnd AS date) " +
            "OR (e.participantLimit > e.confirmedRequests OR :onlyAvailable = FALSE)) " +
            "GROUP BY e.id " +
            "ORDER BY LOWER(:sort) ASC")
    List<Event> getEventsByPublic(@Param("text") String text,
                                  @Param("categories") List<Long> categories,
                                  @Param("paid") Boolean paid,
                                  @Param("rangeStart") LocalDateTime rangeStart,
                                  @Param("rangeEnd") LocalDateTime rangeEnd,
                                  @Param("onlyAvailable") Boolean onlyAvailable,
                                  @Param("sort") String sort,
                                  PageRequest pageRequest);



}
