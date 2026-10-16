package com.cleoaguiar.urlshorteningservice.service;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import com.cleoaguiar.urlshorteningservice.dto.UrlStatisticsResponse;
import com.cleoaguiar.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.cleoaguiar.urlshorteningservice.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShortUrlServiceStatisticsTest {
    @Mock
    private ShortUrlRepository shortUrlRepository;

    @InjectMocks
    private ShortUrlService shortUrlService;

    @Test
    void shouldReturnStatisticsForExistingShortCode() {
        String originalUrl = "https://exemple.com";
        String shortCode = "abc1234";
        Long accessCount = 12L;
        Instant createdAt = Instant.parse("2026-10-14T18:30:00Z");

        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode);

        shortUrl.setCreatedAt(createdAt);
        shortUrl.setAccessCount(accessCount);

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(shortUrl));

        UrlStatisticsResponse response = shortUrlService.getStatistics(shortCode);

        assertEquals(shortCode, response.shortCode());
        assertEquals(originalUrl, response.originalUrl());
        assertEquals(createdAt, response.createdAt());
        assertEquals(accessCount, response.accessCount());

        verify(shortUrlRepository).findByShortCode(shortCode);
    }

    @Test
    void shouldNotIncrementAccessCountWhenRetrievingStatistics() {
        String originalUrl = "https://exemple.com";
        String shortCode = "abc1234";
        Long initialAccessCount = 12L;

        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode);

        shortUrl.setAccessCount(initialAccessCount);

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(shortUrl));

        shortUrlService.getStatistics(shortCode);

        assertEquals(initialAccessCount, shortUrl.getAccessCount());

        verify(shortUrlRepository).findByShortCode(shortCode);
    }

    @Test
    void shouldThrowExceptionWhenShortCodeDoesNotExist() {
        String shortCode = "missing";

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        assertThrows(ShortUrlNotFoundException.class, () -> shortUrlService.getStatistics(shortCode));

        verify(shortUrlRepository).findByShortCode(shortCode);
    }
}
