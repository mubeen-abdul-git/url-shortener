package com.project.url_shortener.service;

import com.project.url_shortener.model.ShortenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

public class UrlService {

//    private final UrlMappingRepository repository;
//    private final ShortCodeGenerator shortCodeGenerator;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.host:localhost}")
    private String serverHost;

    @Transactional
    public ShortenResponse createShortUrl(String originalUrl, String customAlias) {
        // Validate the URL first

        // If custom alias is provided, validate and check if it's already taken

        // Use custom alias if provided, otherwise generate a random code

        // Save the mapping

        // Build the full short URL

        return ShortenResponse.builder()
                .shortCode("shortCode")
                .shortUrl("shortUrl")
                .build();
    }
}