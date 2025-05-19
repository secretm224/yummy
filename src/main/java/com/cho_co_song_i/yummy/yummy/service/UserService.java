package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.dto.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    /**
     * Redis 에 저장할 유저의 기본 정보
     * @param user
     * @param oauthChannelStatus
     * @return
     * @throws Exception
     */
    UserBasicInfoDto getUserBasicInfos(UserTbl user, OauthChannelStatus oauthChannelStatus) throws Exception;

//    /**
//     * 프로필 사진 테이블을 update 또는 insert 해준다.
//     * @param userNo
//     * @param userOAuthInfoDto
//     * @param loginChannel
//     * @return
//     * @throws Exception
//     */
//    void modifyUserPic(Long userNo, UserOAuthInfoDto userOAuthInfoDto, OauthChannelStatus loginChannel) throws Exception;
    
    /**
     * Jwt 의 토큰을 검증하고 그 내부의 내용을 반환해주는 함수.
     * - 만료된 토큰이거나, 위조된 토큰인 경우 삭제도 병행함.
     * @param jwtName
     * @param res
     * @param req
     * @return
     */
    JwtValidationResult validateJwtAndCleanIfInvalid(String jwtName, HttpServletResponse res, HttpServletRequest req);

    /**
     * JWT 의 subject 를 리턴해주는 함수
     * @param jwtValidationResult
     * @return
     */
    String getSubjectFromJwt(JwtValidationResult jwtValidationResult);

    /**
     * Jwt 의 claims 를 반환시켜주는 함수.
     * @param jwtValidationResult
     * @param claimName
     * @param clazz
     * @return
     * @param <T>
     */
    <T> T getClaimFromJwt(JwtValidationResult jwtValidationResult, String claimName, Class<T> clazz);
}
