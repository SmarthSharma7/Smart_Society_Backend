
package com.tiet.smartsocieties.dto.timetable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record TimetableEntryRequest(
        @NotNull String day,
        @NotNull LocalTime startTime,
        @NotBlank @Size(max = 150) String courseName,
        @Size(max = 50) String courseCode,
        @Size(max = 50) String room
) {}
