package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration;

import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.caching.SizeLimitedMapCache;

import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService.CACHE_NAME;

/**
 * @author juliwolf
 */

@Configuration
@EnableCaching
public class CachingConfiguration {

  private final int MAX_SIZE = 10_000;
  @Bean
  public CacheManager cacheManager() {
    final SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(List.of(new SizeLimitedMapCache(CACHE_NAME, false, MAX_SIZE)));
    return cacheManager;
  }
}
