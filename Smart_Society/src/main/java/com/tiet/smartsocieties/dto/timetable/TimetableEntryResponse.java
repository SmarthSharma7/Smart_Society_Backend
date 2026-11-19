
package com.tiet.smartsocieties.dto.timetable;

import java.time.LocalTime;

public record TimetableEntryResponse(
        Long timetableId,
        String day,
        LocalTime startTime,
        LocalTime endTime,
        String courseName,
        String courseCode,
        String room
) {}
