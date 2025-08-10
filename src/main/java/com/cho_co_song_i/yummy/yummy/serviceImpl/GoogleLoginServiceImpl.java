package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.component.JwtProvider;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthLoginDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthUserSimpleInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.dto.oauth.google.GoogleOauthInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.google.GoogleToken;
import com.cho_co_song_i.yummy.yummy.entity.UserOauthGoogleTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.LoginService;
import com.cho_co_song_i.yummy.yummy.service.UserService;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import okhttp3.FormBody;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleLoginServiceImpl implements LoginService {
    @Value("${google.client-id}")
    private String googleClientId;
    @Value("${google.client-secret}")
    private String googleClientSecret;
    @Value("${google.redirect-uri}")
    private String googleRedirectUri;
    @Value("${google.oauth.api-uri}")
    private String googleOauthApiUri;
    @Value("${google.oauth.api-uri-user}")
    private String googleOauthApiUriUser;
    @Value("${spring.redis.google.access_token}")
    private String googleAccessToken;
    @Value("${spring.redis.google.refresh_token}")
    private String googleRefreshToken;

    private final RestTemplate restTemplate;
    private final UserService userService;
    private final RedisAdapter redisAdapter;

    private final OkHttpClient client;

    private final ObjectMapper mapper;

    /**
     * Google api 를 통해서 현재 접속하려는 유저의 정보를 요청해준다.
     * @param code
     * @return
     */
    private GoogleToken exchangeCodeForGoogleToken(String code) {

        /* Google에 토큰 정보 요청 */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<?> tokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<GoogleToken> response = restTemplate.postForEntity(
                googleOauthApiUri,
                tokenRequest,
                GoogleToken.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "google token data is empty");
        }
    }

    /**
     * OAuth 에서 발생된 google token 을 파싱하여 유저의 정보를 반환해주는 함수.
     * @param googleToken
     * @return
     */
    private GoogleOauthInfoDto getGoogleUserTotalInfos(GoogleToken googleToken) {

        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(googleToken.getAccessToken());

        HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> response = restTemplate.exchange(
                googleOauthApiUriUser,
                HttpMethod.GET,
                userInfoRequest,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Failed to get user information using Google Token.: %s", googleToken));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = response.getBody();

        String sub = (String) userInfo.get("sub"); /* google 이 사용자에게 부여하는 고유 식별자 */
        String name = (String) userInfo.get("name"); /* 구글 아이디 사용자 이름 */
        String picture = (String) userInfo.get("picture"); /* 구글 아이디 사용자 프로필 이미지 */

        OauthUserSimpleInfoDto oauthUserSimpleInfoDto = OauthUserSimpleInfoDto
                .builder()
                .userTokenId(sub)
                .nickName(name)
                .profileImg(picture)
                .build();

        return GoogleOauthInfoDto.builder()
                .googleToken(googleToken)
                .oauthUserSimpleInfoDto(oauthUserSimpleInfoDto)
                .build();
    }

    private String refreshAccessToken(String refreshToken) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("client_id", googleClientId)
                .add("client_secret", googleClientSecret)
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .build();

        Request request = new Request.Builder()
                .url(googleOauthApiUri)
                .post(formBody)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) return null;

        JsonNode json = mapper.readTree(response.body().string());
        return json.get("access_token").asText();
    }

    private JsonNode getGoogleUserInfoWithRetry(String accessToken, String refreshToken) throws IOException {
        Request request = new Request.Builder()
                .url(googleOauthApiUriUser)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        Response response = client.newCall(request).execute();

        if (response.code() == 401) {
            String newAccessToken = refreshAccessToken(refreshToken);

            if (newAccessToken == null) throw new RuntimeException("[Error][GoogleLoginServiceImpl->getGoogleUserInfoWithRetry] Failed to refresh token");

            request = new Request.Builder()
                    .url(googleOauthApiUriUser)
                    .addHeader("Authorization", "Bearer " + newAccessToken)
                    .build();

            response = client.newCall(request).execute();
        }

        if (!response.isSuccessful()) {
            throw new IOException("[Error][GoogleLoginServiceImpl->getGoogleUserInfoWithRetry] Failed to get user info: " + response.code());
        }

        return mapper.readTree(response.body().string());
    }


    public OauthChannelStatus getOauthChannel() {
        return OauthChannelStatus.google;
    }

    public UserOAuthResponse getOauthLoginInfo(String code) {

        GoogleToken googleToken = exchangeCodeForGoogleToken(code);
        GoogleOauthInfoDto googleOauthInfoDto = getGoogleUserTotalInfos(googleToken);
        String userTokenId = googleOauthInfoDto.getOauthUserSimpleInfoDto().getUserTokenId();

        return userService.findUserOauthGoogleTblByTokenId(userTokenId)
                .map(googleInfo -> {
                    Optional<UserTbl> userTblOpt = userService.findUserByUserNo(googleInfo.getUserNo());
                    return UserOAuthResponse.builder()
                            .loginChannel(OauthChannelStatus.google)
                            .googleOauthInfoDto(googleOauthInfoDto)
                            .userTbl(userTblOpt.orElse(null))
                            .publicStatus(userTblOpt.isPresent() ? PublicStatus.SUCCESS : PublicStatus.JOIN_TARGET_MEMBER)
                            .build();

                })
                .orElseGet(() -> UserOAuthResponse.builder()
                        .loginChannel(OauthChannelStatus.google)
                        .googleOauthInfoDto(googleOauthInfoDto)
                        .userTbl(null)
                        .publicStatus(PublicStatus.JOIN_TARGET_MEMBER)
                        .build()
                );

    }
    public void saveOauthTokenToRedis(Long userNo, UserOAuthResponse response) {
        String accessKey = String.format("%s:%s", googleAccessToken, userNo);
        String refreshKey = String.format("%s:%s", googleRefreshToken, userNo);

        redisAdapter.set(accessKey, response.getGoogleOauthInfoDto().getGoogleToken().getAccessToken());
        redisAdapter.set(refreshKey, response.getGoogleOauthInfoDto().getGoogleToken().getRefreshToken());
    }

    public OauthUserSimpleInfoDto getUserInfosByOauth(Long userNo) {

        /* Redis 에서 유저의 Google accessToken, refreshToken 을 가져와준다. */
        String accessToken = (String)redisAdapter.get(String.format("%s:%s",  googleAccessToken, String.valueOf(userNo)));
        String refreshToken = (String)redisAdapter.get(String.format("%s:%s",  googleRefreshToken, String.valueOf(userNo)));

        try {
            JsonNode jsonNode = getGoogleUserInfoWithRetry(accessToken, refreshToken);
        } catch(Exception e) {
            log.error("[Error][GoogleLoginServiceImpl->getUserInfosByOauth] {}", e.getMessage());
        }


        int a = 10;

        return null;
    }

    public void inputUserOauth(UserTbl userTbl, String idToken) {
        UserOauthGoogleTbl oauthTbl = new UserOauthGoogleTbl(userTbl, idToken, "inputUserOauth");
        userService.inputUserOauthGoogleTbl(oauthTbl);
    }

    public boolean isUserAuthChannelNotExists(Long userNo) {
        Optional<UserOauthGoogleTbl> oauthTbl = userService.findUserOauthGoogleTblByUserNo(userNo);
        return oauthTbl.isEmpty();
    }
}
