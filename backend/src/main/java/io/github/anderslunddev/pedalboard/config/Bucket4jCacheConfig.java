package io.github.anderslunddev.pedalboard.config;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

/**
 * JCache (javax.cache) CacheManager for bucket4j-spring-boot-starter using Caffeine.
 * The starter uses this to store rate-limit buckets per client key.
 */
@Configuration
@ConditionalOnProperty(name = "bucket4j.enabled", havingValue = "true")
public class Bucket4jCacheConfig {

	public static final String AUTH_RATE_LIMIT_CACHE = "auth-rate-limit";

	@Bean
	CacheManager bucket4jCacheManager() {
		CachingProvider provider = Caching
				.getCachingProvider("com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider");
		CacheManager cacheManager = provider.getCacheManager();
		CaffeineConfiguration<Object, Object> config = new CaffeineConfiguration<>();
		config.setExpireAfterAccess(OptionalLong.of(TimeUnit.HOURS.toNanos(1)));
		config.setMaximumSize(OptionalLong.of(100_000));
		cacheManager.createCache(AUTH_RATE_LIMIT_CACHE, config);
		return cacheManager;
	}
}
