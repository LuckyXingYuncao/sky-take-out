package com.sky.config;

import com.sky.json.JacksonObjectMapper;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        JacksonObjectMapper objectMapper = new JacksonObjectMapper();
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        return builder -> {
            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(1))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(serializer));

            builder.cacheDefaults(defaultConfig)
                    .withCacheConfiguration("workspace:businessData",
                            defaultConfig.entryTtl(Duration.ofMinutes(5)))
                    .withCacheConfiguration("workspace:dishOverview",
                            defaultConfig.entryTtl(Duration.ofMinutes(5)))
                    .withCacheConfiguration("workspace:setmealOverview",
                            defaultConfig.entryTtl(Duration.ofMinutes(5)))
                    .withCacheConfiguration("workspace:orderOverview",
                            defaultConfig.entryTtl(Duration.ofMinutes(5)));
        };
    }
}