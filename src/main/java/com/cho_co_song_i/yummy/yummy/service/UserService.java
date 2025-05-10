package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.dto.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    /**
     * 유저의 기본 회원 정보. -> 뭔가 추가할게 있다면 여기랑 아래의 convertUserToBasicInfo 함수를 추가해주면 된다.
     * @param user
     * @return
     */
    UserBasicInfoDto getUserBasicInfos(UserTbl user);

    /**
     * 유저의 기본정보(Oauth)를 가져와주는 함수
     * @param UserNo
     * @param userOAuthInfoDto
     * @return
     */
    UserBasicInfoDto getUserInfos(Long UserNo, UserOAuthInfoDto userOAuthInfoDto);

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
