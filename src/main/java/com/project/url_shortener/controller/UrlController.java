package com.project.url_shortener.controller;

import com.project.url_shortener.model.ShortenRequest;
import com.project.url_shortener.model.ShortenResponse;
import com.project.url_shortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = urlService.createShortUrl(request.getUrl(), request.getCustomAlias());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
