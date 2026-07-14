package com.project.url_shortener.service;

import com.project.url_shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ShortCodeGeneratorTest {

    @Mock
    private UrlMappingRepository repository;

    @InjectMocks
    private ShortCodeGenerator shortCodeGenerator;

    @Test
    void generate_WhenNoCollision_ShouldReturnCode() {
        when(repository.existsByShortCode(anyString())).thenReturn(false);

        String code = shortCodeGenerator.generate();

        assertNotNull(code);
        assertEquals(7, code.length());
        verify(repository).existsByShortCode(anyString());
    }

    @Test
    void generate_WhenCollisionOccurs_ShouldRetry() {
        when(repository.existsByShortCode(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        String code = shortCodeGenerator.generate();

        assertNotNull(code);
        assertEquals(7, code.length());
        verify(repository, times(2)).existsByShortCode(anyString());
    }

    @Test
    void generate_WhenMultipleCollisions_ShouldRetryUntilSuccess() {
        when(repository.existsByShortCode(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        String code = shortCodeGenerator.generate();

        assertNotNull(code);
        assertEquals(7, code.length());
        verify(repository, times(4)).existsByShortCode(anyString());
    }

    @Test
    void generate_WhenMaxRetriesExceeded_ShouldThrowException() {
        when(repository.existsByShortCode(anyString())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            shortCodeGenerator.generate();
        });

        verify(repository, times(5)).existsByShortCode(anyString());
    }

    @Test
    void generate_ShouldProduceUrlSafeCharacters() {
        when(repository.existsByShortCode(anyString())).thenReturn(false);

        String code = shortCodeGenerator.generate();

        assertTrue(code.matches("^[0-9A-Za-z]+$"));
    }

    @Test
    void generate_ShouldProduceDifferentCodes() {
        when(repository.existsByShortCode(anyString())).thenReturn(false);

        String code1 = shortCodeGenerator.generate();
        String code2 = shortCodeGenerator.generate();

        assertNotEquals(code1, code2);
    }
}