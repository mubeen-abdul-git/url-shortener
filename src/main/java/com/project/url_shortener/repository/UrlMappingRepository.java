package com.project.url_shortener.repository;

import com.project.url_shortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);
}