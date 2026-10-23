package com.cleoaguiar.urlshorteningservice.service;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import com.cleoaguiar.urlshorteningservice.dto.ShortenUrlResponse;
import com.cleoaguiar.urlshorteningservice.exception.ShortCodeGenerationException;
import com.cleoaguiar.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.cleoaguiar.urlshorteningservice.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShortUrlServiceTest {
    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private ShortCodeService shortCodeService;

    @InjectMocks
    private ShortUrlService shortUrlService;

    @Test
    void shouldShortenUrlSuccessfully() {
        String originalUrl = "https://exemple.com";
        String shortCode = "abc1234";

        when(shortCodeService.generateUniqueCode())
                .thenReturn(shortCode);

        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShortenUrlResponse response = shortUrlService.shorten(originalUrl);

        assertEquals(originalUrl, response.originalUrl());
        assertEquals(shortCode, response.shortCode());

        verify(shortCodeService).generateUniqueCode();
        verify(shortUrlRepository).save(any(ShortUrl.class));
    }

    @Test
    void shouldThrowExceptionWhenUnableToGenerateUniqueShortCode() {
        String originalUrl = "https://exemple.com";

        when(shortCodeService.generateUniqueCode())
                .thenThrow(new ShortCodeGenerationException(
                        "Unable to generate a unique short code"
                ));

        assertThrows(
                ShortCodeGenerationException.class,
                () -> shortUrlService.shorten(originalUrl)
        );

        verify(shortCodeService).generateUniqueCode();
        verify(shortUrlRepository, never()).save(any(ShortUrl.class));
    }

    @Test
    void shouldFindShortUrlByShortCode() {
        String originalUrl = "https://exemple.com";
        String shortCode = "abc1234";

        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode);

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(shortUrl));

        ShortenUrlResponse response = shortUrlService.findByShortCode(shortCode);

        assertEquals(originalUrl, response.originalUrl());
        assertEquals(shortCode, response.shortCode());

        verify(shortUrlRepository).findByShortCode(shortCode);
        verify(shortUrlRepository).incrementAccessCount(shortCode);
    }

    @Test
    void shouldThrowExceptionWhenShortCodeDoesNotExist() {
        String shortCode = "unknown";

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> shortUrlService.findByShortCode(shortCode)
        );

        verify(shortUrlRepository).findByShortCode(shortCode);
        verify(shortUrlRepository, never()).incrementAccessCount(shortCode);
    }

    @Test
    void shouldUpdateOriginalUrl() {
        String originalUrl = "https://old.com";
        String shortCode = "abc1234";
        String newUrl = "https://new.com";

        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode);

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(shortUrl));

        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShortenUrlResponse response = shortUrlService.update(shortCode, newUrl);

        assertEquals(newUrl, response.originalUrl());
        assertEquals(shortCode, response.shortCode());

        verify(shortUrlRepository).save(shortUrl);
    }

    @Test
    void shouldIncrementAccessCountForMultipleSuccessfulAccesses() {
        String originalUrl = "https://exemple.com";
        String shortCode = "abc1234";

        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode);

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(shortUrl));

        shortUrlService.findByShortCode(shortCode);
        shortUrlService.findByShortCode(shortCode);

        verify(shortUrlRepository, times(2)).incrementAccessCount(shortCode);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentShortCode() {
        String shortCode = "unknown";
        String newUrl = "https://new.com";

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> shortUrlService.update(shortCode, newUrl)
        );

        verify(shortUrlRepository).findByShortCode(shortCode);
        verify(shortUrlRepository, never()).save(any(ShortUrl.class));
    }

    @Test
    void shouldDeleteShortUrl() {
        String originalUrl = "https://exemple.com";
        String shortCode = "abc1234";

        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode);

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(shortUrl));

        shortUrlService.delete(shortCode);

        verify(shortUrlRepository).findByShortCode(shortCode);
        verify(shortUrlRepository).delete(shortUrl);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonexistentShortCode() {
        String shortCode = "unknown";

        when(shortUrlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> shortUrlService.delete(shortCode)
        );

        verify(shortUrlRepository).findByShortCode(shortCode);
        verify(shortUrlRepository, never()).delete(any(ShortUrl.class));
    }
}
