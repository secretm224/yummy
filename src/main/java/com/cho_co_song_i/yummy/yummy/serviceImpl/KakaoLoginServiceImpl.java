package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.UserAuthTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserPictureTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserPictureTblId;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import com.cho_co_song_i.yummy.yummy.repository.UserPictureRepository;
import com.cho_co_song_i.yummy.yummy.repository.UserRepository;
import com.cho_co_song_i.yummy.yummy.service.JwtProviderService;
import com.cho_co_song_i.yummy.yummy.service.LoginService;
import com.cho_co_song_i.yummy.yummy.service.UserService;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private String kakaoAccessKeyPrefix;
    @Value("${spring.redis.kakao.user_info}")
    private String kakaoUserInfoPrefix;
    @Value("${spring.redis.login.user_info}")
    private String userInfoKey;
    @Value("${spring.redis.oauth-temp-info}")
    private String oauthTempInfo;


    private final RestTemplate restTemplate;
    private final RedisAdapter redisAdapter;
    private final UserService userService;
    private final JwtProviderService jwtProviderService;
    private final EventProducerServiceImpl eventProducerServiceImpl;
    private final YummyLoginServiceImpl yummyLoginServiceImpl;
    private final UserPictureRepository userPictureRepository;
    private final UserRepository userRepository;

    /**
     * 카카오 OAuth 인증과정에서 받은 code를 이용해서 access_token을 요청하고, 그 결과를 KakaoToken 으로 반환하는 메서드.
     * @param code
     * @return
     */
    private KakaoToken exchangeCodeForKakaoToken(String code) throws Exception {

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

    // 유저의 정보를 가져와주는데 리프레시 토큰을 사용해서 어쎄스 토큰을 다시 발급해준다.
    public KakaoUserInfo getKakaoUserInfoWithRetry(String accessToken, String refreshToken) throws Exception {
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



    private KakaoUserInfo getKakaoUserInfo(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                kakaoUserUrl,
                HttpMethod.GET,
                entity,
                KakaoUserInfo.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to fetch Kakao user info");
        }

    }


    /**
     * 유저의 Kakao Oauth에 대한 정보를 반환해주는 함수
     * @param code
     * @return
     * @throws Exception
     */
    private UserOAuthResponse getOauthLoginInfo(String code) throws Exception {
        KakaoToken kakaoToken = exchangeCodeForKakaoToken(code);
        UserOAuthInfoDto userOAuthInfoDto = exchangeCodeForKakaoUser(kakaoToken); /* User 정보 */
        UserAuthTbl userAuth = yummyLoginServiceImpl.getUserAuthTbl(userOAuthInfoDto.getUserTokenId(), OauthChannelStatus.kakao);

        if (userAuth == null) {
            /* 연동한적이 없거나, 가입하지 않은 경우 -> 가입유도 or 기존 아이디에 oauth2 추가 */
            return new UserOAuthResponse(
                    PublicStatus.JOIN_TARGET_MEMBER,
                    kakaoToken,
                    userOAuthInfoDto,
                    OauthChannelStatus.kakao,
                    null);
        }

        /* 연동 이력이 존재하는 경우 */
        Long userNo = userAuth.getId().getUserNo();

        UserTbl userTbl = userRepository.findById(userNo)
                .orElseThrow(()-> new Exception(
                        String.format(
                                "[Error][UserService->getUserInfoAndModifyUserPic] This user does not exist. userNo: %d",
                                userNo)
                ));

        return new UserOAuthResponse(
                PublicStatus.SUCCESS,
                kakaoToken,
                userOAuthInfoDto,
                OauthChannelStatus.kakao,
                userTbl);
    }

    /**
     * OAuth 에서 발생된 kakaoToken 을 파싱하여 유저의 정보를 반환해주는 함수.
     * @param kakaoToken
     * @return
     * @throws Exception
     */
    private UserOAuthInfoDto exchangeCodeForKakaoUser(KakaoToken kakaoToken) throws Exception {
        /*
         * OAuth 인증과정에서 받은 code를 이용해서 access_token을 요청하고, 그 결과를 KakaoToken 으로 반환받는다.
         */
        String idToken = kakaoToken.getIdToken();

        /* Kakao Payload 정보 */
        Map<String, Object> payload = decodeJwtPayload(idToken);

        /* User 정보 */
        return extractKakaoUserInfo(payload);
    }

//    /**
//     * OAuth 에서 발생된 code 를 파싱하여 유저의 정보를 반환해주는 함수
//     * @param code
//     * @return
//     * @throws Exception
//     */
//    private UserOAuthInfoDto exchangeCodeForKakaoUser(String code) throws Exception {
//        /*
//         * OAuth 인증과정에서 받은 code를 이용해서 access_token을 요청하고, 그 결과를 KakaoToken 으로 반환받는다.
//         */
//        KakaoToken kakaoToken = exchangeCodeForKakaoToken(code);
//        String idToken = kakaoToken.getIdToken();
//
//        /* Kakao Payload 정보 */
//        Map<String, Object> payload = decodeJwtPayload(idToken);
//
//        /* User 정보 */
//        return extractKakaoUserInfo(payload);
//    }

    /**
     * 카카오 토큰에서 유저의 정보를 파싱해주는 함수
     * @param payload
     * @return
     */
    private UserOAuthInfoDto extractKakaoUserInfo(Map<String, Object> payload) {
        String userTokenId = (String)payload.get("sub");
        String nickName = (String)payload.get("nickname");
        String userPicture = (String)payload.get("picture");

        return new UserOAuthInfoDto(userTokenId, nickName, userPicture);
    }

    /**
     * 유저의 프로필 사진 정보를 업데이트 해주는 함수 -> 기존에 연동만 되고 없을 수 있으니 insert 도 추가해야 함
     * @param userTbl
     * @param picUrl
     * @throws Exception
     */
    private void modifyUserPictureTbl(UserTbl userTbl, String picUrl) {
        String channel = OauthChannelStatus.kakao.toString();
        UserPictureTblId userPictureTblId = new UserPictureTblId(userTbl.getUserNo(), channel);

        UserPictureTbl userPictureTbl = userPictureRepository.findById(userPictureTblId)
                .orElseGet(() -> {
                    UserPictureTbl newEntry = new UserPictureTbl();
                    newEntry.setId(userPictureTblId);
                    newEntry.setRegDt(new Date());
                    newEntry.setRegId("system");
                    newEntry.setUser(userTbl);
                    return newEntry;
                });

        Date now = new Date();
        userPictureTbl.setPicUrl(picUrl);
        userPictureTbl.setActiveYn('Y');

        if (userPictureTbl.getRegDt() != null) {
            userPictureTbl.setChgDt(now);
            userPictureTbl.setChgId("system");
        }

        userPictureRepository.save(userPictureTbl);
    }


    /**
     * 회원이 oauth2 를 통해 기존아이디 통합 또는 회원가입을 위해 임시 jwt 쿠키를 발급해준다.
     * @param idToken
     * @param res
     */
    @Override
    public void generateTempOauthJwtCookie(String idToken, HttpServletResponse res) {
        String jwtToken = jwtProviderService.generateOauthTempToken(idToken, String.valueOf(OauthChannelStatus.kakao));
        CookieUtil.addCookie(res, "yummy-oauth-token", jwtToken, 3600);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PublicStatus handleOAuthLogin(OauthLoginDto loginDto, HttpServletResponse res, HttpServletRequest req) throws Exception {

        /* 로그인 시도 기록 */
        eventProducerServiceImpl.produceLoginAttemptEvent(req);

        /* 유저의 Kakao Oauth에 대한 정보 */
        UserOAuthResponse userOAuthResponse = getOauthLoginInfo(loginDto.getCode());

        if (userOAuthResponse.getPublicStatus() == PublicStatus.SUCCESS) {
            /* Oauth2 인증 성공해서 유저 정보가 있는 경우 */
            /* 여기서 프로필 사진을 업데이트 시켜준다. -> 이미 연동한 유저이니까. */
//            modifyUserPictureTbl(
//                    userOAuthResponse.getUserTbl(),
//                    userOAuthResponse.getUserOAuthInfoDto().getUserPicture());
            
            return yummyLoginServiceImpl.processOauthLogin(userOAuthResponse, res);
        } else if (userOAuthResponse.getPublicStatus() == PublicStatus.JOIN_TARGET_MEMBER) {
            /*
             * 유저에게 신규 가입 또는 기존회원 연동 하게 시킴.
             * -> 임시 jwt 토큰 발급
             */
            generateTempOauthJwtCookie(userOAuthResponse.getUserOAuthInfoDto().getUserTokenId(), res);

            /* Redis에 Kakao 회원정보 임시저장 -> 토큰 아이디, 프로필 사진, 닉네임 등 임시적으로 저장해준다. */
            redisAdapter.set(
                    String.format("%s:%s", oauthTempInfo, userOAuthResponse.getUserOAuthInfoDto().getUserTokenId()),
                    userOAuthResponse.getUserOAuthInfoDto()
            );

            return PublicStatus.JOIN_TARGET_MEMBER;
        } else {
            return PublicStatus.CASE_ERR;
        }
    }
}