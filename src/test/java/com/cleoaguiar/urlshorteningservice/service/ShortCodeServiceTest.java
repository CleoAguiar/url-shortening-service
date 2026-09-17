package com.cleoaguiar.urlshorteningservice.service;

import com.cleoaguiar.urlshorteningservice.repository.ShortUrlRepository;
import com.cleoaguiar.urlshorteningservice.exception.ShortCodeGenerationException;
import com.cleoaguiar.urlshorteningservice.service.ShortCodeGenerator;
import com.cleoaguiar.urlshorteningservice.service.ShortCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortCodeServiceTest {
    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @InjectMocks
    private ShortCodeService shortCodeService;

    @Test
    void shouldGenerateAnotherCodeWhenCollisionOccurs() {
        when(shortCodeGenerator.generate()).thenReturn("ABC1234", "XYZ5678");
        when(shortUrlRepository.existsByShortCode("ABC1234")).thenReturn(true);
        when(shortUrlRepository.existsByShortCode("XYZ5678")).thenReturn(false);

        String result = shortCodeService.generateUniqueCode();
        assertEquals("XYZ5678", result);
        verify(shortCodeGenerator, times(2)).generate();
    }

    @Test
    void shouldThrowExceptionWhenMaximumAttemptsAreReached() {
        when(shortCodeGenerator.generate()).thenReturn("ABC1234");
        when(shortUrlRepository.existsByShortCode("ABC1234")).thenReturn(true);
        assertThrows(ShortCodeGenerationException.class, ()-> shortCodeService.generateUniqueCode());
        verify(shortCodeGenerator, times(5)).generate();
    }
}