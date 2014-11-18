package de.gpue.gotissues.config;

import java.util.Arrays;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
	@Bean
	public CacheManager chacheManager(){
		SimpleCacheManager c = new SimpleCacheManager();
		ConcurrentMapCache cb = new ConcurrentMapCache("default");
		c.setCaches(Arrays.asList(new Cache[]{cb}));
		return c;
	}
}
