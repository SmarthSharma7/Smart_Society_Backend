
package com.tiet.smartsocieties.dto.timetable;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimetableClashResponse(
        LocalDate date,
        String day,
        String courseName,
        String courseCode,
        String room,
        LocalTime classStartTime,
        LocalTime classEndTime,
        LocalTime eventStartTime,
        LocalTime eventEndTime
) {}
