package com.tiet.smartsocieties.controller;

import com.tiet.smartsocieties.dto.event.EventRequest;
import com.tiet.smartsocieties.dto.event.EventResponse;
import com.tiet.smartsocieties.entity.Event;
import com.tiet.smartsocieties.entity.User;
import com.tiet.smartsocieties.repository.UserRepository;
import com.tiet.smartsocieties.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/society-member")
@RequiredArgsConstructor
public class SocietyMemberController {

    private final EventService eventService;
    private final UserRepository userRepository;

    @PostMapping("/events")
    public EventResponse createEvent(
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Event event = eventService.createSocietyMemberEvent(
                user.getUserId(),
                request
        );

        return new EventResponse(
                event.getEventId(),
                event.getSociety().getSocietyId(),
                event.getSociety().getName(),
                event.getCategory().getCategoryId(),
                event.getCategory().getName(),
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getStartDatetime(),
                event.getEndDatetime(),
                event.getRegistrationDeadline(),
                event.getMaxCapacity(),
                event.getStatus().name()
        );
    }

    @GetMapping("/events/{id}/can-delete")
    public ResponseEntity<Boolean> canDeleteEvent(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(
                eventService.canDeleteSocietyMemberEvent(
                        user.getUserId(),
                        id
                )
        );
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<String> deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        eventService.deleteSocietyMemberEvent(
                user.getUserId(),
                id
        );

        return ResponseEntity.ok("Event deleted successfully");
    }
}