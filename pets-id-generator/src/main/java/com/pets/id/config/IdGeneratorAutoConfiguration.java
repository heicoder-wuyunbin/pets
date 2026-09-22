package com.pets.id.config;

import com.pets.id.properties.IdGeneratorProperties;
import com.pets.id.repo.SegmentRepository;
import com.pets.id.service.IdService;
import com.pets.id.service.RedisSegmentIdService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 分布式ID发号器自动配置。
 * 注册为 Spring Boot 自动配置，确保引用方无需额外 @ComponentScan。
 */
@AutoConfiguration
@AutoConfigureAfter(name = "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration")
@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties(IdGeneratorProperties.class)
public class IdGeneratorAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public IdService idService(RedisTemplate<Object, Object> redisTemplate,
                              SegmentRepository segmentRepository,
                              IdGeneratorProperties properties) {
        return new RedisSegmentIdService(redisTemplate, segmentRepository, properties);
    }
}
