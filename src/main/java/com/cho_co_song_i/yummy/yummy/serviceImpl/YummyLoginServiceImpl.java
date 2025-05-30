package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.component.JwtProvider;
import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthLoginDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthUserSimpleInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.dto.userCache.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
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
    @Value("${spring.redis.oauth_main_channel}")
    private String oauthMainChannelPrefix;

    private final LoginServiceFactory loginServiceFactory;
    private final RedisAdapter redisAdapter;
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final EventProducerService eventProducerService;

    /* Error 테스트용 */
    private void test() {
        throw new RuntimeException("일부러");
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

    /**
     * 유저 정보 조회 = Redis 조회 + Oauth 조회
     * @param userNo
     * @param loginChannel
     * @return
     * @throws Exception
     */
    private Optional<UserBasicInfoDto> getUserProfileByChannel(Long userNo, OauthChannelStatus loginChannel) throws Exception {
        return findUserProfileFromRedis(userNo)
                .map(userInfo -> {
                    if (loginChannel == OauthChannelStatus.standard) {

                        try {

                            String key = String.format("%s:%s", oauthMainChannelPrefix, userNo);
                            String channel = redisAdapter.getValue(key, new TypeReference<String>() {});

                            if (channel != null) {
                                LoginService loginService = loginServiceFactory.getService(channel);
                                OauthUserSimpleInfoDto oauthInfo = loginService.getUserInfosByOauth(userNo);
                                userInfo.setUserPic(oauthInfo.getProfileImg());
                            }

                        } catch (Exception e) {
                            log.error("[Error][YummyLoginServiceImpl->getUserProfileByChannel] oauthChannel lookup failed - userNo: {}, error: {}", userNo, e.getMessage());
                        }

                    } else {
                        LoginService loginService = loginServiceFactory.getService(loginChannel);
                        OauthUserSimpleInfoDto oauthInfo = loginService.getUserInfosByOauth(userNo);
                        userInfo.setUserPic(oauthInfo.getProfileImg());
                    }
                    return userInfo;
                });
    }


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
        String newAccessToken = jwtProvider.generateAccessToken(userNo, false, tokenId, loginChannel);
        CookieUtil.addCookie(res, "yummy-access-token", newAccessToken, 7200);
        return true;
    }

    /**
     * Oauth 타입마다 임시 토큰 발급해주는 메소드
     * @param res
     * @param userOAuthResponse
     */
    private void generateTempOauthCookieByChannel(HttpServletResponse res, UserOAuthResponse userOAuthResponse) {

        String idToken = "";

        if (userOAuthResponse.getLoginChannel() == OauthChannelStatus.kakao) {
            idToken = userOAuthResponse.getKakaoOauthInfoDto().getOauthUserSimpleInfoDto().getUserTokenId();
        }
        /* 여기에 이어서 다른 채널 로직도 짜주면 된다...*/

        jwtProvider.generateTempOauthJwtCookie(res, idToken, userOAuthResponse.getLoginChannel());
    }

    public void processCommonLogin(HttpServletResponse res, StandardLoginBasicResDto loginInfo, OauthChannelStatus loginChannel) {

        /* 유저 정보 */
        UserTbl user = loginInfo.getUserTbl();
        Long userNo = user.getUserNo();

        /**
         * 1. 로그인 성공시 JWT 토큰을 발급.
         * 임시비밀번호를 발급 받았는지의 여부에 따라 JWT 토큰내부의 내용이 달라진다.
         */
        String tokenId = UUID.randomUUID().toString(); /* yummy service 전용 토큰 아이디 */
        String accessToken = jwtProvider.generateAccessToken(userNo, loginInfo.isTempUserYn(), tokenId, loginChannel);
        String refreshToken = jwtProvider.generateRefreshToken(userNo);

        /* 2. Refresh Token 을 Redis 에 넣어준다. && DB 에는 Tokenid 를 넣어준다. */
        userService.inputUserTokenId(user, tokenId);
        String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, userNo, tokenId);
        redisAdapter.set(refreshKey, refreshToken, Duration.ofDays(7));

        /* 3. accessToken을 쿠키에 저장해준다. */
        CookieUtil.addCookie(res, "yummy-access-token", accessToken, 7200);

        /* 4. 기본적인 유저의 정보를 가져와준다. */
        /* 기본 회원 정보 - 브라우저 돌아다니면서 사용할 수 있는 정보 */
        UserBasicInfoDto userBasicInfo = userService.getUserBasicInfos(user);

        /* 5. 기본 회원정보를 Redis 에 저장한다. */
        String basicUserInfo = String.format("%s:%s", userInfoKey, userNo);
        redisAdapter.set(basicUserInfo, userBasicInfo);
    }

    public void standardLogoutUser(HttpServletResponse res) {
        CookieUtil.clearCookie(res, "yummy-access-token");
    }

    public StandardLoginBasicResDto verifyAndGetLoginUserInfo(StandardLoginDto standardLoginDto) throws Exception {

        /* 1. 사용자 조회 */
        UserTbl user = userService.findUserByLoginId(standardLoginDto.getUserId());

        if (user == null) {
            log.info("[YummyLoginService->standardLoginUser][Login] No User: {}", standardLoginDto.getUserId());
            return new StandardLoginBasicResDto(PublicStatus.AUTH_ERROR, null, false);
        }

        /* 2. 사용자가 임시비밀번호를 발급받은 사용자인지 체크 */
        boolean tempUserYn = userService.isTempLoginUser(user);

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
        /* 1. 로그인 시도 기록 */
        eventProducerService.produceLoginAttemptEvent(req);

        String type = loginDto.getOauthType();

        /* 2. Oauth 채널에 맞는 로그인 서비스 가져오기 */
        LoginService loginService = loginServiceFactory.getService(loginDto.getOauthType());

        /* 3. Oauth 토큰 정보 조회 */
        UserOAuthResponse userOAuthResponse = loginService.getOauthLoginInfo(loginDto.getCode());

        if (userOAuthResponse.getPublicStatus() == PublicStatus.SUCCESS) {
            /* 이미 연동되어있는 유저인 경우 */
            /* 3-1. 사용자 정보 조회 */
            UserTbl user = userOAuthResponse.getUserTbl();
            log.info("[YummyLoginService->processOauthLogin] Login successful: {}", user.getUserId());

            /* 3-2. 임시 비밀번호 여부 확인 */
            boolean tempUserYn = userService.isTempLoginUser(user);
            StandardLoginBasicResDto loginRes = new StandardLoginBasicResDto(PublicStatus.SUCCESS, user, tempUserYn);

            /* 3-3. Redis에 Oauth 토큰 저장 */
            loginService.saveOauthTokenToRedis(user.getUserNo(), userOAuthResponse);

            /* 3-4. 공통 로그인 처리 (쿠키 생성 등) */
            processCommonLogin(res, loginRes, userOAuthResponse.getLoginChannel());

        } else if (userOAuthResponse.getPublicStatus() == PublicStatus.JOIN_TARGET_MEMBER) {
            /* 연동되어 있지 않은 유저인 경우 */
            generateTempOauthCookieByChannel(res, userOAuthResponse);
        }

        return userOAuthResponse.getPublicStatus();
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus standardLoginUser(StandardLoginDto standardLoginDto, HttpServletResponse res, HttpServletRequest req) throws Exception {

        /* 로그인 시도 기록 */
        eventProducerService.produceLoginAttemptEvent(req);

        StandardLoginBasicResDto loginRes = verifyAndGetLoginUserInfo(standardLoginDto);

        if (loginRes.getPublicStatus() != PublicStatus.SUCCESS) {
            log.warn("[YummyLoginService->standardLoginUser][Login] Login failed: {}", standardLoginDto.getUserId());
            return PublicStatus.AUTH_ERROR;
        }

        processCommonLogin(res, loginRes, OauthChannelStatus.standard);

        return PublicStatus.SUCCESS;
    }

    public ServiceResponse<Optional<UserBasicInfoDto>> verifyLoginUser(HttpServletResponse res, HttpServletRequest req) throws Exception {

        /* 1. 액세스 토큰 확인 */
        JwtValidationResult jwtResult = jwtProvider.validateJwtAndCleanIfInvalid("yummy-access-token", res, req);

        // ??? 이쪽부분 뭔가가 이상한데??...
        if (jwtResult.getStatus() == JwtValidationStatus.EMPTY) return ServiceResponse.empty(PublicStatus.SUCCESS);

        Long userNo = Long.parseLong(jwtProvider.getSubjectFromJwt(jwtResult));
        OauthChannelStatus channel = OauthChannelStatus
                .valueOf(jwtProvider.getClaimFromJwt(jwtResult, "oauthChannel", String.class));

        /* 2. 액세스 토큰 이상 없는 경우 */
        if (jwtResult.getStatus() == JwtValidationStatus.SUCCESS) {

            /* 임시 비밀번호 발급 받은 경우 */
            Boolean isTempPw = jwtProvider.getClaimFromJwt(jwtResult, "isTempPw", Boolean.class);

            if (Boolean.TRUE.equals(isTempPw)) {
                return ServiceResponse.empty(PublicStatus.TEMP_PW_CHECK);
            }

            /* 임시 비밀번호 발급받지 않은 경우 -> 회원정보 가져가준다. */
            return ServiceResponse.of(PublicStatus.SUCCESS, getUserProfileByChannel(userNo, channel));
        }

        /* 3. 액세스 토큰 기간 만료 */
        if (jwtResult.getStatus() == JwtValidationStatus.EXPIRED) {
            String tokenId = jwtProvider.getClaimFromJwt(jwtResult, "tokenId", String.class);

            /* 리프레시 토큰을 통해서 액세스 토큰 재 발행 */
            if (refreshAccessTokenIfPresent(res, userNo, tokenId, channel)) {
                return ServiceResponse.of(PublicStatus.SUCCESS, getUserProfileByChannel(userNo, channel));
            } else {
                log.warn("[Warn][YummyLoginService->checkLoginUser] refreshToken expired or does not exist: userNo={}", userNo);
                return ServiceResponse.empty(PublicStatus.AUTH_ERROR);
            }
        }

        /* 4. 그외 모든 오류들은 아래와 같이 처리 */
        return ServiceResponse.empty(PublicStatus.AUTH_ERROR);
    }

    public PublicStatus verifyOauthTokenValid(HttpServletResponse res, HttpServletRequest req) {
        /* 유효한 Oauth 토큰인지 확인 */
        JwtValidationResult jwtResult = jwtProvider.validateJwtAndCleanIfInvalid("yummy-oauth-token", res, req);
        return jwtResult.getStatus() == JwtValidationStatus.SUCCESS ? PublicStatus.SUCCESS : PublicStatus.AUTH_ERROR;
    }

    /**
     * 테스트 용 메소드
     */
    public void testing() {}
}
