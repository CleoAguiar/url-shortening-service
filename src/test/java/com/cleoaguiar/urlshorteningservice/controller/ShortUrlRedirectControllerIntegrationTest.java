package com.cleoaguiar.urlshorteningservice.controller;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import com.cleoaguiar.urlshorteningservice.repository.ShortUrlRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ShortUrlRedirectControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    private final Set<String> createdShortCodes = new HashSet<>();

    @AfterEach
    void tearDown() {
        for (String shortCode : createdShortCodes) {
            shortUrlRepository.findByShortCode(shortCode)
                    .ifPresent(shortUrlRepository::delete);
        }

        createdShortCodes.clear();
    }

    @Test
    void shouldRedirectToOriginalUrlWhenShortCodeExists() throws Exception {
        String originalUrl = "https://example.com/integration-test/" + UUID.randomUUID();

        String shortCode = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);

        ShortUrl shortUrl = shortUrlRepository.saveAndFlush(
                new ShortUrl(originalUrl, shortCode)
        );

        createdShortCodes.add(shortUrl.getShortCode());

        mockMvc.perform(get("/{shortCode}", shortUrl.getShortCode()))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        originalUrl
                ))
                .andExpect(content().string(""));

    }

}
