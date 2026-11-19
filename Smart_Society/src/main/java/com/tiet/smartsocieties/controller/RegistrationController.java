
package com.tiet.smartsocieties.controller;

import com.tiet.smartsocieties.dto.registration.*;
import com.tiet.smartsocieties.dto.timetable.TimetableClashResponse;
import com.tiet.smartsocieties.entity.User;
import com.tiet.smartsocieties.repository.UserRepository;
import com.tiet.smartsocieties.service.RegistrationService;
import com.tiet.smartsocieties.service.TimetableConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserRepository userRepository;

    private Long currentUserId(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        ));

        return user.getUserId();
    }

    @PostMapping("/events/{eventId}")
    public ResponseEntity<?> register(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "false") boolean confirmClashes,
            Authentication authentication
    ) {

        try {
            return ResponseEntity.ok(
                    registrationService.register(
                            currentUserId(authentication),
                            eventId,
                            confirmClashes
                    )
            );

        } catch (TimetableConflictException ex) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(
                            new RegistrationConflictResponse(
                                    ex.getMessage(),
                                    ex.getClashes()
                            )
                    );

        } catch (IllegalArgumentException ex) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        }
    }

    @GetMapping("/events/{eventId}/clashes")
    public List<TimetableClashResponse> eventClashes(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return registrationService.getEventClashes(
                currentUserId(authentication),
                eventId
        );
    }

    @GetMapping("/my")
    public List<RegistrationResponse> myRegistrations(
            Authentication authentication
    ) {
        return registrationService.getMyRegistrations(
                currentUserId(authentication)
        );
    }

    @GetMapping("/society")
    public List<RegistrationResponse> societyRegistrations(
            Authentication authentication
    ) {
        return registrationService.getSocietyRegistrations(
                currentUserId(authentication)
        );
    }
}
