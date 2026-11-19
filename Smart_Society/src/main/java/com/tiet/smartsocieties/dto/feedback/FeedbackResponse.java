package com.tiet.smartsocieties.dto.feedback;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long feedbackId,
        Long eventId,
        String eventTitle,
        String societyName,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        String studentName,
        String studentEmail
) {}