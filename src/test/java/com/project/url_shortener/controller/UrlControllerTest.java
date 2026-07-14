package com.project.url_shortener.controller;

import com.project.url_shortener.model.ShortenRequest;
import com.project.url_shortener.model.ShortenResponse;
import com.project.url_shortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @MockitoBean
    private UrlService urlService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shortenUrl_WithValidRequest_ShouldReturn201() throws Exception {
        ShortenRequest request = ShortenRequest.builder()
                .url("https://example.com/test")
                .build();

        ShortenResponse response = ShortenResponse.builder()
                .shortCode("abc123")
                .shortUrl("http://localhost:8080/abc123")
                .build();

        when(urlService.createShortUrl("https://example.com/test", null))
                .thenReturn(response);

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc123"));
    }

    @Test
    void shortenUrl_WithCustomAlias_ShouldReturn201() throws Exception {
        ShortenRequest request = ShortenRequest.builder()
                .url("https://example.com/test")
                .customAlias("my-link")
                .build();

        ShortenResponse response = ShortenResponse.builder()
                .shortCode("my-link")
                .shortUrl("http://localhost:8080/my-link")
                .build();

        when(urlService.createShortUrl("https://example.com/test", "my-link"))
                .thenReturn(response);

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("my-link"));
    }

    @Test
    void shortenUrl_WithMissingUrl_ShouldReturn400() throws Exception {
        ShortenRequest request = ShortenRequest.builder()
                .build();

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortenUrl_WithEmptyUrl_ShouldReturn400() throws Exception {
        ShortenRequest request = ShortenRequest.builder()
                .url("")
                .build();

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortenUrl_WithTooLongUrl_ShouldReturn400() throws Exception {
        String longUrl = "https://example.com/" + "a".repeat(2048);
        ShortenRequest request = ShortenRequest.builder()
                .url(longUrl)
                .build();

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortenUrl_WithTooShortAlias_ShouldReturn400() throws Exception {
        ShortenRequest request = ShortenRequest.builder()
                .url("https://example.com/test")
                .customAlias("ab")
                .build();

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortenUrl_WithTooLongAlias_ShouldReturn400() throws Exception {
        String longAlias = "a".repeat(51);
        ShortenRequest request = ShortenRequest.builder()
                .url("https://example.com/test")
                .customAlias(longAlias)
                .build();

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirect_WithValidCode_ShouldReturn301() throws Exception {
        when(urlService.getOriginalUrl("abc123"))
                .thenReturn("https://example.com/test");

        mockMvc.perform(get("/abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://example.com/test"));
    }

    @Test
    void redirect_WithInvalidCode_ShouldReturn404() throws Exception {
        when(urlService.getOriginalUrl("nonexistent"))
                .thenThrow(new com.project.url_shortener.exception.ResourceNotFoundException("Short code 'nonexistent' not found"));

        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
