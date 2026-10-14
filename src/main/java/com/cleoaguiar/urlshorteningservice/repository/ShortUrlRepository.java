package com.cleoaguiar.urlshorteningservice.repository;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    boolean existsByShortCode(String shortCode);

    Optional<ShortUrl> findByShortCode(String shortCode);

    @Modifying
    @Transactional
    @Query("""
            UPDATE ShortUrl s
            SET s.accessCount = s.accessCount + 1
            WHERE s.shortCode = :shortCode
            """)
    int incrementAccessCount(@Param("shortCode") String shortCode);
}
