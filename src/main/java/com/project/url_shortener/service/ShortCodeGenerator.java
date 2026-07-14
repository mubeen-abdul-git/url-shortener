package com.project.url_shortener.service;

import com.project.url_shortener.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    // Base62 charset: 0-9, A-Z, a-z
    private static final String BASE62_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_RETRIES = 5;

    private final UrlMappingRepository repository;
    private final SecureRandom random;

    @Autowired
    public ShortCodeGenerator(UrlMappingRepository repository) {
        this.repository = repository;
        this.random = new SecureRandom();
    }

    // Generate a unique short code. Retries if collision occurs.
    public String generate() {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String code = generateRandomCode();
            if (!repository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Failed to generate unique short code after " + MAX_RETRIES + " attempts");
    }

    // Generate a random 7-character Base62 string
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(BASE62_CHARSET.charAt(random.nextInt(BASE62_CHARSET.length())));
        }
        return sb.toString();
    }
}
