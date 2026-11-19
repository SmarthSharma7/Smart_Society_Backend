package com.tiet.smartsocieties.controller;

import com.tiet.smartsocieties.dto.feedback.FeedbackRequest;
import com.tiet.smartsocieties.dto.feedback.FeedbackResponse;
import com.tiet.smartsocieties.entity.User;
import com.tiet.smartsocieties.repository.UserRepository;
import com.tiet.smartsocieties.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    private Long currentUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getUserId();
    }

    @PostMapping("/events/{eventId}")
    public FeedbackResponse submit(@PathVariable Long eventId,
                                   @Valid @RequestBody FeedbackRequest request,
                                   Authentication authentication) {
        try {
            return feedbackService.submit(currentUserId(authentication), eventId, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/events/{eventId}/my")
    public FeedbackResponse myFeedback(@PathVariable Long eventId, Authentication authentication) {
        return feedbackService.getMyFeedback(currentUserId(authentication), eventId);
    }

    @GetMapping("/society")
    public List<FeedbackResponse> societyFeedback(Authentication authentication) {
        try {
            return feedbackService.getSocietyFeedback(currentUserId(authentication));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
    }
}