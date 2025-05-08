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


    /**
     *
     * @param topicName
     * @param payload
     */
    public void sendMessage(String topicName, Object payload) {
        kafkaTemplate.send(topicName, payload);
    }

    /**
     * payload 정보를 json 형태로 Kafka Topic 에 Produce 해주는 함수
     * @param topicName
     * @param payload
     */
    public void sendMessageJson(String topicName, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topicName, json); /* 강제로 JSON으로 보내기 */
        } catch (JsonProcessingException e) {
            throw new RuntimeException("[Error][KafkaProducerService->sendMessageJson] Kafka JSON serialization failed: {}", e);
        }
    }
}
