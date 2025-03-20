package com.cho_co_song_i.yummy.yummy.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ObjectMapperConfig {

    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // JavaTimeModule 등록
        objectMapper.registerModule(new JavaTimeModule());

        // ISO-8601 형식으로 날짜를 직렬화하도록 설정 (예: 2025-03-17T08:29:07Z)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }
}