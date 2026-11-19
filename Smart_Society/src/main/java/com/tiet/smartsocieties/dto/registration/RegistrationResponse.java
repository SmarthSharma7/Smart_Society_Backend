package com.tiet.smartsocieties.dto.registration;

import java.time.LocalDateTime;

public record RegistrationResponse(
        Long registrationId,
        Long eventId,
        String eventTitle,
        String societyName,
        LocalDateTime startDatetime,
        LocalDateTime endDatetime,
        String venue,
        String status,
        LocalDateTime registeredAt,
        String studentName,
        String studentEmail,
        boolean feedbackSubmitted
) {}