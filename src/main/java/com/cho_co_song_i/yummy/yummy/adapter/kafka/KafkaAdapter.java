package com.cho_co_song_i.yummy.yummy.adapter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaAdapter {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 토픽에 메시지를 보내주는 함수
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
     * @throws Exception
     */
    public void sendMessageJson(String topicName, Object payload) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        kafkaTemplate.send(topicName, json); /* 강제로 JSON으로 보내기 */
    }
}
