package kr.hhplus.be.global.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import kr.hhplus.be.global.cache.CacheType
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer

@Configuration
@EnableCaching
class RedisConfig {

    @Bean
    fun redisCacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        val objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
            .activateDefaultTyping(
                LaissezFaireSubTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )

        val valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)

        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
            .disableCachingNullValues()

        val cacheConfigMap: Map<String, RedisCacheConfiguration> = CacheType.entries.associateBy(
            keySelector = { it.cacheName },
            valueTransform = { defaultConfig.entryTtl(it.ttl) }
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .initialCacheNames(CacheType.entries.map { it.cacheName }.toSet())
            .withInitialCacheConfigurations(cacheConfigMap)
            .build()
    }
}
