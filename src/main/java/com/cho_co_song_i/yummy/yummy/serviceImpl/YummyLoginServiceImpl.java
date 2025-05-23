package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthLoginDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.dto.userCache.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.repository.UserRepository;
import com.cho_co_song_i.yummy.yummy.repository.UserTempPwHistoryRepository;
import com.cho_co_song_i.yummy.yummy.repository.UserTokenIdRepository;
import com.cho_co_song_i.yummy.yummy.service.*;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class YummyLoginServiceImpl implements YummyLoginService {

    @Value("${spring.redis.login.user_info}")
    private String userInfoKey;
    @Value("${spring.redis.refresh-key-prefix}")
    private String refreshKeyPrefix;

    private final RedisAdapter redisAdapter;
    private final UserService userService;
    private final JwtProviderService jwtProviderService;
    private final EventProducerService eventProducerService;

    /* Oauth2 */
    private final KakaoLoginServiceImpl kakaoLoginService;
    private final NaverLoginServiceImpl naverLoginService;
    private final GoogleLoginServiceImpl googleLoginService;

    private final UserTokenIdRepository userTokenIdRepository;
    private final UserTempPwHistoryRepository userTempPwHistoryRepository;
    private final UserRepository userRepository;

    // Error 테스트용
    private void test() {
        throw new RuntimeException("일부러");
    }

    /**
     * 로그인 - 유저가 임시비밀번호 발급을 했는지 확인 (비밀번호 찾기)
     * @param userTbl
     * @return
     */
    private Boolean isTempLoginUser(UserTbl userTbl) {
        return userTempPwHistoryRepository.existsByUserNo(userTbl.getUserNo()) != 0;
    }

    /**
     * 로그인에 성공한 유제의 access token 아이디를 디비에 넣어준다.
     * @param user
     * @param tokenId
     */
    private void inputUserTokenId(UserTbl user, String tokenId) {
        UserTokenIdTbl userTokenIdTbl = new UserTokenIdTbl();
        userTokenIdTbl.setUser(user);

        UserTokenIdTblId userTokenIdTblId = new UserTokenIdTblId(tokenId, user.getUserNo());
        userTokenIdTbl.setId(userTokenIdTblId);
        userTokenIdTbl.setRegDt(new Date());
        userTokenIdTbl.setRegId("system");

        userTokenIdRepository.save(userTokenIdTbl);
    }

    /**
     * 유저 정보 조회 - Redis 로부터 유저 정보를 조회해준다.
     * @param userNo
     * @return
     * @throws Exception
     */
    private Optional<UserBasicInfoDto> findUserProfileFromRedis(Long userNo) throws Exception {
        String keyPrefix = String.format("%s:%s", userInfoKey, userNo);
        UserBasicInfoDto userDto = redisAdapter.getValue(keyPrefix, new TypeReference<UserBasicInfoDto>() {});

        return Optional.ofNullable(userDto);
    }


//    private UserBasicInfoDto findAndModifyUserProfile(Long userNo, OauthChannelStatus loginChannel) throws Exception {
//
//        /**
//         * Oauth 에서 데이터를 가져오고 해당 데이터를 Redis 에 넣어준다.
//         *
//         */
//        OauthUserSimpleInfoDto oauthUserSimpleInfoDto = new OauthUserSimpleInfoDto();
//
//        if (loginChannel == OauthChannelStatus.kakao) {
//            LoginService kakaoLogin = loginServiceMap.get("KakaoLoginServiceImpl");
//
//
//        } else if (loginChannel == OauthChannelStatus.naver) {
//
//        } else if (loginChannel == OauthChannelStatus.telegram) {
//
//        } else if (loginChannel == OauthChannelStatus.google) {
//
//        } else {
//
//        }
//
//        /* Redis 에서 가져오는 유저 정보 */
//        //Optional<UserBasicInfoDto> userBasicInfoDto = findUserProfileFromRedis(userNo);
//    }


    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     * @param res
     * @param userNo
     * @param tokenId
     * @return
     * @throws Exception
     */
    private boolean refreshAccessTokenIfPresent(HttpServletResponse res, Long userNo, String tokenId, OauthChannelStatus loginChannel) throws Exception {
        String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, userNo, tokenId);
        String refreshToken = redisAdapter.getValue(refreshKey, new TypeReference<String>() {});

        /* Refresh Token 도 존재하지 않는 경우 -> 로그인 창으로 보내준다. */
        if (refreshToken == null) {
            return false;
        }

        /* 새 access 토큰 발급 후 쿠키 저장 */
        String newAccessToken = jwtProviderService.generateAccessToken(userNo, false, tokenId, loginChannel);
        CookieUtil.addCookie(res, "yummy-access-token", newAccessToken, 7200);
        return true;
    }

    public void processCommonLogin(HttpServletResponse res, StandardLoginBasicResDto loginInfo, OauthChannelStatus loginChannel) throws Exception {

        /* 유저 정보 */
        UserTbl user = loginInfo.getUserTbl();
        Long userNo = user.getUserNo();
        //String userNoStr = user.getUserNo().toString();

        /**
         * 1. 로그인 성공시 JWT 토큰을 발급.
         * 임시비밀번호를 발급 받았는지의 여부에 따라 JWT 토큰내부의 내용이 달라진다.
         */
        String tokenId = UUID.randomUUID().toString(); /* yummy service 전용 토큰 아이디 */
        String accessToken = jwtProviderService.generateAccessToken(userNo, loginInfo.isTempUserYn(), tokenId, loginChannel);
        String refreshToken = jwtProviderService.generateRefreshToken(userNo);

        /* 2. Refresh Token 을 Redis 에 넣어준다. && DB 에는 Tokenid 를 넣어준다. */
        inputUserTokenId(user, tokenId);
        String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, userNo, tokenId);
        redisAdapter.set(refreshKey, refreshToken, Duration.ofDays(7));

        /* 3. accessToken을 쿠키에 저장해준다. */
        CookieUtil.addCookie(res, "yummy-access-token", accessToken, 7200);

        /* 4. 기본적인 유저의 정보를 가져와준다. */
        /* 기본 회원 정보 - 브라우저 돌아다니면서 사용할 수 있는 정보 - private 한 정보같은건 넣으면 안된다. */
        UserBasicInfoDto userBasicInfo = userService.getUserBasicInfos(user, loginChannel);

        /* 5. 기본 회원정보를 Redis 에 저장한다. */
        String basicUserInfo = String.format("%s:%s", userInfoKey, userNo);
        redisAdapter.set(basicUserInfo, userBasicInfo);
    }

    public void standardLogoutUser(HttpServletResponse res) {
        CookieUtil.clearCookie(res, "yummy-access-token");
    }

    public StandardLoginBasicResDto verifyLoginUserInfo(StandardLoginDto standardLoginDto) throws Exception {

        /* 1. 사용자 조회 */
        UserTbl user = userRepository.findUserByLoginId(standardLoginDto.getUserId());

        if (user == null) {
            log.info("[YummyLoginService->standardLoginUser][Login] No User: {}", standardLoginDto.getUserId());
            return new StandardLoginBasicResDto(PublicStatus.AUTH_ERROR, null, false);
        }

        /* 2. 사용자가 임시비밀번호를 발급받은 사용자인지 체크 */
        boolean tempUserYn = isTempLoginUser(user);

        /* 3. 비밀번호 해시 비교 */
        String hashedInput = HashUtil.hashWithSalt(standardLoginDto.getUserPw(), user.getUserPwSalt());

        if (!hashedInput.equals(user.getUserPw())) {
            log.info("[YummyLoginService->standardLoginUser][Login] password mismatch: {}", standardLoginDto.getUserId());
            return new StandardLoginBasicResDto(PublicStatus.AUTH_ERROR, null, tempUserYn);
        }

        log.info("[YummyLoginService->standardLoginUser][Login] Login successful: {}", standardLoginDto.getUserId());

        return new StandardLoginBasicResDto(PublicStatus.SUCCESS, user, tempUserYn);
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus processOauthLogin(OauthLoginDto loginDto, HttpServletResponse res, HttpServletRequest req) throws Exception {
        /* 로그인 시도 기록 */
        eventProducerService.produceLoginAttemptEvent(req);

        /* 유저의 Oauth에 대한 정보 -> 각 Oauth 별로 진행 */
        UserOAuthResponse userOAuthResponse = new UserOAuthResponse();

        // UserOAuthResponse userOAuthResponse = getOauthLoginInfo(loginDto.getCode());
        if (loginDto.getOauthType().equals(OauthChannelStatus.kakao.toString())) {
            userOAuthResponse = kakaoLoginService.getOauthLoginInfo(loginDto.getCode());
        }

//        else if (loginDto.getOauthType().equals(OauthChannelStatus.kakao.toString()) {
//
//        } else if (loginDto.getOauthType().equals(OauthChannelStatus.kakao.toString()) {
//
//        } else {
//
//        }


        return PublicStatus.SUCCESS;
    }

//    @Transactional(rollbackFor = Exception.class)
//    public PublicStatus processOauthLogin(UserOAuthResponse userOAuthResponse, HttpServletResponse res) throws Exception {
//
//        /* 1. 유저정보 조회 */
//        UserTbl user = userOAuthResponse.getUserTbl();
//        log.info("[YummyLoginService->oauthLogin][Login] Login successful: {}", user.getUserId());
//
//        /* 2. 사용자가 임시비밀번호를 발급받은 사용자인지 체크 */
//        boolean tempUserYn = isTempLoginUser(user);
//
//        StandardLoginBasicResDto loginRes = new StandardLoginBasicResDto(PublicStatus.SUCCESS, user, tempUserYn);
//
//        /* 3. 로그인 처리(Oauth2)  */
//        processCommonLogin(res, loginRes, userOAuthResponse.getLoginChannel());
//
//        return PublicStatus.SUCCESS;
//    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus standardLoginUser(StandardLoginDto standardLoginDto, HttpServletResponse res, HttpServletRequest req) throws Exception {

        /* 로그인 시도 기록 */
        eventProducerService.produceLoginAttemptEvent(req);

        StandardLoginBasicResDto loginRes = verifyLoginUserInfo(standardLoginDto);

        if (loginRes.getPublicStatus() != PublicStatus.SUCCESS) {
            log.warn("[YummyLoginService->standardLoginUser][Login] Login failed: {}", standardLoginDto.getUserId());
            return PublicStatus.AUTH_ERROR;
        }

        processCommonLogin(res, loginRes, OauthChannelStatus.standard);

        return PublicStatus.SUCCESS;
    }

    public ServiceResponse<Optional<UserBasicInfoDto>> verifyLoginUser(HttpServletResponse res, HttpServletRequest req) throws Exception {

        /* 1. 액세스 토큰 확인 */
        JwtValidationResult jwtResult = userService.validateJwtAndCleanIfInvalid("yummy-access-token", res, req);
        Long userNo = Long.parseLong(userService.getSubjectFromJwt(jwtResult));
        OauthChannelStatus channel = OauthChannelStatus
                .valueOf(userService.getClaimFromJwt(jwtResult, "oauthChannel", String.class));

        /* 2. 액세스 토큰 이상 없는 경우 */
        if (jwtResult.getStatus() == JwtValidationStatus.SUCCESS) {

            /* 임시 비밀번호 발급 받은 경우 */
            Boolean isTempPw = userService.getClaimFromJwt(jwtResult, "isTempPw", Boolean.class);

            if (Boolean.TRUE.equals(isTempPw)) {
                return ServiceResponse.empty(PublicStatus.TEMP_PW_CHECK);
            }

            /* 임시 비밀번호 발급받지 않은 경우 -> 회원정보 가져가준다. */
            return ServiceResponse.of(PublicStatus.SUCCESS, findUserProfileFromRedis(userNo));
        }

        /* 3. 액세스 토큰 기간 만료 */
        if (jwtResult.getStatus() == JwtValidationStatus.EXPIRED) {
            String tokenId = userService.getClaimFromJwt(jwtResult, "tokenId", String.class);

            /* 리프레시 토큰을 통해서 액세스 토큰 재 발행 */
            if (refreshAccessTokenIfPresent(res, userNo, tokenId, channel)) {
                return ServiceResponse.of(PublicStatus.SUCCESS, findUserProfileFromRedis(userNo));
            } else {
                log.warn("[Warn][YummyLoginService->checkLoginUser] refreshToken expired or does not exist: userNo={}", userNo);
                return ServiceResponse.empty(PublicStatus.AUTH_ERROR);
            }
        }

        /* 4. 그외 모든 오류들은 아래와 같이 처리 */
        return ServiceResponse.empty(PublicStatus.AUTH_ERROR);
    }


    public void testing() {

        System.out.println("????????????????????????????????????????");
        //LoginService kakaoLogin = loginServiceMap.get("KakaoLoginServiceImpl");
        //kakaoLogin.getUserInfosByOauth(1L);
//        UserTbl user = userRepository.findUserByLoginId("ssh9308");
//        System.out.println("=========================================================");
//        System.out.println(user.getUserNo());

    }
}
