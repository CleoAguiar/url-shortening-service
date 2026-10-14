package com.cleoaguiar.urlshorteningservice.service;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import com.cleoaguiar.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.cleoaguiar.urlshorteningservice.repository.ShortUrlRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@ActiveProfiles("test")
public class ShortUrlAccessCountIntegrationTest {
    @Autowired
    private ShortUrlService shortUrlService;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String shortCode;

    @BeforeEach
    void setUp() {
        shortCode = "it" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);

        shortUrlRepository.save(
                new ShortUrl("https://exemple.com", shortCode)
        );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update(
                "DELETE FROM shortened_urls WHERE short_code = ?", shortCode
        );
    }

    @Test
    void shouldInitializeAccessCountToZero() {
        Long accessCount = getAccessCount(shortCode);
        assertEquals(0L, accessCount);
    }

    @Test
    void shouldPersistAccessCountAfterMultipleSuccessfulAccesses() {
        shortUrlService.findByShortCode(shortCode);
        shortUrlService.findByShortCode(shortCode);

        Long accessCount = getAccessCount(shortCode);

        assertEquals(2L, accessCount);
    }

    @Test
    void shouldNotIncrementAccessCountWhenShortCodeDoesNotExist() {
        String nonexistentCode = "missing";

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> shortUrlService.findByShortCode(nonexistentCode)
        );

        Long accessCount = getAccessCount(shortCode);

        assertEquals(0L, accessCount);
    }

    private Long getAccessCount(String code) {
        return jdbcTemplate.queryForObject(
                "SELECT access_count FROM shortened_urls WHERE short_code = ?", Long.class, code
        );
    }
}
