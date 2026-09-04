package com.cleoaguiar.urlshorteningservice.domain.repository;

import com.cleoaguiar.urlshorteningservice.domain.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

}
