package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.kafka.KafkaAdapter;
import com.cho_co_song_i.yummy.yummy.dto.JoinEmailCodeDto;
import com.cho_co_song_i.yummy.yummy.dto.SendIdFormDto;
import com.cho_co_song_i.yummy.yummy.dto.SendPwFormDto;
import com.cho_co_song_i.yummy.yummy.dto.TryLoginHistDto;
import com.cho_co_song_i.yummy.yummy.service.EventProducerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class EventProducerServiceImpl implements EventProducerService {

    /* 로그인 히스토리 관련 Kafka Topic */
    @Value("${spring.topic.kafka.login-history}")
    private String loginKafkaTopic;

    /* 회원의 아이디 찾기 관련 Kafka Topic */
    @Value("${spring.topic.kafka.find-user-id-info}")
    private String findIdTopic;

    /* 회원의 비밀번호 찾기 관련 Kafka Topic */
    @Value("${spring.topic.kafka.find-user-pw-info}")
    private String findPwTopic;

    private final KafkaAdapter kafkaAdapter;

    public void produceLoginAttemptEvent(HttpServletRequest req) throws Exception {
        String ip = req.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty()) {
            ip = req.getRemoteAddr();
        }

        TryLoginHistDto tryLoginHistDto = new TryLoginHistDto(LocalDateTime.now(), ip);

        kafkaAdapter.sendMessageJson(loginKafkaTopic, tryLoginHistDto);
    }

    public void produceUserTempPw(String userId, String userEmail, String tempPw) throws Exception {
        kafkaAdapter.sendMessageJson(
                findPwTopic,
                new SendPwFormDto(LocalDateTime.now(), userId, userEmail, tempPw)
        );
    }

    public void produceUserIdInfo(String userId, String userEmail) throws Exception {
        kafkaAdapter.sendMessageJson(
                findIdTopic,
                new SendIdFormDto(LocalDateTime.now(), userId, userEmail));
    }

    //dev-yummy-join-hist
    //yummy-join-hist
    public void produceJoinEmailCode(String userEmail , String EmailCode) throws Exception{
        kafkaAdapter.sendMessageJson("dev-yummy-join-hist",
                                     new JoinEmailCodeDto(
                                             LocalDateTime.now(),
                                             userEmail,
                                             EmailCode
                                     ));
    }

}
