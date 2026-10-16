package com.cleoaguiar.urlshorteningservice.controller;

import com.cleoaguiar.urlshorteningservice.dto.UrlStatisticsResponse;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortUrlController.class)
public class ShortUrlControllerStatisticsTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    void shouldReturnStatisticsWhenShortCodeExists() throws Exception {
        String originalUrl = "https://exemple.com";
        String shortCode = "abc1234";
        Long accessCount = 12L;
        Instant createdAt = Instant.parse("2026-10-14T18:30:00Z");

        UrlStatisticsResponse response = new UrlStatisticsResponse(
                shortCode,
                originalUrl,
                createdAt,
                accessCount
        );

        when(shortUrlService.getStatistics(shortCode))
                .thenReturn(response);

        mockMvc.perform(get("/api/urls/{shortCode}/statistics", shortCode))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.accessCount").value(accessCount));
    }

    @Test
    void shouldReturnNotFoundWhenShortCodeDoesNotExist() throws Exception {
        String shortCode = "missing";

        when(shortUrlService.getStatistics(shortCode))
                .thenThrow(new ShortUrlNotFoundException(
                        "Short URL not found: " + shortCode
                ));

        mockMvc.perform(get("/api/urls/{shortCode}/statistics", shortCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Short URL not found: " + shortCode))
                .andExpect(jsonPath("$.path")
                        .value("/api/urls/" + shortCode + "/statistics"));

        verify(shortUrlService).getStatistics(shortCode);
    }
}
