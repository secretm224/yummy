package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.SendIdFormDto;
import com.cho_co_song_i.yummy.yummy.dto.SendPwFormDto;
import com.cho_co_song_i.yummy.yummy.dto.TryLoginHistDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EventProducerService {

    /* 로그인 히스토리 관련 Kafka Topic */
    @Value("${spring.topic.kafka.login-history}")
    private String loginKafkaTopic;

    /* 회원의 아이디 찾기 관련 Kafka Topic */
    @Value("${spring.topic.kafka.find-user-id-info}")
    private String findIdTopic;

    /* 회원의 비밀번호 찾기 관련 Kafka Topic */
    @Value("${spring.topic.kafka.find-user-pw-info}")
    private String findPwTopic;

    private final KafkaProducerService kafkaProducerService;

    public EventProducerService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * 로그인 시도하는 아이피 로그를 기록해준다 -> Kafka 로 Producing
     * @param req
     */
    public void produceLoginAttemptEvent(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty()) {
            ip = req.getRemoteAddr();
        }

        TryLoginHistDto tryLoginHistDto = new TryLoginHistDto(LocalDateTime.now(), ip);

        kafkaProducerService.sendMessageJson(loginKafkaTopic, tryLoginHistDto);
    }

    /**
     * 유저의 임시비밀번호를 이메일로 보내준다. -> 비밀번호 찾기
     * @param userId
     * @param userEmail
     * @param tempPw
     */
    public void produceUserTempPw(String userId, String userEmail, String tempPw) {
        kafkaProducerService.sendMessageJson(
                findPwTopic,
                new SendPwFormDto(LocalDateTime.now(), userId, userEmail, tempPw)
        );
    }

    /**
     * 유저의 아이디 정보를 이메일로 보내준다 -> 아이디 찾기
     * @param userId
     * @param userEmail
     */
    public void produceUserIdInfo(String userId, String userEmail) {
        kafkaProducerService.sendMessageJson(
                findIdTopic,
                new SendIdFormDto(LocalDateTime.now(), userId, userEmail));
    }



}
