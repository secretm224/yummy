package com.cho_co_song_i.yummy.yummy.service;

import jakarta.servlet.http.HttpServletRequest;

public interface EventProducerService {
    /**
     * 로그인 시도하는 아이피 로그를 기록해준다 -> Kafka 로 Producing
     * @param req
     * @throws Exception
     */
    void produceLoginAttemptEvent(HttpServletRequest req) throws Exception;
    /**
     * 유저의 임시비밀번호를 이메일로 보내준다. -> 비밀번호 찾기
     * @param userId
     * @param userEmail
     * @param tempPw
     * @throws Exception
     */
    void produceUserTempPw(String userId, String userEmail, String tempPw) throws Exception;
    /**
     * 유저의 아이디 정보를 이메일로 보내준다 -> 아이디 찾기
     * @param userId
     * @param userEmail
     * @throws Exception
     */
    void produceUserIdInfo(String userId, String userEmail) throws Exception;

    /**
     * 회원가입시 이메일 검증코드 발송
     * @param userEmail
     * @param EmailCode
     * @throws Exception
     */
    void produceJoinEmailCode(String userEmail , String EmailCode) throws Exception;
}
