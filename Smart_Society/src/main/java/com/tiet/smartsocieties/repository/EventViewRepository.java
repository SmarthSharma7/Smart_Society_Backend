package com.tiet.smartsocieties.repository;

import com.tiet.smartsocieties.entity.EventView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventViewRepository
        extends JpaRepository<EventView, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(ev) > 0 THEN TRUE ELSE FALSE END
            FROM EventView ev
            WHERE ev.student.userId = :userId
              AND ev.event.eventId = :eventId
            """)
    boolean existsForUserAndEvent(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId
    );
}