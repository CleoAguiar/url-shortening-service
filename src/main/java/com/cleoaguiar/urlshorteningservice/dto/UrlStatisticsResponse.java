package com.cleoaguiar.urlshorteningservice.dto;

import java.time.Instant;

public record UrlStatisticsResponse (
        String shortCode,
        String originalUrl,
        Instant createdAt,
        Long accessCount
) {
}
