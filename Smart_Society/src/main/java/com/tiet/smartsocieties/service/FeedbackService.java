package com.tiet.smartsocieties.service;

import com.tiet.smartsocieties.dto.feedback.FeedbackRequest;
import com.tiet.smartsocieties.dto.feedback.FeedbackResponse;
import com.tiet.smartsocieties.entity.*;
import com.tiet.smartsocieties.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final FeedbackRepository feedbackRepository;
    private final SocietyMemberRepository societyMemberRepository;

    @Transactional
    public FeedbackResponse submit(Long userId, Long eventId, FeedbackRequest request) {
        if (request == null || request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Enums.UserRole.STUDENT && user.getRole() != Enums.UserRole.SOCIETY_MEMBER) {
            throw new IllegalArgumentException("Only students and society members can submit feedback");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (event.getEndDatetime() == null || event.getEndDatetime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Feedback is available after the event has ended");
        }
        EventRegistration registration = registrationRepository
                .findByEventEventIdAndStudentUserId(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("You must have registered for this event to submit feedback"));
        if (registration.getStatus() != EventRegistration.RegistrationStatus.REGISTERED
                && registration.getStatus() != EventRegistration.RegistrationStatus.ATTENDED) {
            throw new IllegalArgumentException("Only users with an active or attended registration can submit feedback");
        }
        if (feedbackRepository.existsByEventEventIdAndStudentUserId(eventId, userId)) {
            throw new IllegalArgumentException("You have already submitted feedback for this event");
        }

        Feedback feedback = new Feedback();
        feedback.setEvent(event);
        feedback.setStudent(user);
        feedback.setRating(request.rating());
        feedback.setComment(request.comment() == null || request.comment().isBlank() ? null : request.comment().trim());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        return toResponse(feedbackRepository.save(feedback));
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getSocietyFeedback(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Enums.UserRole.SOCIETY_MEMBER) {
            throw new IllegalArgumentException("Only society members can view society feedback");
        }
        List<Long> societyIds = societyMemberRepository
                .findByUserUserIdAndStatus(userId, SocietyMember.MembershipStatus.ACTIVE)
                .stream().map(m -> m.getSociety().getSocietyId()).distinct().toList();
        if (societyIds.isEmpty()) return List.of();
        return feedbackRepository.findAllForSocieties(societyIds).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getMyFeedback(Long userId, Long eventId) {
        return feedbackRepository.findByEventEventIdAndStudentUserId(eventId, userId)
                .map(this::toResponse).orElse(null);
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        Event event = feedback.getEvent();
        User student = feedback.getStudent();
        return new FeedbackResponse(feedback.getFeedbackId(), event.getEventId(), event.getTitle(),
                event.getSociety().getName(), feedback.getRating(), feedback.getComment(),
                feedback.getCreatedAt(), student.getName(), student.getEmail());
    }
}