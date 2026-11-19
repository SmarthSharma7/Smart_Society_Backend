
package com.tiet.smartsocieties.dto.registration;

import com.tiet.smartsocieties.dto.timetable.TimetableClashResponse;
import java.util.List;

public record RegistrationConflictResponse(
        String message,
        List<TimetableClashResponse> clashes
) {}
