package com.tiet.smartsocieties.service;

import com.tiet.smartsocieties.dto.notification.EventNotificationResponse;
import com.tiet.smartsocieties.entity.Event;
import com.tiet.smartsocieties.entity.EventView;
import com.tiet.smartsocieties.entity.User;
import com.tiet.smartsocieties.repository.EventRepository;
import com.tiet.smartsocieties.repository.EventViewRepository;
import com.tiet.smartsocieties.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventNotificationService {

    private final EventRepository eventRepository;
    private final EventViewRepository eventViewRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<EventNotificationResponse> getNotifications(Long userId) {

        List<Event> unviewedEvents =
                eventRepository.findUnviewedUpcomingEventsForUser(
                        userId,
                        Event.EventStatus.APPROVED,
                        LocalDateTime.now()
                );

        return unviewedEvents.stream()
                .map(event -> new EventNotificationResponse(
                        event.getEventId(),
                        event.getTitle(),
                        event.getCategory().getName()
                ))
                .toList();
    }

    @Transactional
    public void markEventViewed(
            Long userId,
            Long eventId,
            String source
    ) {

        if (eventViewRepository.existsForUserAndEvent(userId, eventId)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Event not found"));

        EventView eventView = new EventView();

        eventView.setStudent(user);
        eventView.setEvent(event);
        eventView.setViewedAt(LocalDateTime.now());
        eventView.setSource(normalizeSource(source));

        eventViewRepository.save(eventView);
    }

    private String normalizeSource(String source) {

        if (source == null || source.isBlank()) {
            return "DETAIL";
        }

        String normalized = source.trim().toUpperCase();

        return normalized.length() > 50
                ? normalized.substring(0, 50)
                : normalized;
    }
}