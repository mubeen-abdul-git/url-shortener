package com.project.url_shortener.util;

import org.springframework.web.util.InvalidUrlException;

public class UrlValidator {

    public static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidUrlException("URL cannot be empty");
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
    }
}
