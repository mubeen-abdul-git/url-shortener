package com.project.url_shortener.util;

import org.springframework.web.util.InvalidUrlException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class UrlValidator {

    private static final Set<String> RESERVED_WORDS = Set.of(
            "api", "health", "admin", "stats", "metrics", "actuator"
    );

    public static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidUrlException("URL cannot be empty");
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                throw new InvalidUrlException("URL must use http or https scheme");
            }

            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw new InvalidUrlException("URL must have a valid host");
            }

        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL format: " + e.getMessage());
        }
    }

    public static void validateCustomAlias(String alias) {
        if (alias == null) {
            return;
        }

        String normalizedAlias = alias.toLowerCase();

        if (normalizedAlias.length() < 3 || normalizedAlias.length() > 50) {
            throw new InvalidUrlException("Custom alias must be 3-50 characters");
        }

        if (!normalizedAlias.matches("^[a-z0-9-]+$")) {
            throw new InvalidUrlException("Custom alias can only contain letters, numbers, and hyphens");
        }

        if (normalizedAlias.startsWith("-") || normalizedAlias.endsWith("-")) {
            throw new InvalidUrlException("Custom alias cannot start or end with a hyphen");
        }

        if (RESERVED_WORDS.contains(normalizedAlias)) {
            throw new InvalidUrlException("Custom alias is reserved");
        }
    }
}
