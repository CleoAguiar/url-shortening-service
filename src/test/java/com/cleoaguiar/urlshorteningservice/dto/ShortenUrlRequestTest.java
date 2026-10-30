package com.cleoaguiar.urlshorteningservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortenUrlRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @Test
    void shouldAcceptValidUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com");
        Set<ConstraintViolation<ShortenUrlRequest>> violation = validator.validate(request);
        assertTrue(violation.isEmpty());
    }

    @Test
    void shouldRejectNullUrl() {
     ShortenUrlRequest request = new ShortenUrlRequest(null);

     Set<ConstraintViolation<ShortenUrlRequest>> violation = validator.validate(request);
     assertFalse(violation.isEmpty());
    }

    @Test
    void shouldRejectBlankUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest(" ");
        Set<ConstraintViolation<ShortenUrlRequest>> violation = validator.validate(request);
        assertFalse(violation.isEmpty());
    }

    @Test
    void shouldRejectInvalidUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest("not-a-url");
        Set<ConstraintViolation<ShortenUrlRequest>> violation = validator.validate(request);
        assertFalse(violation.isEmpty());
    }
}
