package com.cho_co_song_i.yummy.yummy.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.password}")
    private String redisPassWord;

    @Value("${spring.redis.cluster.nodes}")
    private String uris;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        String[] nodes = uris.split(",");

        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();

        for (String node : nodes) {
            String[] parts = node.split(":");
            if (parts.length == 2) {
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                clusterConfiguration.clusterNode(host, port);
            }
        }

        if (redisPassWord != null && !redisPassWord.isEmpty()) {
            clusterConfiguration.setPassword(redisPassWord);
        }

        return new LettuceConnectionFactory(clusterConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        /* key & value 직렬화 설정 (JSON 직렬화) */
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
