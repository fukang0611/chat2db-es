package com.example.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 缓存配置
 */
@Configuration
@EnableCaching
@EnableAsync
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            "textEmbeddings",
            "vectorSearchResults", 
            "hybridSearchResults",
            "aiGeneratedDSL",
            "searchResults"
        );
        
        return cacheManager;
    }
}