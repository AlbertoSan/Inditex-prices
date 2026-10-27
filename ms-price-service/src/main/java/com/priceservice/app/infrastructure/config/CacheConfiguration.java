package com.priceservice.app.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's caching abstraction. The provider (Caffeine) and its policy
 * (TTL, maximum size) are configured in application.properties, so switching
 * to another provider (e.g. Redis) does not require code changes.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {
}
