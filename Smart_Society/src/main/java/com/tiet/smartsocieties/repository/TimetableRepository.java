
package com.tiet.smartsocieties.repository;

import com.tiet.smartsocieties.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimetableRepository
        extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findByStudentUserIdOrderByDayAscStartTimeAsc(
            Long studentId
    );

    Optional<TimetableEntry> findByTimetableIdAndStudentUserId(
            Long timetableId,
            Long studentId
    );

    boolean existsByStudentUserIdAndDayAndStartTimeLessThanAndEndTimeGreaterThan(
            Long studentId,
            TimetableEntry.TimetableDay day,
            java.time.LocalTime endTime,
            java.time.LocalTime startTime
    );

    List<TimetableEntry> findByStudentUserIdAndDay(
            Long studentId,
            TimetableEntry.TimetableDay day
    );
}
