package com.cho_co_song_i.yummy.yummy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean deleteKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
    public <T> T getValue(String key, TypeReference<T> typeReference) {
        Object redisData = redisTemplate.opsForValue().get(key);

        if (redisData == null) {
            return null;
        }

        try {
            String json = redisData.toString();
            return objectMapper.readValue(json, typeReference);
        } catch(Exception e) {
            throw new RuntimeException("[Error][RedisService->getValue]Failed to parse Redis data", e);
        }

    }
}
