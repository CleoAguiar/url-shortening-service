package com.cleoaguiar.urlshorteningservice.controller;

import com.cleoaguiar.urlshorteningservice.dto.ShortenUrlResponse;
import com.cleoaguiar.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.cleoaguiar.urlshorteningservice.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortUrlController.class)
public class ShortUrlControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    void shouldReturnCreatedWhenUrlIsShortened() throws Exception {
        String originalUrl = "https://example.com";
        String shortCode = "abc1234";
        Instant createdAt = Instant.parse("2026-09-24T22:00:00Z");

        ShortenUrlResponse response = new ShortenUrlResponse(
                1L,
                originalUrl,
                shortCode,
                createdAt
        );

        when(shortUrlService.shorten(originalUrl))
                .thenReturn(response);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "https://example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

    }

    @Test
    void shouldReturnBadRequestWhenOriginalUrlIsInvalid() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "invalid-url"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(shortUrlService);
    }

    @Test
    void shouldReturnShortUrlWhenShortCodeExists() throws Exception {
        String originalUrl = "https://example.com";
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

        mockMvc.perform(get("/api/urls/{shortCode}", shortCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "https://example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

        verify(shortUrlService).findByShortCode(shortCode);
    }

    @Test
    void shouldReturnNotFoundWhenShortCodeDoesNotExist() throws Exception {
        String shortCode = "unknown";

        when(shortUrlService.findByShortCode(shortCode))
                .thenThrow(new ShortUrlNotFoundException(
                        "Short URL not found for code: " + shortCode
                ));

        mockMvc.perform(get("/api/urls/{shortCode}", shortCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Short URL not found for code: " + shortCode))
                .andExpect(jsonPath("$.path").value("/api/urls/" + shortCode));

        verify(shortUrlService).findByShortCode(shortCode);
    }

    @Test
    void shouldUpdateShortUrlSuccessfully() throws Exception {
        String newUrl = "https://new.com";
        String shortCode = "abc1234";
        Instant createdAt = Instant.parse("2026-09-24T22:00:00Z");

        ShortenUrlResponse response = new ShortenUrlResponse(
                1L,
                newUrl,
                shortCode,
                createdAt
        );

        when(shortUrlService.update(shortCode, newUrl))
                .thenReturn(response);

        mockMvc.perform(put("/api/urls/{shortCode}", shortCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "https://new.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originalUrl").value(newUrl))
                .andExpect(jsonPath("$.shortCode").value(shortCode));

    }

    @Test
    void shouldReturnBadRequestWhenUrlIsInvalid() throws Exception {
        String shortCode = "abc1234";

        mockMvc.perform(put("/api/urls/{shortCode}", shortCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "invalid-url"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(shortUrlService);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentShortCode() throws Exception {
        String shortCode = "unknown";
        String newUrl = "https://new.com";

        when(shortUrlService.update(shortCode, newUrl))
                .thenThrow(new ShortUrlNotFoundException(
                        "Short URL not found for code: " + shortCode
                ));

        mockMvc.perform(put("/api/urls/{shortCode}", shortCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "https://new.com"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

}
