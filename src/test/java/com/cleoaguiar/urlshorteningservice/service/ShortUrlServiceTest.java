package com.cleoaguiar.urlshorteningservice.service;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import com.cleoaguiar.urlshorteningservice.dto.ShortenUrlResponse;
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
    private ShortCodeGenerator shortCodeGenerator;

    @InjectMocks
    private ShortUrlService shortUrlService;

    @Test
    void shouldShortenUrlSuccessfully() {
        String originalUrl = "https://exemple.com";
        String shortCode = "abc1234";

        when(shortCodeGenerator.generate())
                .thenReturn(shortCode);

        when(shortUrlRepository.existsByShortCode(shortCode))
                .thenReturn(false);

        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShortenUrlResponse response = shortUrlService.shorten(originalUrl);

        assertEquals(originalUrl, response.originalUrl());
        assertEquals(shortCode, response.shortCode());
    }

    @Test
    void shouldGenerateNewShortCodeWhenCollisionOccurs() {
        String originalUrl = "https://exemple.com";
        String duplicateCode = "abc1234";
        String uniqueCode = "xyz5678";

        when(shortCodeGenerator.generate())
                .thenReturn(duplicateCode, uniqueCode);

        when(shortUrlRepository.existsByShortCode(duplicateCode))
                .thenReturn(true);

        when(shortUrlRepository.existsByShortCode(uniqueCode))
                .thenReturn(false);

        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShortenUrlResponse response = shortUrlService.shorten(originalUrl);

        assertEquals(originalUrl, response.originalUrl());
        assertEquals(uniqueCode, response.shortCode());

        verify(shortCodeGenerator, times(2)).generate();
    }

    @Test
    void shouldThrowExceptionWhenUnableToGenerateUniqueShortCode() {
        String originalUrl = "https://exemple.com";
        String duplicateCode = "abc1234";

        when(shortCodeGenerator.generate())
                .thenReturn(duplicateCode);

        when(shortUrlRepository.existsByShortCode(duplicateCode))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class, () -> shortUrlService.shorten(originalUrl)
        );

        verify(shortCodeGenerator, times(5)).generate();
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
    }
}
