package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.dto.LocationCityDto;
import com.cho_co_song_i.yummy.yummy.dto.UserProfileDto;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.utils.AesUtil;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.cho_co_song_i.yummy.yummy.utils.CookieUtil.getCookieValue;

@Service
@Slf4j
public class YummyLoginService {

    @Value("${spring.redis.login.user_info}")
    private String userInfoKey;

    @Value("${spring.redis.refresh-key-prefix}")
    private String refreshKeyPrefix;

    private final RedisService redisService;
    private final JwtProviderService jwtProviderService;

    public YummyLoginService(RedisService redisService, JwtProviderService jwtProviderService) {
        this.redisService = redisService;
        this.jwtProviderService = jwtProviderService;
    }

    /**
     * 해당 브라우저가 로그인을 했는지 체크해준다. -> Optional.empty() 라면 다시 로그인 해줘야 한다는 의미.
     * @param res
     * @param req
     * @return
     */
    public Optional<UserProfileDto> checkLoginUser(HttpServletResponse res, HttpServletRequest req) {

        String accessToken = getCookieValue(req, "yummy-access-token");
        String userNumHash = getCookieValue(req, "yummy-user-number");

        if (accessToken == null || userNumHash == null) {
            return Optional.empty();
        }

        /* 1. 액세스 토큰 유효성 검증 */
        JwtValidationResult jwtResult = jwtProviderService.validateTokenAndGetSubject(accessToken);

        /* 2. 유효한 토큰이면 → Redis 에서 유저 정보 조회 */
        if (jwtResult.getStatus() == JwtValidationStatus.SUCCESS) {
            return fetchUserProfileFromRedis(userNumHash);
        }

        /* 3. 액세스 토큰 만료 → refresh 토큰 검증 및 access 재발급 */
        if (jwtResult.getStatus() == JwtValidationStatus.EXPIRED) {
            boolean refreshed = tryRefreshAccessToken(userNumHash, res);
            if (refreshed) {
                return fetchUserProfileFromRedis(userNumHash);
            } else {
                log.warn("[Warn][YummyLoginService->checkLoginUser] refreshToken expired or does not exist: userNumHash={}", userNumHash);
                return Optional.empty();
            }
        }

        /* 4. 그 외 INVALID, 오류 등 */
        return Optional.empty();
    }

    /**
     * 유저 정보 조회
     * @param userNumHash
     * @return
     */
    private Optional<UserProfileDto> fetchUserProfileFromRedis(String userNumHash) {
        String keyPrefix = String.format("%s:%s", userInfoKey, userNumHash);
        UserProfileDto userDto = redisService.getValue(keyPrefix, new TypeReference<UserProfileDto>() {});
        return Optional.ofNullable(userDto);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     * @param userNumHash
     * @param res
     * @return
     */
    private boolean tryRefreshAccessToken(String userNumHash, HttpServletResponse res) {
        String refreshKey = String.format("%s:%s", refreshKeyPrefix, userNumHash);
        String refreshToken = redisService.getValue(refreshKey, new TypeReference<String>() {});

        if (refreshToken == null) {
            return false;
        }

        /* 새 access 토큰 발급 후 쿠키 저장 */
        String newAccessToken = jwtProviderService.generateAccessToken(userNumHash);
        CookieUtil.addCookie(res, "yummy-access-token", newAccessToken, 1000 * 60 * 60 * 2);
        return true;
    }



}
