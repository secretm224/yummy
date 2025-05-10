package com.cho_co_song_i.yummy.yummy.adapter.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisAdapter {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisAdapter(RedisTemplate<String, String> redisTemplate) {
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

    /**
     * Redis 에서 특정 키에 대한 Value 를 사용자가 원하는 인스턴스로 변환해주는 함수.
     * @param key
     * @param typeReference
     * @return
     * @param <T>
     */
    public <T> T getValue(String key, TypeReference<T> typeReference) {
        Object redisData = redisTemplate.opsForValue().get(key);

        if (redisData == null) {
            return null;
        }

        try {
            String json = redisData.toString();
            return objectMapper.readValue(json, typeReference);
        } catch(Exception e) {
            throw new RuntimeException("[Error][RedisService->getValue] Failed to parse Redis data", e);
        }
    }

    /**
     * Redis 에서 특정 키에 특정 DTO 를 JSON으로 직렬화 한다음 저장해주는 함수
     * @param key
     * @param value
     * @return
     * @param <T>
     */
    public <T> Boolean set(String key, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("[Error][RedisService->set] {} ", e);
        }
    }

    /**
     * Redis 에서 특정 키에 특정 DTO 를 JSON으로 직렬화 한다음 저장해주는 함수 (ttl 추가)
     * @param key
     * @param value
     * @param ttl
     * @return
     * @param <T>
     */
    public <T> Boolean set(String key, T value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("[Error][RedisService->set] Failed to set Redis value", e);
        }
    }
}
