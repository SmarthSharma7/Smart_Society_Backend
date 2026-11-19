
package com.tiet.smartsocieties.service;

import com.tiet.smartsocieties.dto.timetable.*;
import com.tiet.smartsocieties.entity.Event;
import com.tiet.smartsocieties.entity.TimetableEntry;
import com.tiet.smartsocieties.entity.User;
import com.tiet.smartsocieties.repository.EventRepository;
import com.tiet.smartsocieties.repository.TimetableRepository;
import com.tiet.smartsocieties.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    private static final LocalTime FIRST_SLOT =
            LocalTime.of(8, 0);

    private static final int SLOT_MINUTES = 50;
    private static final int SLOT_COUNT = 12;

    @Transactional(readOnly = true)
    public List<TimetableEntryResponse> getMyTimetable(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        requireTimetableRole(user);

        return timetableRepository
                .findByStudentUserIdOrderByDayAscStartTimeAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TimetableEntryResponse addEntry(
            Long userId,
            TimetableEntryRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        requireTimetableRole(user);

        TimetableEntry.TimetableDay day;

        try {
            day = TimetableEntry.TimetableDay.valueOf(
                    request.day().toUpperCase(Locale.ROOT)
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Select a valid weekday from Monday to Friday"
            );
        }

        if (day == TimetableEntry.TimetableDay.SATURDAY ||
                day == TimetableEntry.TimetableDay.SUNDAY) {

            throw new IllegalArgumentException(
                    "Classes can only be added Monday to Friday"
            );
        }

        LocalTime start = request.startTime();

        boolean validSlot = false;

        for (int i = 0; i < SLOT_COUNT; i++) {

            LocalTime slotStart =
                    FIRST_SLOT.plusMinutes((long) i * SLOT_MINUTES);

            if (slotStart.equals(start)) {
                validSlot = true;
                break;
            }
        }

        if (!validSlot) {
            throw new IllegalArgumentException(
                    "Choose one of the 12 available 50-minute slots"
            );
        }

        LocalTime end = start.plusMinutes(SLOT_MINUTES);

        boolean overlaps = timetableRepository
                .existsByStudentUserIdAndDayAndStartTimeLessThanAndEndTimeGreaterThan(
                        userId,
                        day,
                        end,
                        start
                );

        if (overlaps) {
            throw new IllegalArgumentException(
                    "This timetable slot already has a class"
            );
        }

        TimetableEntry entry = new TimetableEntry();

        entry.setStudent(user);
        entry.setDay(day);
        entry.setStartTime(start);
        entry.setEndTime(end);
        entry.setCourseName(request.courseName().trim());
        entry.setCourseCode(blankToNull(request.courseCode()));
        entry.setRoom(blankToNull(request.room()));

        return toResponse(timetableRepository.save(entry));
    }

    @Transactional
    public void deleteEntry(Long userId, Long timetableId) {

        TimetableEntry entry = timetableRepository
                .findByTimetableIdAndStudentUserId(
                        timetableId,
                        userId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Timetable entry not found"
                        ));

        timetableRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<TimetableClashResponse> getEventClashes(
            Long userId,
            Long eventId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        requireTimetableRole(user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Event not found"));

        if (event.getStartDatetime() == null ||
                event.getEndDatetime() == null) {
            return List.of();
        }

        List<TimetableClashResponse> clashes =
                new ArrayList<>();

        LocalDate date =
                event.getStartDatetime().toLocalDate();

        LocalDate lastDate =
                event.getEndDatetime().toLocalDate();

        while (!date.isAfter(lastDate)) {

            DayOfWeek javaDay = date.getDayOfWeek();

            if (javaDay.getValue() <= 5) {

                TimetableEntry.TimetableDay day =
                        TimetableEntry.TimetableDay.valueOf(
                                javaDay.name()
                        );

                LocalDateTime eventStart =
                        date.equals(event.getStartDatetime().toLocalDate())
                                ? event.getStartDatetime()
                                : date.atStartOfDay();

                LocalDateTime eventEnd =
                        date.equals(event.getEndDatetime().toLocalDate())
                                ? event.getEndDatetime()
                                : date.atTime(LocalTime.MAX);

                List<TimetableEntry> classes =
                        timetableRepository.findByStudentUserIdAndDay(
                                userId,
                                day
                        );

                for (TimetableEntry entry : classes) {

                    boolean overlaps =
                            entry.getStartTime().isBefore(
                                    eventEnd.toLocalTime()
                            ) &&
                                    entry.getEndTime().isAfter(
                                            eventStart.toLocalTime()
                                    );

                    if (overlaps) {

                        clashes.add(
                                new TimetableClashResponse(
                                        date,
                                        day.name(),
                                        entry.getCourseName(),
                                        entry.getCourseCode(),
                                        entry.getRoom(),
                                        entry.getStartTime(),
                                        entry.getEndTime(),
                                        eventStart.toLocalTime(),
                                        eventEnd.toLocalTime()
                                )
                        );
                    }
                }
            }

            date = date.plusDays(1);
        }

        return clashes;
    }

    private void requireTimetableRole(User user) {

        if (user.getRole() !=
                com.tiet.smartsocieties.entity.Enums.UserRole.STUDENT
                &&
                user.getRole() !=
                        com.tiet.smartsocieties.entity.Enums.UserRole.SOCIETY_MEMBER) {

            throw new IllegalArgumentException(
                    "Only students and society members can manage a timetable"
            );
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private TimetableEntryResponse toResponse(
            TimetableEntry entry
    ) {

        return new TimetableEntryResponse(
                entry.getTimetableId(),
                entry.getDay().name(),
                entry.getStartTime(),
                entry.getEndTime(),
                entry.getCourseName(),
                entry.getCourseCode(),
                entry.getRoom()
        );
    }
}
