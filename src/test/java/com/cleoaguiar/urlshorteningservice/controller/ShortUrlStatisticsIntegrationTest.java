package com.cleoaguiar.urlshorteningservice.controller;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import com.cleoaguiar.urlshorteningservice.repository.ShortUrlRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ShortUrlStatisticsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String shortCode;
    private final String originalUrl = "https://example.com";

    @BeforeEach
    void setUp() {
        shortCode = "it" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);

        shortUrlRepository.save(
                new ShortUrl(originalUrl, shortCode)
        );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update(
                "DELETE FROM shortened_urls WHERE short_code = ?",
                shortCode
        );
    }

    @Test
    void shouldStartWithZeroAccessCount() {
        assertEquals(0L, getAccessCount());
    }

    @Test
    void shouldIncrementAccessCountAfterSuccessfulRedirection() throws Exception {
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        assertEquals(3L, getAccessCount());
    }

    @Test
    void shouldNotIncrementAccessCountWhenRetrievingStatistics() throws Exception {
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound());

        mockMvc.perform(get("/api/urls/{shortCode}/statistics", shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessCount").value(1));

        assertEquals(1L, getAccessCount());
    }

    @Test
    void shouldReturnExpectedStatisticsResponseStructure() throws Exception {
        mockMvc.perform(get("/api/urls/{shortCode}/statistics", shortCode))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.accessCount").value(0));
    }

    @Test
    void shouldNotChangeExistingStatisticsWhenRedirectingUnknownCode() throws Exception {
        mockMvc.perform(get("/{shortCode}", "missing-code"))
                .andExpect(status().isNotFound());

        assertEquals(0L, getAccessCount());
    }

    @Test
    void shouldReturnNotFoundWhenRetrievingStatisticsForUnknownCode() throws Exception {
        mockMvc.perform(get("/api/urls/{shortCode}/statistics", "missing-code"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private Long getAccessCount() {
        return jdbcTemplate.queryForObject(
                "SELECT access_count FROM shortened_urls WHERE short_code = ?",
                Long.class,
                shortCode
        );
    }
}
