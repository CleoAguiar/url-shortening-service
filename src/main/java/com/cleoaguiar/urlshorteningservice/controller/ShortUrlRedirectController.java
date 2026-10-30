package com.cleoaguiar.urlshorteningservice.controller;

import com.cleoaguiar.urlshorteningservice.dto.ShortenUrlResponse;
import com.cleoaguiar.urlshorteningservice.service.ShortUrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class ShortUrlRedirectController {
    private final ShortUrlService shortUrlService;

    public ShortUrlRedirectController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode
    ) {
        ShortenUrlResponse response = shortUrlService.findByShortCode(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(response.originalUrl()))
                .build();
    }
}
