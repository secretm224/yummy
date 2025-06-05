package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthLoginDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.dto.userCache.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface YummyLoginService {
    /**
     * Oauth2 / Standard Login 공통 처리 함수
     * @param res
     * @param loginInfo
     * @param loginChannel
     */
    void processCommonLogin(HttpServletResponse res, StandardLoginBasicResDto loginInfo, OauthChannelStatus loginChannel);
    /**
     * 정석적인 방법으로 로그인하는 경우 -> 아이디/비밀번호 입력해서 로그인 시도
     * @param standardLoginDto
     * @param res
     * @param req
     * @return
     * @throws Exception
     */
    PublicStatus standardLoginUser(StandardLoginDto standardLoginDto, HttpServletResponse res, HttpServletRequest req) throws Exception;
    /**
     * 유저의 로그아웃을 위해서 로그인관련 인증 토큰을 다 제거해주는 함수
     * @param res
     */
    void standardLogoutUser(HttpServletResponse res);
    /**
     * Oauth2 로그인 처리 - 각 채널별로 처리해줌.
     * @param loginDto
     * @param res
     * @param req
     * @return
     * @throws Exception
     */
    PublicStatus processOauthLogin(OauthLoginDto loginDto, HttpServletResponse res, HttpServletRequest req) throws Exception;
    /**
     * 기본적인 유저 로그인 검증 후 유저로그인 관련 정보 반환하는 함수
     * @param standardLoginDto
     * @return
     */
    StandardLoginBasicResDto verifyAndGetLoginUserInfo(StandardLoginDto standardLoginDto) throws Exception;
    /**
     * 해당 브라우저가 로그인을 했는지 체크해준다. -> Optional.empty() 라면 다시 로그인 해줘야 한다는 의미.
     * @param res
     * @param req
     * @return
     * @throws Exception
     */
    ServiceResponse<Optional<UserBasicInfoDto>> verifyLoginUser(HttpServletResponse res, HttpServletRequest req) throws Exception;


    PublicStatus verifyOauthTokenValid(HttpServletResponse res, HttpServletRequest req);

    // 테스트 메소드
    void testing();
}
