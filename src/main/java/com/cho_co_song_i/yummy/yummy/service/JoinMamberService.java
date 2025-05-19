package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface JoinMamberService {
    /**
     * 유저의 비밀번호를 바꿔주는 함수
     * @param changePwDto
     * @return
     * @throws Exception
     */
    PublicStatus changePasswd(HttpServletResponse res, HttpServletRequest req, ChangePwDto changePwDto) throws Exception;
    /**
     * 회원의 비밀번호를 찾아주는 함수
     * @param findPwDto
     * @return
     * @throws Exception
     */
    PublicStatus recoverUserPw(FindPwDto findPwDto) throws Exception;
    /**
     * 회원의 아이디를 찾아주는 함수
     * @param findIdDto
     * @return
     * @throws Exception
     */
    PublicStatus recoverUserId(FindIdDto findIdDto) throws Exception;
    /**
     * 회원가입 해주는 서비스 함수
     * @param joinMemberDto
     * @return
     * @throws Exception
     */
    PublicStatus joinMember(HttpServletResponse res, HttpServletRequest req, JoinMemberDto joinMemberDto) throws Exception;
    /**
     * 사용자가 Oauth 로 로그인 했을때 해당 Oauth 정보를 기존의 회원정보와 연동시키는 함수.
     * @param standardLoginDto
     * @param res
     * @param req
     * @return
     * @throws Exception
     */
    PublicStatus linkMemberByOauth(StandardLoginDto standardLoginDto, HttpServletResponse res, HttpServletRequest req) throws Exception;
    /**
     * 회원가입이메일검증코드발송
     * @param userEmail
     * @return
     * @throws Exception
     */
    PublicStatus generateVerificationCode(String userEmail) throws Exception;
    /**
     * 회원가입 인증코드 검증
     * @param userEmail
     * @param code
     * @return
     */
    PublicStatus checkVerificationCode(String userEmail,int code);
}