package com.cleoaguiar.urlshorteningservice.dto;

import java.time.Instant;

public record ShortenUrlResponse(
        Long id,
        String originalUrl,
        String shortCode,
        Instant createdAt
) {
}
