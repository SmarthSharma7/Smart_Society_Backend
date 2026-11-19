package com.tiet.smartsocieties.service;

import com.tiet.smartsocieties.dto.registration.RegistrationResponse;
import com.tiet.smartsocieties.dto.timetable.TimetableClashResponse;
import com.tiet.smartsocieties.entity.Event;
import com.tiet.smartsocieties.entity.EventRegistration;
import com.tiet.smartsocieties.entity.Enums;
import com.tiet.smartsocieties.entity.SocietyMember;
import com.tiet.smartsocieties.entity.User;
import com.tiet.smartsocieties.repository.EventRegistrationRepository;
import com.tiet.smartsocieties.repository.FeedbackRepository;
import com.tiet.smartsocieties.repository.EventRepository;
import com.tiet.smartsocieties.repository.SocietyMemberRepository;
import com.tiet.smartsocieties.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final SocietyMemberRepository societyMemberRepository;
    private final TimetableService timetableService;
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public RegistrationResponse register(Long userId, Long eventId, boolean confirmClashes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Enums.UserRole.STUDENT
                && user.getRole() != Enums.UserRole.SOCIETY_MEMBER) {
            throw new IllegalArgumentException("Only student and society member accounts can register for events");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        LocalDateTime now = LocalDateTime.now();
        if (event.getStatus() != Event.EventStatus.APPROVED) {
            throw new IllegalArgumentException("Registration is available only for approved events");
        }
        if (event.getStartDatetime() == null || !event.getStartDatetime().isAfter(now)) {
            throw new IllegalArgumentException("This event has already started or ended");
        }
        if (event.getRegistrationDeadline() != null && event.getRegistrationDeadline().isBefore(now)) {
            throw new IllegalArgumentException("The registration deadline has passed");
        }

        List<TimetableClashResponse> clashes = timetableService.getEventClashes(userId, eventId);
        if (!clashes.isEmpty() && !confirmClashes) {
            throw new TimetableConflictException(clashes);
        }

        EventRegistration registration = registrationRepository
                .findByEventEventIdAndStudentUserId(eventId, userId)
                .orElseGet(EventRegistration::new);

        if (registration.getRegistrationId() != null &&
                registration.getStatus() != EventRegistration.RegistrationStatus.CANCELLED) {
            throw new IllegalArgumentException("You are already registered for this event");
        }

        if (event.getMaxCapacity() != null) {
            long occupied = registrationRepository.countByEventEventIdAndStatusIn(
                    eventId,
                    List.of(EventRegistration.RegistrationStatus.REGISTERED,
                            EventRegistration.RegistrationStatus.ATTENDED));
            if (occupied >= event.getMaxCapacity()) {
                throw new IllegalArgumentException("This event has reached its registration capacity");
            }
        }

        registration.setEvent(event);
        registration.setStudent(user);
        registration.setStatus(EventRegistration.RegistrationStatus.REGISTERED);
        registration.setRegisteredAt(now);
        registration.setCancelledAt(null);
        return toResponse(registrationRepository.save(registration));
    }

    @Transactional(readOnly = true)
    public List<TimetableClashResponse> getEventClashes(Long userId, Long eventId) {
        return timetableService.getEventClashes(userId, eventId);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations(Long userId) {
        return registrationRepository.findAllForStudent(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getSocietyRegistrations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Enums.UserRole.SOCIETY_MEMBER) {
            throw new IllegalArgumentException("Only society members can view society registrations");
        }
        List<Long> societyIds = societyMemberRepository
                .findByUserUserIdAndStatus(userId, SocietyMember.MembershipStatus.ACTIVE)
                .stream().map(m -> m.getSociety().getSocietyId()).distinct().toList();
        if (societyIds.isEmpty()) return List.of();
        return registrationRepository.findAllForSocieties(societyIds).stream().map(this::toResponse).toList();
    }

    private RegistrationResponse toResponse(EventRegistration registration) {
        Event event = registration.getEvent();
        User student = registration.getStudent();
        return new RegistrationResponse(
                registration.getRegistrationId(), event.getEventId(), event.getTitle(),
                event.getSociety().getName(), event.getStartDatetime(), event.getEndDatetime(),
                event.getVenue(), registration.getStatus().name(), registration.getRegisteredAt(),
                student.getName(), student.getEmail(),
                feedbackRepository.existsByEventEventIdAndStudentUserId(event.getEventId(), student.getUserId()));
    }
}