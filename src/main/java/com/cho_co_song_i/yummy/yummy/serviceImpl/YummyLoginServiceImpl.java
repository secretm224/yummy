package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.repository.UserTokenIdRepository;
import com.cho_co_song_i.yummy.yummy.service.JwtProviderService;
import com.cho_co_song_i.yummy.yummy.service.UserService;
import com.cho_co_song_i.yummy.yummy.service.YummyLoginService;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

import static com.cho_co_song_i.yummy.yummy.entity.QUserAuthTbl.userAuthTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserTbl.userTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserTempPwTbl.userTempPwTbl;

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
    private final EventProducerServiceImpl eventProducerServiceImpl;
    private final JPAQueryFactory queryFactory;
    private final UserTokenIdRepository userTokenIdRepository;

    /**
     * 로그인 - 유저가 임시비밀번호 발급을 했는지 확인 (비밀번호 찾기)
     * @param userTbl
     * @return
     */
    private Boolean isTempLoginUser(UserTbl userTbl) {

        UserTempPwTbl userTempPw = queryFactory
                .selectFrom(userTempPwTbl)
                .where(
                        userTempPwTbl.userId.eq(userTbl.getUserId()),
                        userTempPwTbl.userNo.eq(userTbl.getUserNo())
                )
                .fetchFirst();

        return userTempPw != null;
    }


    /**
     * 로그인한 사용자의 정보를 조회해주는 함수 - 필터: 아이디
     * @param userId
     * @return
     */
    private UserTbl findUserLoginInfoById(String userId) {
        return queryFactory
                .selectFrom(userTbl)
                .where(userTbl.userId.eq(userId))
                .fetchFirst();
    }

    /**
     * 로그인한 사용자의 정보를 조회해주는 함수 - 필터: 회원 고유번호
     * @param userNum
     * @return
     */
    private UserTbl findUserLoginInfoByNo(Long userNum) {
        return queryFactory
                .selectFrom(userTbl)
                .where(userTbl.userNo.eq(userNum))
                .fetchFirst();
    }

    /**
     * 로그인에 성공한 유제의 토큰 아이디를 디비에 넣어준다.
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
    private Optional<UserBasicInfoDto> findUserProfileFromRedis(String userNo) throws Exception {
        String keyPrefix = String.format("%s:%s", userInfoKey, userNo);
        UserBasicInfoDto userDto = redisAdapter.getValue(keyPrefix, new TypeReference<UserBasicInfoDto>() {});

        return Optional.ofNullable(userDto);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     * @param res
     * @param userNo
     * @param tokenId
     * @return
     * @throws Exception
     */
    private boolean refreshAccessTokenIfPresent(HttpServletResponse res, String userNo, String tokenId) throws Exception {
        String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, userNo, tokenId);
        String refreshToken = redisAdapter.getValue(refreshKey, new TypeReference<String>() {});

        /* Refresh Token 도 존재하지 않는 경우 -> 로그인 창으로 보내준다. */
        if (refreshToken == null) {
            return false;
        }

        /* 새 access 토큰 발급 후 쿠키 저장 */
        String newAccessToken = jwtProviderService.generateAccessToken(userNo, false, tokenId);
        CookieUtil.addCookie(res, "yummy-access-token", newAccessToken, 7200);
        return true;
    }

    public void processCommonLogin(HttpServletResponse res, StandardLoginBasicResDto loginInfo) {

        /* 유저 정보 */
        UserTbl user = loginInfo.getUserTbl();
        String userNoStr = user.getUserNo().toString();

        /**
         * 1. 로그인 성공시 JWT 토큰을 발급.
         * 임시비밀번호를 발급 받았는지의 여부에 따라 JWT 토큰내부의 내용이 달라진다.
         */
        String tokenId = UUID.randomUUID().toString();
        String accessToken = jwtProviderService.generateAccessToken(userNoStr, loginInfo.isTempUserYn(), tokenId);
        String refreshToken = jwtProviderService.generateRefreshToken(userNoStr);

        /* 2. Refresh Token 을 Redis 에 넣어준다. && DB 에는 Tokenid 를 넣어준다. */
        inputUserTokenId(user, tokenId);
        String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, userNoStr, tokenId);
        redisAdapter.set(refreshKey, refreshToken, Duration.ofDays(7));

        /* 3. accessToken을 쿠키에 저장해준다. */
        CookieUtil.addCookie(res, "yummy-access-token", accessToken, 7200);

        /* 4. 기본적인 유저의 정보를 가져와준다. */
        /* 기본 회원 정보 - 브라우저 돌아다니면서 사용할 수 있는 정보 - private 한 정보같은건 넣으면 안된다. */
        UserBasicInfoDto userBasicInfo = userService.getUserBasicInfos(user);

        /* 5. 기본 회원정보를 Redis 에 저장한다. */
        String basicUserInfo = String.format("%s:%s", userInfoKey, userNoStr);
        redisAdapter.set(basicUserInfo, userBasicInfo);
    }

    public void standardLogoutUser(HttpServletResponse res) {
        CookieUtil.clearCookie(res, "yummy-access-token");
    }


    public UserAuthTbl getUserAuthTbl(String userToken, OauthChannelStatus oauthChannelStatus) {
        return queryFactory
                .selectFrom(userAuthTbl)
                .join(userTbl).on(userTbl.eq(userAuthTbl.user))
                .where(
                        userAuthTbl.id.loginChannel.eq(String.valueOf(oauthChannelStatus)),
                        userAuthTbl.id.tokenId.eq(userToken)
                )
                .fetchFirst();
    }


    private void test() {
        throw new RuntimeException("일부러");
    }

    public StandardLoginBasicResDto verifyLoginUserInfo(StandardLoginDto standardLoginDto) throws Exception {

        /* 1. 사용자 조회 */
        UserTbl user = findUserLoginInfoById(standardLoginDto.getUserId());

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
    public PublicStatus processOauthLogin(UserOAuthResponse userOAuthResponse, HttpServletResponse res) {

        /* 1. 유저정보 조회 */
        UserTbl user = findUserLoginInfoByNo(userOAuthResponse.getUserNum());

        if (user == null) {
            log.info("[YummyLoginService->oauthLogin][Login] No User: {}", userOAuthResponse.getUserNum());
            return PublicStatus.AUTH_ERROR;
        }

        log.info("[YummyLoginService->oauthLogin][Login] Login successful: {}", user.getUserId());

        /* 2. 사용자가 임시비밀번호를 발급받은 사용자인지 체크 */
        boolean tempUserYn = isTempLoginUser(user);

        StandardLoginBasicResDto loginRes = new StandardLoginBasicResDto(PublicStatus.SUCCESS, user, tempUserYn);

        /* 3. 로그인 처리 */
        processCommonLogin(res, loginRes);

        return PublicStatus.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus standardLoginUser(StandardLoginDto standardLoginDto, HttpServletResponse res, HttpServletRequest req) throws Exception {

        /* 로그인 시도 기록 */
        eventProducerServiceImpl.produceLoginAttemptEvent(req);

        StandardLoginBasicResDto loginRes = verifyLoginUserInfo(standardLoginDto);

        if (loginRes.getPublicStatus() != PublicStatus.SUCCESS) {
            log.warn("[YummyLoginService->standardLoginUser][Login] Login failed: {}", standardLoginDto.getUserId());
            return PublicStatus.AUTH_ERROR;
        }

        processCommonLogin(res, loginRes);

        return PublicStatus.SUCCESS;
    }

    public ServiceResponse<Optional<UserBasicInfoDto>> verifyLoginUser(HttpServletResponse res, HttpServletRequest req) throws Exception {

        /* 1. 액세스 토큰 확인 */
        JwtValidationResult jwtResult = userService.validateJwtAndCleanIfInvalid("yummy-access-token", res, req);
        String userNo = userService.getSubjectFromJwt(jwtResult);

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
            if (refreshAccessTokenIfPresent(res, userNo, tokenId)) {
                return ServiceResponse.of(PublicStatus.SUCCESS, findUserProfileFromRedis(userNo));
            } else {
                log.warn("[Warn][YummyLoginService->checkLoginUser] refreshToken expired or does not exist: userNo={}", userNo);
                return ServiceResponse.empty(PublicStatus.AUTH_ERROR);
            }
        }

        /* 4. 그외 모든 오류들은 아래와 같이 처리 */
        return ServiceResponse.empty(PublicStatus.AUTH_ERROR);
    }
}
