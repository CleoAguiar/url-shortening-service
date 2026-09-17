package com.cleoaguiar.urlshorteningservice.service;

import com.cleoaguiar.urlshorteningservice.service.RandomShortCodeGenerator;
import com.cleoaguiar.urlshorteningservice.service.ShortCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomShortCodeGeneratorTest {
    private final ShortCodeGenerator generator = new RandomShortCodeGenerator();

    @Test
    void shouldGenerateCodeWithExpectedLength() {
        ShortCodeGenerator generator = new RandomShortCodeGenerator();
        String code = generator.generate();
        assertEquals(7, code.length());
    }

    @Test
    void shouldGenerateUrlSafeCharacters() {
        ShortCodeGenerator generator = new RandomShortCodeGenerator();
        String code = generator.generate();
        assertTrue(code.matches("[a-zA-Z0-9]{7}"));
    }

    @Test
    void shouldGenerateDifferentCodes() {
        ShortCodeGenerator generator = new RandomShortCodeGenerator();
        String firstCode = generator.generate();
        String secondCode = generator.generate();
        assertNotEquals(firstCode, secondCode);
    }

}
