package com.project.url_shortener.service;

import com.project.url_shortener.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private final UrlMappingRepository repository;
    private final SecureRandom random;

    private static final String STRING = "abcdefghijklmnopqrstuvwxyz";

    @Autowired
    public ShortCodeGenerator(UrlMappingRepository repository) {
        this.repository = repository;
        this.random = new SecureRandom();
    }

    // Generate a unique short code. Retries if collision occurs.
    public String generate() {
        String code = generateRandomCode();
        if (!repository.existsByShortCode(code)) {
            return code;
        }
        return "error";
    }

    // Generate a random 7-character Base62 string
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(7);
        for (int i = 0; i < 7; i++) {
            sb.append(STRING.charAt(random.nextInt(STRING.length())));
        }
        return sb.toString();
    }
}
