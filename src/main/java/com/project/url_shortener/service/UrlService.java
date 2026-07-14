package com.project.url_shortener.service;

import com.project.url_shortener.exception.DuplicateAliasException;
import com.project.url_shortener.exception.ResourceNotFoundException;
import com.project.url_shortener.model.ShortenResponse;
import com.project.url_shortener.model.UrlMapping;
import com.project.url_shortener.repository.UrlMappingRepository;
import com.project.url_shortener.util.UrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlMappingRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.host:localhost}")
    private String serverHost;

    @Transactional
    public ShortenResponse createShortUrl(String originalUrl, String customAlias) {
        // Validate the URL first
        UrlValidator.validateUrl(originalUrl);

        // If custom alias is provided, validate and check if it's already taken
        if (customAlias != null) {
            UrlValidator.validateCustomAlias(customAlias);
            customAlias = customAlias.toLowerCase();

            if (repository.existsByCustomAlias(customAlias)) {
                throw new DuplicateAliasException("Custom alias '" + customAlias + "' already exists");
            }
        }

        // Use custom alias if provided, otherwise generate a random code
        String shortCode = customAlias != null ? customAlias : shortCodeGenerator.generate();

        // Save the mapping
        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .customAlias(customAlias)
                .clickCount(0L)
                .build();

        repository.save(mapping);

        // Build the full short URL
        String shortUrl = String.format("http://%s:%d/%s", serverHost, serverPort, shortCode);

        return ShortenResponse.builder()
                .shortCode(shortCode)
                .shortUrl(shortUrl)
                .build();
    }

    // Looks up the original URL and increments the click counter
    @Transactional
    public String getOriginalUrl(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short code '" + shortCode + "' not found"));

        // Update analytics
        mapping.setClickCount(mapping.getClickCount() + 1);
        mapping.setLastAccessedAt(LocalDateTime.now());
        repository.save(mapping);

        return mapping.getOriginalUrl();
    }
}
