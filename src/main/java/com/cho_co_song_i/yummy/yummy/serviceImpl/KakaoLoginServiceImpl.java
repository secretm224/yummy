package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthUserSimpleInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.kakao.KakaoOauthInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.kakao.KakaoUserInfoRaw;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import com.cho_co_song_i.yummy.yummy.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.cho_co_song_i.yummy.yummy.utils.JwtUtil.decodeJwtPayload;


@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoLoginServiceImpl implements LoginService {
    @Value("${kakao.auth.url}")
    private String kakaoAuthUrl;
    @Value("${kakao.redirect.url}")
    private String kakaoRedirectUrl;
    @Value("${kakao.client.id}")
    private String kakaoApiKey;
    @Value("${kakao.api.url}")
    private String kakaoApiUrl;
    @Value("${kakao.auth.user.me}")
    private String kakaoUserUrl;

    @Value("${spring.redis.kakao.access_token}")
    private String kakaoAccessToken;
    @Value("${spring.redis.kakao.refresh_token}")
    private String kakaoRefreshToken;

    @Value("${spring.redis.oauth-temp-info}")
    private String oauthTempInfo;

    private final RestTemplate restTemplate;
    private final RedisAdapter redisAdapter;

    private final UserService userService;

    /**
     * 카카오 OAuth 인증과정에서 받은 code를 이용해서 access_token을 요청하고, 그 결과를 KakaoToken 으로 반환하는 메서드.
     * @param code
     * @return
     */
    private KakaoToken exchangeCodeForKakaoToken(String code) {

        /* URL-encoded 포맷으로 만들어주기 위한 데이터 구조 를 위해서 MultiValueMap 를 사용한다.
         * MultiValueMap<String,Object> params = new LinkedMultiValueMap<>();
         * params.add("grant_type","authorization_code");
         * params.add("client_id",kakaoApiKey);
         * params.add("redirect_url",kakaoRedirectUrl);
         * params.add("code",code);
         *
         * => grant_type=authorization_code&client_id=...&redirect_url=...&code=...
         * */
        MultiValueMap<String,Object> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoApiKey);
        params.add("redirect_url", kakaoRedirectUrl);
        params.add("code", code);

        /* api 요청 데이터 및 헤더 세팅 */
        HttpHeaders headers = new HttpHeaders();
        /* Content-Type 의 형식을 정함. -> application/x-www-form-urlencoded
         * key-value 쌍의 데이터를 &로 연결해서 보내는 URL 인코딩 방식의 데이터 포멧팅
         * */
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        /* HTTP 요청을 보낼 때 사용할 바디와 헤더를 함께 감싸는 객체
         * 이것을 이용해서 RestTemplate이 요청을 보낼 수 있다.
         * */
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);

        log.info("kakao api request data {}",entity);

        /* Rest Template 을 이용하여 Post 요청을 보내기 위함.
         * restTemplate.postForEntity(.., entity, ..) 여기서 entity 가 현재 header + body 라고 보면된다.
         * */
        ResponseEntity<KakaoToken> response = restTemplate.postForEntity(kakaoAuthUrl, entity, KakaoToken.class);
        log.info("kakao api response data {}",response);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
            return response.getBody();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"kakao token data is empty");
        }
    }

    /**
     * 유저의 카카오 기본정보를 반환해주는 함수
     * 어쎄스 토큰이 만료된 경우 리프레시 토큰을 가지고 토큰을 재발급해준다.
     * @param accessToken
     * @param refreshToken
     * @return
     */
    private KakaoUserInfoRaw getKakaoUserInfoWithRetry(String accessToken, String refreshToken) {
        try {
            return getKakaoUserInfo(accessToken);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("[WARN][KakaoLoginServiceImpl->getKakaoUserInfoWithRetry] Access token expired. Trying to refresh...");

                KakaoToken newToken = refreshAccessToken(refreshToken);
                return getKakaoUserInfo(newToken.getAccessToken());
            } else {
                throw e;
            }
        }
    }

    /**
     * 카카오 리프레시 토큰을 통해서 어쎄스토큰을 재 발급해준다.
     * @param refreshToken
     * @return
     */
    private KakaoToken refreshAccessToken(String refreshToken) {

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", kakaoApiKey);
        params.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoToken> response = restTemplate.postForEntity(kakaoAuthUrl, entity, KakaoToken.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to refresh access token");
        }
    }


    /**
     * 만료되지 않은 어쎄스토큰을 사용하여 카카오 유저 정보를 반환해주는 함수
     * @param accessToken
     * @return
     * @throws Exception
     */
    private KakaoUserInfoRaw getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfoRaw> response = restTemplate.exchange(
                kakaoUserUrl,
                HttpMethod.GET,
                entity,
                KakaoUserInfoRaw.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to fetch Kakao user info");
        }

    }

    private OauthUserSimpleInfoDto extractFromUserInfoRaw(KakaoUserInfoRaw kakaoUserInfoRaw) {

        String userTokenId = String.valueOf(kakaoUserInfoRaw.getId());
        String nickName = (String)kakaoUserInfoRaw.getProperties().get("nickname");
        String profileImg = (String)kakaoUserInfoRaw.getProperties().get("profile_image");

        return OauthUserSimpleInfoDto.builder()
                .userTokenId(userTokenId)
                .nickName(nickName)
                .profileImg(profileImg)
                .build();
    }
    /**
     * OAuth 에서 발생된 kakaoToken 을 파싱하여 유저의 정보를 반환해주는 함수.
     * @param kakaoToken
     * @return
     * @throws Exception
     */
    private KakaoOauthInfoDto getKakaoUserTotalInfos(KakaoToken kakaoToken) {
        /*
         * OAuth 인증과정에서 받은 code를 이용해서 access_token을 요청하고, 그 결과를 KakaoToken 으로 반환받는다.
         */
        String idToken = kakaoToken.getIdToken();

        /* Kakao Payload 정보 */
        Map<String, Object> payload = decodeJwtPayload(idToken);

        OauthUserSimpleInfoDto oauthUserSimpleInfoDto = OauthUserSimpleInfoDto.builder()
                .nickName((String)payload.get("nickname"))
                .profileImg((String)payload.get("picture"))
                .userTokenId((String)payload.get("sub"))
                .build();

        /* 유저의 Kakao Oauth 전반적인 정보 */
        return KakaoOauthInfoDto.builder()
                .oauthUserSimpleInfoDto(oauthUserSimpleInfoDto)
                .kakaoToken(kakaoToken)
                .build();
    }

    public OauthChannelStatus getOauthChannel() {
        return OauthChannelStatus.kakao;
    }

    public OauthUserSimpleInfoDto getUserInfosByOauth(Long userNo) {
        /* Redis 에서 유저의 Kakao accessToken, refreshToken 을 가져와준다. */
        String accessToken = (String)redisAdapter.get(String.format("%s:%s",  kakaoAccessToken, String.valueOf(userNo)));
        String refreshToken = (String)redisAdapter.get(String.format("%s:%s",  kakaoRefreshToken, String.valueOf(userNo)));

        KakaoUserInfoRaw kakaoUserInfoRaw = getKakaoUserInfoWithRetry(accessToken, refreshToken);

        return extractFromUserInfoRaw(kakaoUserInfoRaw);
    }

    public UserOAuthResponse getOauthLoginInfo(String code) {
        KakaoToken kakaoToken = exchangeCodeForKakaoToken(code);
        KakaoOauthInfoDto kakaoOauthInfoDto = getKakaoUserTotalInfos(kakaoToken);
        String tokenId = kakaoOauthInfoDto.getOauthUserSimpleInfoDto().getUserTokenId();

        return userService.findUserOauthKakaoTblByTokenId(tokenId)
                .map(kakaoInfo -> {
                    Optional<UserTbl> userTblOpt = userService.findUserByUserNo(kakaoInfo.getUserNo());

                    return UserOAuthResponse.builder()
                            .loginChannel(OauthChannelStatus.kakao)
                            .kakaoOauthInfoDto(kakaoOauthInfoDto)
                            .userTbl(userTblOpt.orElse(null))
                            .publicStatus(userTblOpt.isPresent() ? PublicStatus.SUCCESS : PublicStatus.JOIN_TARGET_MEMBER)
                            .build();
                })
                .orElseGet(() -> UserOAuthResponse.builder()
                        .loginChannel(OauthChannelStatus.kakao)
                        .kakaoOauthInfoDto(kakaoOauthInfoDto)
                        .userTbl(null)
                        .publicStatus(PublicStatus.JOIN_TARGET_MEMBER)
                        .build()
                );
    }

    public void saveOauthTokenToRedis(Long userNo, UserOAuthResponse response) {
        String accessKey = String.format("%s:%s", kakaoAccessToken, userNo);
        String refreshKey = String.format("%s:%s", kakaoRefreshToken, userNo);

        redisAdapter.set(accessKey, response.getKakaoOauthInfoDto().getKakaoToken().getAccessToken());
        redisAdapter.set(refreshKey, response.getKakaoOauthInfoDto().getKakaoToken().getRefreshToken());
    }

    public void inputUserOauth(UserTbl userTbl, String idToken) {
        UserOauthKakaoTbl oauthTbl = UserOauthKakaoTbl.builder()
                .user(userTbl)
                .userNo(userTbl.getUserNo())
                .tokenId(idToken)
                .oauthBannedYn('N')
                .reg_dt(new Date())
                .reg_id("system")
                .build();

        userService.inputUserOauthKakaoTbl(oauthTbl);
    }

    public boolean isUserAuthChannelNotExists(Long userNo) {
        Optional<UserOauthKakaoTbl> oauthTbl = userService.findUserOauthKakaoTblByUserNo(userNo);
        return oauthTbl.isEmpty();
    }
}