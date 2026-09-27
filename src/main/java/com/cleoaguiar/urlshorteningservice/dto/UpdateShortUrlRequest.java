package com.cleoaguiar.urlshorteningservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UpdateShortUrlRequest(
        @NotBlank(message = "Original URL is required")
        @URL(message = "Original URL must be valid")
        String originalUrl
) {
}
