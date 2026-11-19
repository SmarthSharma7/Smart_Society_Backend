package com.tiet.smartsocieties.repository;

import com.tiet.smartsocieties.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusAndStartDatetimeAfterOrderByStartDatetimeAsc(
            Event.EventStatus status,
            LocalDateTime now
    );

    List<Event> findByStatusAndTitleContainingIgnoreCaseOrderByStartDatetimeAsc(
            Event.EventStatus status,
            String q
    );

    List<Event> findBySocietySocietyIdOrderByStartDatetimeDesc(
            Long societyId
    );

    @Query("""
            SELECT e
            FROM Event e
            JOIN FETCH e.category
            WHERE e.status = :status
              AND e.startDatetime > :now
              AND NOT EXISTS (
                  SELECT ev.viewId
                  FROM EventView ev
                  WHERE ev.student.userId = :userId
                    AND ev.event.eventId = e.eventId
              )
            ORDER BY e.startDatetime ASC
            """)
    List<Event> findUnviewedUpcomingEventsForUser(
            @Param("userId") Long userId,
            @Param("status") Event.EventStatus status,
            @Param("now") LocalDateTime now
    );
}