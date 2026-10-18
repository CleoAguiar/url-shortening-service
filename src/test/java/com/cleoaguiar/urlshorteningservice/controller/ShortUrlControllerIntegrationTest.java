package com.cleoaguiar.urlshorteningservice.controller;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import com.cleoaguiar.urlshorteningservice.repository.ShortUrlRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ShortUrlControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Set<String> testUrls = new HashSet<>();
    private final Set<String> createdShortCodes = new HashSet<>();

    @AfterEach
    void tearDown() {
        for (String shortCode : createdShortCodes) {
            jdbcTemplate.update(
                    "DELETE FROM shortened_urls WHERE short_code = ?",
                    shortCode
            );
        }

        for (String originalUrl : testUrls) {
            jdbcTemplate.update(
                    "DELETE FROM shortened_urls WHERE original_url = ?",
                    originalUrl
            );
        }

        createdShortCodes.clear();
        testUrls.clear();
    }

    @Test
    void shouldCreateShortUrlAndPersistIt() throws Exception {
        String originalUrl = "https://example.com/integration-test/" + UUID.randomUUID();

        testUrls.add(originalUrl);

        MvcResult result = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "%s"
                                }
                                """.formatted(originalUrl)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortCode").isString())
                .andExpect(jsonPath("$.createdAt").isString())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String shortCode = JsonPath.read(responseBody, "$.shortCode");

        createdShortCodes.add(shortCode);

        ShortUrl persistedUrl = shortUrlRepository
                .findByShortCode(shortCode)
                .orElseThrow();

        assertNotNull(persistedUrl.getId());
        assertEquals(originalUrl, persistedUrl.getOriginalUrl());
        assertEquals(shortCode, persistedUrl.getShortCode());
        assertNotNull(persistedUrl.getCreatedAt());
        assertEquals(0L, persistedUrl.getAccessCount());
    }

    @Test
    void shouldRetrieveShortUrlByShortCode() throws Exception {
        String originalUrl = "https://example.com/retrieval-test/" + UUID.randomUUID();

        testUrls.add(originalUrl);

        MvcResult createResult = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "%s"
                                }
                                """.formatted(originalUrl)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        String shortCode = JsonPath.read(createResponse, "$.shortCode");

        createdShortCodes.add(shortCode);

        mockMvc.perform(get("/api/urls/{shortCode}", shortCode))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.createdAt").isString());

        ShortUrl persistedUrl = shortUrlRepository
                .findByShortCode(shortCode)
                .orElseThrow();

        assertEquals(originalUrl, persistedUrl.getOriginalUrl());
        assertEquals(shortCode, persistedUrl.getShortCode());
        assertEquals(1L, persistedUrl.getAccessCount());
    }

    @Test
    void shouldUpdateShortUrlAndPersistChanges() throws Exception {
        String originalUrl = "https://example.com/update-test/" + UUID.randomUUID();
        String updatedUrl = "https://example.com/updated-test/" + UUID.randomUUID();

        testUrls.add(originalUrl);
        testUrls.add(updatedUrl);

        MvcResult createResult = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "%s"
                                }
                                """.formatted(originalUrl)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        String shortCode = JsonPath.read(createResponse, "$.shortCode");

        createdShortCodes.add(shortCode);

        mockMvc.perform(put("/api/urls/{shortCode}", shortCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "%s"
                                }
                                """.formatted(updatedUrl)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.originalUrl").value(updatedUrl))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.createdAt").isString());

        ShortUrl persistedUrl = shortUrlRepository
                .findByShortCode(shortCode)
                .orElseThrow();

        assertEquals(updatedUrl, persistedUrl.getOriginalUrl());
        assertEquals(shortCode, persistedUrl.getShortCode());
        assertEquals(0L, persistedUrl.getAccessCount());
    }

    @Test
    void shouldDeleteShortUrlAndConfirmItNoLongerExists() throws Exception {
        String originalUrl = "https://example.com/delete-test/" + UUID.randomUUID();

        testUrls.add(originalUrl);

        MvcResult createResult = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "%s"
                                }
                                """.formatted(originalUrl)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        String shortCode = JsonPath.read(createResponse, "$.shortCode");

        createdShortCodes.add(shortCode);

        mockMvc.perform(delete("/api/urls/{shortCode}", shortCode))
                .andExpect(status().isNoContent());

        assertTrue(shortUrlRepository.findByShortCode(shortCode).isEmpty());
    }
}
