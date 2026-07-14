package com.project.url_shortener.service;

import com.project.url_shortener.exception.DuplicateAliasException;
import com.project.url_shortener.exception.InvalidUrlException;
import com.project.url_shortener.exception.ResourceNotFoundException;
import com.project.url_shortener.model.UrlMapping;
import com.project.url_shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlService = new UrlService(repository, shortCodeGenerator);
    }

    @Test
    void createShortUrl_WithValidUrl_ShouldReturnShortCode() {
        String originalUrl = "https://example.com/test";
        String generatedCode = "abc123";

        when(shortCodeGenerator.generate()).thenReturn(generatedCode);
        when(repository.existsByCustomAlias(anyString())).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = urlService.createShortUrl(originalUrl, null);

        assertNotNull(response);
        assertEquals(generatedCode, response.getShortCode());
        assertTrue(response.getShortUrl().contains(generatedCode));

        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void createShortUrl_WithCustomAlias_ShouldUseAlias() {
        String originalUrl = "https://example.com/test";
        String customAlias = "my-link";

        when(repository.existsByCustomAlias(customAlias)).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = urlService.createShortUrl(originalUrl, customAlias);

        assertNotNull(response);
        assertEquals(customAlias, response.getShortCode());
        assertTrue(response.getShortUrl().contains(customAlias));

        verify(repository).save(any(UrlMapping.class));
        verify(shortCodeGenerator, never()).generate();
    }

    @Test
    void createShortUrl_WithDuplicateAlias_ShouldThrowException() {
        String originalUrl = "https://example.com/test";
        String customAlias = "existing-alias";

        when(repository.existsByCustomAlias(customAlias)).thenReturn(true);

        assertThrows(DuplicateAliasException.class, () -> {
            urlService.createShortUrl(originalUrl, customAlias);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void createShortUrl_WithInvalidUrl_ShouldThrowException() {
        String invalidUrl = "not-a-valid-url";

        assertThrows(InvalidUrlException.class, () -> {
            urlService.createShortUrl(invalidUrl, null);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void createShortUrl_WithEmptyUrl_ShouldThrowException() {
        assertThrows(InvalidUrlException.class, () -> {
            urlService.createShortUrl("", null);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void getOriginalUrl_WithValidCode_ShouldReturnUrl() {
        String shortCode = "abc123";
        String originalUrl = "https://example.com/test";
        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .clickCount(0L)
                .build();

        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(mapping));
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = urlService.getOriginalUrl(shortCode);

        assertEquals(originalUrl, result);
        verify(repository).save(argThat(saved -> saved.getClickCount() == 1L));
    }

    @Test
    void getOriginalUrl_WithInvalidCode_ShouldThrowException() {
        String shortCode = "nonexistent";

        when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            urlService.getOriginalUrl(shortCode);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void createShortUrl_WithHttpUrl_ShouldAccept() {
        String httpUrl = "http://example.com/test";

        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(repository.existsByCustomAlias(anyString())).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = urlService.createShortUrl(httpUrl, null);

        assertNotNull(response);
        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void createShortUrl_WithHttpsUrl_ShouldAccept() {
        String httpsUrl = "https://example.com/test";

        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(repository.existsByCustomAlias(anyString())).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = urlService.createShortUrl(httpsUrl, null);

        assertNotNull(response);
        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void createShortUrl_WithFtpUrl_ShouldReject() {
        String ftpUrl = "ftp://example.com/file";

        assertThrows(InvalidUrlException.class, () -> {
            urlService.createShortUrl(ftpUrl, null);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void createShortUrl_SameUrlMultipleTimes_ShouldCreateDifferentCodes() {
        String originalUrl = "https://example.com/test";

        when(shortCodeGenerator.generate())
                .thenReturn("abc123")
                .thenReturn("xyz789");
        when(repository.existsByCustomAlias(anyString())).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response1 = urlService.createShortUrl(originalUrl, null);
        var response2 = urlService.createShortUrl(originalUrl, null);

        assertNotEquals(response1.getShortCode(), response2.getShortCode());
        verify(repository, times(2)).save(any(UrlMapping.class));
    }
}