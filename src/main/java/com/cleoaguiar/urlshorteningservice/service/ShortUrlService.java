package com.cleoaguiar.urlshorteningservice.service;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import com.cleoaguiar.urlshorteningservice.dto.ShortenUrlResponse;
import com.cleoaguiar.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.cleoaguiar.urlshorteningservice.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;

@Service
public class ShortUrlService {
    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    public ShortUrlService(
            ShortUrlRepository shortUrlRepository,
            ShortCodeGenerator shortCodeGenerator
    ) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String shortCode = shortCodeGenerator.generate();

            if (!shortUrlRepository.existsByShortCode(shortCode)) {
                return shortCode;
            }
        }

        throw new IllegalStateException(
                "Unable to generate a unique short code after "
                        + MAX_GENERATION_ATTEMPTS
                        + " attemps"
        );
    }

    public ShortenUrlResponse shorten(String originalUrl) {
        String shortCode = generateUniqueShortCode();
        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode);
        ShortUrl savedShortUrl = shortUrlRepository.save(shortUrl);

        return new ShortenUrlResponse(
                savedShortUrl.getId(),
                savedShortUrl.getOriginalUrl(),
                savedShortUrl.getShortCode(),
                savedShortUrl.getCreatedAt()
        );
    }

    public ShortenUrlResponse findByShortCode(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(
                        "Short URL not found for code: " + shortCode
                ));

        return new ShortenUrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrl.getCreatedAt()
        );
    }

}
