package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.UserLocationDetailTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.utils.AesUtil;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

import static com.cho_co_song_i.yummy.yummy.utils.CookieUtil.getCookieValue;
import static com.cho_co_song_i.yummy.yummy.entity.QUserTbl.userTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserLocationDetailTbl.userLocationDetailTbl;

@Service
@Slf4j
public class YummyLoginService {

    @Value("${spring.redis.login.user_info}")
    private String userInfoKey;

    @Value("${spring.redis.refresh-key-prefix}")
    private String refreshKeyPrefix;

    private final RedisService redisService;
    private final JwtProviderService jwtProviderService;
    private final JPAQueryFactory queryFactory;

    public YummyLoginService(RedisService redisService, JwtProviderService jwtProviderService,
                             JPAQueryFactory queryFactory) {
        this.redisService = redisService;
        this.jwtProviderService = jwtProviderService;
        this.queryFactory = queryFactory;
    }

    /* Entity -> DTO 변환 (UserTbl) */
    private UserBasicInfoDto convertUserToBasicInfo(UserTbl userTbl, UserLocationDetailTbl userLocationDetail) {
        return new UserBasicInfoDto(
                userTbl.getUserId(),
                userTbl.getUserNm(),
                userTbl.getUserBirth(),
                userLocationDetail.getLngX(),
                userLocationDetail.getLatY()
        );
    }

    /**
     * 정석적인 방법으로 로그인하는 경우 -> 아이디/비밀번호 입력해서 로그인 시도
     * @param res
     * @param req
     * @return
     */
    public Optional<UserBasicInfoDto> standardLoginUser(StandardLoginDto standardLoginDto,
                                                        HttpServletResponse res,
                                                        HttpServletRequest req) {
        try {

            /* 1. 사용자 조회 */
            UserTbl user = queryFactory
                    .selectFrom(userTbl)
                    .where(userTbl.userId.eq(standardLoginDto.getUserId()))
                    .fetchFirst();

            if (user == null) {
                log.info("[YummyLoginService->standardLoginUser][Login] No User: {}", standardLoginDto.getUserId());
                return Optional.empty();
            }

            /* 2. 비밀번호 해시 비교 */
            String hashedInput = HashUtil.hashWithSalt(standardLoginDto.getUserPw(), user.getUserPwSalt());

            if (!hashedInput.equals(user.getUserPw())) {
                log.info("[YummyLoginService->standardLoginUser][Login] password mismatch: {}", standardLoginDto.getUserId());
                return Optional.empty();
            }

            /* 3. 로그인 성공 */
            log.info("[YummyLoginService->standardLoginUser][Login] Login successful: {}", standardLoginDto.getUserId());

            /* 4. 로그인 성공시 JWT 토큰을 발급한다. */
            String hashedId = user.getUserIdHash();
            String accessToken = jwtProviderService.generateAccessToken(hashedId);
            String refreshToken = jwtProviderService.generateRefreshToken(hashedId);

            /* 5. Refresh Token 을 Redis 에 넣어준다. */
            String refreshKey = String.format("%s:%s", refreshKeyPrefix, hashedId);
            redisService.set(refreshKey, refreshToken);

            /* 6. accessToken && 해시화된 아이디를 쿠키에 저장해준다. */
            CookieUtil.addCookie(res, "yummy-access-token", accessToken, 7200);
            CookieUtil.addCookie(res, "yummy-user-id", hashedId, 7200);

            /* 7. 기본적인 유저의 정보를 가져와준다. */
            /* 7-1. 회원의 집 정보 */
            UserLocationDetailTbl userLocationDetail = queryFactory
                    .selectFrom(userLocationDetailTbl)
                    .where(userLocationDetailTbl.id.userNo.eq(user.getUserNo()))
                    .fetchFirst();

            /* 기본 회원 정보 - 브라우저 돌아다니면서 사용할 수 있는 정보 - private 한 정보같은건 넣으면 안된다. */
            UserBasicInfoDto userBasicInfo = convertUserToBasicInfo(user, userLocationDetail);

            /* 7. 기본 회원정보를 Redis 에 저장한다. */
            String basicUserInfo = String.format("%s:%s", userInfoKey, hashedId);
            redisService.set(basicUserInfo, userBasicInfo);

            return Optional.of(userBasicInfo);
        } catch (Exception e) {
            log.error("[Error][YummyLoginService->standardLoginUser] {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 해당 브라우저가 로그인을 했는지 체크해준다. -> Optional.empty() 라면 다시 로그인 해줘야 한다는 의미.
     * @param res
     * @param req
     * @return
     */
    public Optional<UserBasicInfoDto> checkLoginUser(HttpServletResponse res, HttpServletRequest req) {

        String accessToken = getCookieValue(req, "yummy-access-token");
        String userIdHash = getCookieValue(req, "yummy-user-id");

        if (accessToken == null || userIdHash == null) {
            return Optional.empty();
        }

        /* 1. 액세스 토큰 유효성 검증 */
        JwtValidationResult jwtResult = jwtProviderService.validateTokenAndGetSubject(accessToken);

        /* 2. 유효한 토큰이면 → Redis 에서 유저 정보 조회 */
        if (jwtResult.getStatus() == JwtValidationStatus.SUCCESS) {
            return fetchUserProfileFromRedis(userIdHash);
        }

        /* 3. 액세스 토큰 만료 → refresh 토큰 검증 및 access 재발급 */
        if (jwtResult.getStatus() == JwtValidationStatus.EXPIRED) {
            boolean refreshed = tryRefreshAccessToken(userIdHash, res);
            if (refreshed) {
                return fetchUserProfileFromRedis(userIdHash);
            } else {
                log.warn("[Warn][YummyLoginService->checkLoginUser] refreshToken expired or does not exist: userIdHash={}", userIdHash);
                return Optional.empty();
            }
        }

        /* 4. 그 외 INVALID, 오류 등 */
        return Optional.empty();
    }

    /**
     * 유저 정보 조회
     * @param userIdHash
     * @return
     */
    private Optional<UserBasicInfoDto> fetchUserProfileFromRedis(String userIdHash) {
        String keyPrefix = String.format("%s:%s", userInfoKey, userIdHash);
        UserBasicInfoDto userDto = redisService.getValue(keyPrefix, new TypeReference<UserBasicInfoDto>() {});
        return Optional.ofNullable(userDto);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     * @param userIdHash
     * @param res
     * @return
     */
    private boolean tryRefreshAccessToken(String userIdHash, HttpServletResponse res) {
        String refreshKey = String.format("%s:%s", refreshKeyPrefix, userIdHash);
        String refreshToken = redisService.getValue(refreshKey, new TypeReference<String>() {});

        /* Refresh Token 도 존재하지 않는 경우 -> 로그인 창으로 보내준다. */
        if (refreshToken == null) {
            return false;
        }

        /* 새 access 토큰 발급 후 쿠키 저장 */
        String newAccessToken = jwtProviderService.generateAccessToken(userIdHash);
        CookieUtil.addCookie(res, "yummy-access-token", newAccessToken, 1000 * 60 * 60 * 2);
        return true;
    }

    /**
     * 유저의 로그아웃을 위해서 로그인관련 인증 토큰을 다 제거해주는 함수
     * @param res
     */
    public void standardLogoutUser(HttpServletResponse res) {
        CookieUtil.clearCookies(res, "yummy-access-token", "yummy-user-id");
    }


}
