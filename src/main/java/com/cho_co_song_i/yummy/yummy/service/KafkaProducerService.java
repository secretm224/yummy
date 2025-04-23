package com.cho_co_song_i.yummy.yummy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper; /* 여기 자동 주입 */

    @Value("${spring.topic.kafka.find-user-info}")
    private String topicName;

    public void sendMessage(Object payload) {
        kafkaTemplate.send(topicName, payload);
    }

    public void sendMessageJson(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topicName, json); /* 강제로 JSON으로 보내기 */
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kafka JSON 직렬화 실패", e);
        }
    }
}
