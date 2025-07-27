package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.JoinMemberDto;
import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.dto.userCache.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.UserOauthGoogleTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserOauthKakaoTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserTempPwTbl;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface UserService {
    /**
     * Redis 에 저장할 유저의 기본 정보
     * @param user
     * @return
     */
    UserBasicInfoDto getUserBasicInfos(UserTbl user);
    /**
     * UserTbl에 OAuth 메인 채널을 지정해주는 메소드
     * @param loginChannel
     * @param user
     */
    void modifyUserTblMainOauthChannel(OauthChannelStatus loginChannel, UserTbl user);
    /**
     * 로그인에 성공한 유제의 access token 아이디를 디비에 넣어준다.
     * @param user
     * @param tokenId
     */
    void inputUserTokenId(UserTbl user, String tokenId);
    /**
     * 로그인 - 유저가 임시비밀번호 발급을 했는지 확인
     * @param userTbl
     * @return
     */
    Boolean isTempLoginUser(UserTbl userTbl);
    /**
     * 유저 아이디를 통해서 유저 전체의 정보를 가져와준다.
     * @param userId
     * @return
     */
    UserTbl findUserByLoginId(String userId);
    /**
     * 유저의 고유번호를 통해서 유저 전체의 정보를 가져와준다.
     * @param userNo
     * @return
     */
    Optional<UserTbl> findUserByUserNo(Long userNo);
    /**
     * UserTempPwTbl 을 insert 해주는 함수
     * @param userTempPwTbl
     */
    void inputUserTempPwTbl(UserTempPwTbl userTempPwTbl);
    /**
     * UserEmailTbl을 insert 해주는 함수
     * @param user
     * @param email
     */
    void inputUserEmail(UserTbl user, String email);
    /**
     * UserTbl 객체를 DB에 생성하고 반환
     * @param joinMemberDto
     * @return
     * @throws Exception
     */
    UserTbl createUser(JoinMemberDto joinMemberDto) throws Exception;
    /**
     * userPhoneNumberTbl 생성 함수
     * @param user
     * @param phoneNumber
     * @param telecom
     */
    void inputUserPhoneNumber(UserTbl user, String phoneNumber, String telecom);
    /**
     * 유저 고유번호에 대응하는 UserTempPwTbl 데이터를 반환
     * @param userNo
     * @return
     */
    Optional<UserTempPwTbl> findUserTempPwTblByUserNo(Long userNo);
    /**
     * UserTempPwTbl 데이터를 delete 해주는 함수
     * @param userTempPwTbl
     */
    void deleteUserTempPwTbl(UserTempPwTbl userTempPwTbl);
    /**
     * 회원이 입력한 이메일 주소가 기존에 사용중인 이메일 주소인지 확인해주는 함수
     * @param email
     * @return
     */
    boolean isDuplicatedUserEmail(String email);
    /**
     * TokenId 에 대응되는 UserOauthKakaoTbl 데이터를 반환해주는 함수
     * @param tokenId
     * @return
     */
    Optional<UserOauthKakaoTbl> findUserOauthKakaoTblByTokenId(String tokenId);
    /**
     * TokenId 에 대응되는 UserOauthGoogleTbl 데이터를 반환해주는 함수
     * @param tokenId
     * @return
     */
    Optional<UserOauthGoogleTbl> findUserOauthGoogleTblByTokenId(String tokenId);
    /**
     * UserOauthKakaoTbl 데이터를 DB에 넣어주는 함수
     * @param userOauthKakaoTbl
     */
    void inputUserOauthKakaoTbl(UserOauthKakaoTbl userOauthKakaoTbl);
    /**
     * UserOauthGoogleTbl 데이터를 DB에 넣어주는 함수
     * @param userOauthGoogleTbl
     */
    void inputUserOauthGoogleTbl(UserOauthGoogleTbl userOauthGoogleTbl);
    /**
     * 유저의 고유번호를 통해서 UserOauthKakaoTbl 데이터를 가져와주는 함수
     * @param userNo
     * @return
     */
    Optional<UserOauthKakaoTbl> findUserOauthKakaoTblByUserNo(Long userNo);
    /**
     * 유저의 고유번호를 통해서 UserOauthGoogleTbl 데이터를 가져와주는 함수
     * @param userNo
     * @return
     */
    Optional<UserOauthGoogleTbl> findUserOauthGoogleTblByUserNo(Long userNo);
}
