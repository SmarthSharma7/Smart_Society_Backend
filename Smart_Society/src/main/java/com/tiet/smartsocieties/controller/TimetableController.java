
package com.tiet.smartsocieties.controller;

import com.tiet.smartsocieties.dto.timetable.*;
import com.tiet.smartsocieties.entity.User;
import com.tiet.smartsocieties.repository.UserRepository;
import com.tiet.smartsocieties.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;
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

    @GetMapping("/my")
    public List<TimetableEntryResponse> myTimetable(
            Authentication authentication
    ) {
        return timetableService.getMyTimetable(
                currentUserId(authentication)
        );
    }

    @PostMapping("/my")
    @ResponseStatus(HttpStatus.CREATED)
    public TimetableEntryResponse addEntry(
            @Valid @RequestBody TimetableEntryRequest request,
            Authentication authentication
    ) {

        try {
            return timetableService.addEntry(
                    currentUserId(authentication),
                    request
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        }
    }

    @DeleteMapping("/my/{timetableId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(
            @PathVariable Long timetableId,
            Authentication authentication
    ) {

        try {
            timetableService.deleteEntry(
                    currentUserId(authentication),
                    timetableId
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ex.getMessage()
            );
        }
    }
}
