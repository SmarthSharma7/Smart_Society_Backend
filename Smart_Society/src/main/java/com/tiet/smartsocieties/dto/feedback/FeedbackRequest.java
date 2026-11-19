package com.tiet.smartsocieties.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record FeedbackRequest(
        @Min(1) @Max(5) Integer rating,
        @Size(max = 5000) String comment
) {}