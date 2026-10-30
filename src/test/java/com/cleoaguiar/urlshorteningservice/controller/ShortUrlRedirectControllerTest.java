package com.cleoaguiar.urlshorteningservice.controller;

import com.cleoaguiar.urlshorteningservice.dto.ShortenUrlResponse;
import com.cleoaguiar.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.cleoaguiar.urlshorteningservice.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortUrlRedirectController.class)
public class ShortUrlRedirectControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    void shouldRedirectToOriginalUrlWhenShortCodeExists() throws Exception {
        String originalUrl  = "https://example.com";
        String shortCode = "abc1234";
        Instant createdAt = Instant.parse("2026-09-24T22:00:00Z");

        ShortenUrlResponse response = new ShortenUrlResponse(
                1L,
                originalUrl,
                shortCode,
                createdAt
        );

        when(shortUrlService.findByShortCode(shortCode))
                .thenReturn(response);

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        verify(shortUrlService).findByShortCode(shortCode);
    }

    @Test
    void shouldReturnNotFoundWhenRedirectingNonexistentShortCode() throws Exception {
        String shortCode = "unknown";

        when(shortUrlService.findByShortCode(shortCode))
                .thenThrow(new ShortUrlNotFoundException(
                        "Short URL not found for code: " + shortCode
                ));

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isNotFound());

        verify(shortUrlService).findByShortCode(shortCode);
    }
}
