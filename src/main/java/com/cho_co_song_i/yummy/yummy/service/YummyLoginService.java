package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.repository.UserTokenIdRepository;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.cho_co_song_i.yummy.yummy.entity.QUserTbl.userTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserTempPwTbl.userTempPwTbl;

@Service
@Slf4j
public class YummyLoginService {

    @Value("${spring.redis.login.user_info}")
    private String userInfoKey;

    @Value("${spring.redis.refresh-key-prefix}")
    private String refreshKeyPrefix;

    @PersistenceContext
    private final EntityManager entityManager;
    private final RedisService redisService;
    private final UserService userService;
    private final JwtProviderService jwtProviderService;
    private final JPAQueryFactory queryFactory;
    private final UserTokenIdRepository userTokenIdRepository;

    public YummyLoginService(RedisService redisService, JwtProviderService jwtProviderService,
                             JPAQueryFactory queryFactory, EntityManager entityManager,
                             UserTokenIdRepository userTokenIdRepository, UserService userService) {
        this.redisService = redisService;
        this.jwtProviderService = jwtProviderService;
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
        this.userTokenIdRepository = userTokenIdRepository;
        this.userService = userService;
    }

    /**
     * 로그인 - 유저가 임시비밀번호 발급을 했는지 확인 (비밀번호 찾기)
     * @param userTbl
     * @return
     */
    private Boolean tempLoginUserCheck(UserTbl userTbl) {

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
     * Oauth2 를 통한 로그인 처리
     * @param userNum
     * @param idToken
     * @param res
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public PublicStatus oauthLogin(Long userNum, String idToken, HttpServletResponse res) throws Exception {

        /* 1. 사용자 조회 */
        UserTbl user = queryFactory
                .selectFrom(userTbl)
                .where(userTbl.userNo.eq(userNum))
                .fetchFirst();

        if (user == null) {
            log.info("[YummyLoginService->oauthLogin][Login] No User: {}", userNum);
            return PublicStatus.AUTH_ERROR;
        }

        log.info("[YummyLoginService->oauthLogin][Login] Login successful: {}", user.getUserId());

        /* 2. 로그인 처리 */
        handlePostLogin(user, false, res);

        return PublicStatus.SUCCESS;
    }

    /**
     * 정석적인 방법으로 로그인하는 경우 -> 아이디/비밀번호 입력해서 로그인 시도
     * @param res
     * @param req
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public PublicStatus standardLoginUser(StandardLoginDto standardLoginDto,
                                                        HttpServletResponse res,
                                                        HttpServletRequest req) throws Exception {
        
        /* 1. 사용자 조회 */
        UserTbl user = queryFactory
                .selectFrom(userTbl)
                .where(userTbl.userId.eq(standardLoginDto.getUserId()))
                .fetchFirst();

        if (user == null) {
            log.info("[YummyLoginService->standardLoginUser][Login] No User: {}", standardLoginDto.getUserId());
            return PublicStatus.AUTH_ERROR;
        }
        
        /* 2. 사용자가 임시비밀번호를 발급받은 사용자인지 체크 */
        boolean tempUserYn = tempLoginUserCheck(user);

        /* 3. 비밀번호 해시 비교 */
        String hashedInput = HashUtil.hashWithSalt(standardLoginDto.getUserPw(), user.getUserPwSalt());

        if (!hashedInput.equals(user.getUserPw())) {
            log.info("[YummyLoginService->standardLoginUser][Login] password mismatch: {}", standardLoginDto.getUserId());
            return PublicStatus.AUTH_ERROR;
        }

        log.info("[YummyLoginService->standardLoginUser][Login] Login successful: {}", standardLoginDto.getUserId());

        handlePostLogin(user, tempUserYn, res);

        return PublicStatus.SUCCESS;
    }


    /**
     * Oauth2 / Standard Login 공통 처리 함수
     * @param user
     * @param tempUserYn
     * @param res
     * @throws Exception
     */
    private void handlePostLogin(UserTbl user, boolean tempUserYn, HttpServletResponse res) throws Exception {

        /* 1. 로그인 성공시 JWT 토큰을 발급. */
        String tokenId = UUID.randomUUID().toString();
        String accessToken = jwtProviderService.generateAccessToken(user.getUserNo().toString(), tempUserYn, tokenId);
        String refreshToken = jwtProviderService.generateRefreshToken(user.getUserNo().toString());

        /* 2. Refresh Token 을 Redis 에 넣어준다. && DB 에는 Tokenid 를 넣어준다. */
        insertUserTokenId(user, tokenId);
        String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, user.getUserNo().toString(), tokenId);
        redisService.set(refreshKey, refreshToken, Duration.ofDays(7));

        /* 3. accessToken을 쿠키에 저장해준다. */
        CookieUtil.addCookie(res, "yummy-access-token", accessToken, 7200);

        /* 4. 기본적인 유저의 정보를 가져와준다. */
        /* 기본 회원 정보 - 브라우저 돌아다니면서 사용할 수 있는 정보 - private 한 정보같은건 넣으면 안된다. */
        UserBasicInfoDto userBasicInfo = userService.getUserBasicInfos(user);

        /* 5. 기본 회원정보를 Redis 에 저장한다. */
        String basicUserInfo = String.format("%s:%s", userInfoKey, user.getUserNo().toString());
        redisService.set(basicUserInfo, userBasicInfo);
    }


    /**
     * 로그인에 성공한 유제의 토큰 아이디를 디비에 넣어준다.
     * @param user
     * @param tokenId
     * @throws Exception
     */
    private void insertUserTokenId(UserTbl user, String tokenId) throws Exception {
        UserTokenIdTbl userTokenIdTbl = new UserTokenIdTbl();
        userTokenIdTbl.setUser(user);

        UserTokenIdTblId userTokenIdTblId = new UserTokenIdTblId(tokenId, user.getUserNo());
        userTokenIdTbl.setId(userTokenIdTblId);
        userTokenIdTbl.setRegDt(new Date());
        userTokenIdTbl.setRegId("system");

        userTokenIdRepository.save(userTokenIdTbl);
    }


    /**
     * 해당 브라우저가 로그인을 했는지 체크해준다. -> Optional.empty() 라면 다시 로그인 해줘야 한다는 의미.
     * @param res
     * @param req
     * @return
     */
    public ServiceResponse<Optional<UserBasicInfoDto>> checkLoginUser(HttpServletResponse res, HttpServletRequest req) {

        /* 1. 액세스 토큰 확인 */
        JwtValidationResult jwtResult = userService.getValidateResultJwt("yummy-access-token", req);
        JwtValidationStatus status = userService.getStatusJwt(jwtResult, res);
        String userNo = userService.getSubjectFromJwt(jwtResult);

        /* 2. 액세스 토큰 이상 없는 경우 */
        if (status == JwtValidationStatus.SUCCESS) {

            /* 임시 비밀번호 발급 받은 경우 */
            Boolean isTempPw = userService.getClaimFromJwt(jwtResult, "isTempPw", Boolean.class);

            if (Boolean.TRUE.equals(isTempPw)) {
                return ServiceResponse.empty(PublicStatus.TEMP_PW_CHECK);
            }

            /* 임시 비밀번호 발급받지 않은 경우 -> 회원정보 가져가준다. */
            return ServiceResponse.of(PublicStatus.SUCCESS, fetchUserProfileFromRedis(userNo));
        }

        /* 3. 액세스 토큰 기간 만료 */
        if (status == JwtValidationStatus.EXPIRED) {
            String tokenId = userService.getClaimFromJwt(jwtResult, "tokenId", String.class);

            /* 리프레시 토큰을 통해서 액세스 토큰 재 발행 */
            if (tryRefreshAccessToken(res, userNo, tokenId)) {
                return ServiceResponse.of(PublicStatus.SUCCESS, fetchUserProfileFromRedis(userNo));
            } else {
                log.warn("[Warn][YummyLoginService->checkLoginUser] refreshToken expired or does not exist: userNo={}", userNo);
                return ServiceResponse.empty(PublicStatus.AUTH_ERROR);
            }
        }

        /* 4. 그외 모든 오류들은 아래와 같이 처리 */
        return ServiceResponse.empty(PublicStatus.AUTH_ERROR);
    }



    /**
     * 유저 정보 조회
     * @param userNo
     * @return
     */
    private Optional<UserBasicInfoDto> fetchUserProfileFromRedis(String userNo) {
        String keyPrefix = String.format("%s:%s", userInfoKey, userNo);
        UserBasicInfoDto userDto = redisService.getValue(keyPrefix, new TypeReference<UserBasicInfoDto>() {});
        log.info("userDto: {}", userDto);

        return Optional.ofNullable(userDto);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     * @param res
     * @param userNo
     * @param tokenId
     * @return
     */
    private boolean tryRefreshAccessToken(HttpServletResponse res, String userNo, String tokenId) {
        String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, userNo, tokenId);
        String refreshToken = redisService.getValue(refreshKey, new TypeReference<String>() {});

        /* Refresh Token 도 존재하지 않는 경우 -> 로그인 창으로 보내준다. */
        if (refreshToken == null) {
            return false;
        }

        /* 새 access 토큰 발급 후 쿠키 저장 */
        String newAccessToken = jwtProviderService.generateAccessToken(userNo, false, tokenId);
        CookieUtil.addCookie(res, "yummy-access-token", newAccessToken, 7200);
        return true;
    }

    /**
     * 유저의 로그아웃을 위해서 로그인관련 인증 토큰을 다 제거해주는 함수
     * @param res
     */
    public void standardLogoutUser(HttpServletResponse res) {
        CookieUtil.clearCookie(res, "yummy-access-token");
    }
}
