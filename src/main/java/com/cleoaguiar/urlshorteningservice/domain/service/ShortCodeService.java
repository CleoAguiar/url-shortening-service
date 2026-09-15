package com.cleoaguiar.urlshorteningservice.domain.service;

import com.cleoaguiar.urlshorteningservice.domain.repository.ShortUrlRepository;
import com.cleoaguiar.urlshorteningservice.exception.ShortCodeGenerationException;
import org.springframework.stereotype.Service;

@Service
public class ShortCodeService {
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final ShortCodeGenerator shortCodeGenerator;
    private final ShortUrlRepository shortUrlRepository;

    public ShortCodeService(ShortCodeGenerator shortCodeGenerator, ShortUrlRepository shortUrlRepository) {
        this.shortCodeGenerator = shortCodeGenerator;
        this.shortUrlRepository = shortUrlRepository;
    }

    public String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = shortCodeGenerator.generate();

            if(!shortUrlRepository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new ShortCodeGenerationException("Unable to generate a unique short code");
    }
}
