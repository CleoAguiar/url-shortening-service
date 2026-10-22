package com.cleoaguiar.urlshorteningservice.service;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import com.cleoaguiar.urlshorteningservice.dto.ShortenUrlResponse;
import com.cleoaguiar.urlshorteningservice.dto.UrlStatisticsResponse;
import com.cleoaguiar.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.cleoaguiar.urlshorteningservice.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;

@Service
public class ShortUrlService {
    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeService shortCodeService;

    public ShortUrlService(
            ShortUrlRepository shortUrlRepository,
            ShortCodeService shortCodeService
    ) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeService = shortCodeService;
    }

    public ShortenUrlResponse shorten(String originalUrl) {
        String shortCode = shortCodeService.generateUniqueCode();
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

        shortUrlRepository.incrementAccessCount(shortCode);

        return new ShortenUrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrl.getCreatedAt()
        );
    }

    public ShortenUrlResponse update(String shortCode, String newOriginalUrl) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(
                        "Short URL not found for code: " + shortCode
                ));

        shortUrl.updateOriginalUrl(newOriginalUrl);

        ShortUrl updatedShortUrl = shortUrlRepository.save(shortUrl);

        return new ShortenUrlResponse(
                updatedShortUrl.getId(),
                updatedShortUrl.getOriginalUrl(),
                updatedShortUrl.getShortCode(),
                updatedShortUrl.getCreatedAt()
        );
    }

    public void delete(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(
                        "Short URL not found for code: " + shortCode
                ));

        shortUrlRepository.delete(shortUrl);
    }

    public UrlStatisticsResponse getStatistics(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(
                        "Short URL not found: " + shortCode
                ));

        return new UrlStatisticsResponse(
                shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt(),
                shortUrl.getAccessCount()
        );
    }
}
