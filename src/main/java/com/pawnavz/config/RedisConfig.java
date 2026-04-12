package com.pawnavz.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Falls back to in-memory caching when Redis is not configured.
     * To enable Redis, set spring.cache.type=redis in application.yaml
     * and add spring-boot-starter-data-redis dependency.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple", matchIfMissing = true)
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "products", "userProfile", "categories"
        );
    }
}
