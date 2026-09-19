package com.cleoaguiar.urlshorteningservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ShortenUrlRequest (
        @NotBlank(message = "Original URL is required")
        @URL(message = "Original URL must be a valid URL")
        String originalUrl
){
}
