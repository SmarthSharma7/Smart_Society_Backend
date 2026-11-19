
package com.tiet.smartsocieties.service;

import com.tiet.smartsocieties.dto.timetable.TimetableClashResponse;
import java.util.List;

public class TimetableConflictException extends RuntimeException {

    private final List<TimetableClashResponse> clashes;

    public TimetableConflictException(
            List<TimetableClashResponse> clashes
    ) {
        super(
                "This event conflicts with classes in your timetable. Confirm to continue."
        );

        this.clashes = clashes;
    }

    public List<TimetableClashResponse> getClashes() {
        return clashes;
    }
}
