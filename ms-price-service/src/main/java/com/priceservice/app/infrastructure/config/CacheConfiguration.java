package com.priceservice.app.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's caching abstraction. The provider (Caffeine) and its policy
 * (TTL, tamaño máximo) se configuran en application.properties, de modo que
 * cambiar a otro proveedor (p. ej. Redis) no requiere tocar código.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {
}
