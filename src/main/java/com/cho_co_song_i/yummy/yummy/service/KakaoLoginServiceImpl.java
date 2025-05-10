package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.OauthLoginDto;
import com.cho_co_song_i.yummy.yummy.dto.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.entity.UserAuthTbl;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static com.cho_co_song_i.yummy.yummy.utils.JwtUtil.decodeJwtPayload;



@Service
@Slf4j
public class KakaoLoginServiceImpl implements LoginService {

    @Value("${kakao.auth.url}")
    private String kakaoAuthUrl;
    @Value("${kakao.redirect.url}")
    private String kakaoRedirectUrl;
    @Value("${kakao.client.id}")
    private String kakaoApiKey;
    @Value("${kakao.api.url}")

    private String kakaoApiUrl;
    @Value("${spring.redis.kakao.access_token}")
    private String kakaoAccessKeyPrefix;
    @Value("${spring.redis.kakao.user_info}")
    private String kakaoUserInfoPrefix;
    @Value("${spring.redis.login.user_info}")
    private String userInfoKey;

    private final RestTemplate restTemplate;
    private final RedisService redisService;
    private final UserService userService;
    private final JwtProviderService jwtProviderService;
    private final EventProducerService eventProducerService;
    private final YummyLoginServiceImpl yummyLoginServiceImpl;


    public KakaoLoginServiceImpl(RestTemplate restTemplate,
                                 RedisService redisService, JwtProviderService jwtProviderService,
                                 UserService userService, EventProducerService eventProducerService,
                                 YummyLoginServiceImpl yummyLoginServiceImpl) {
        this.restTemplate = restTemplate;
        this.redisService = redisService;
        this.jwtProviderService = jwtProviderService;
        this.userService = userService;
        this.eventProducerService = eventProducerService;
        this.yummyLoginServiceImpl = yummyLoginServiceImpl;
    }

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


    /**
     * 유저의 Kakao Oauth에 대한 정보를 반환해주는 함수
     * @param code
     * @return
     * @throws Exception
     */
    private UserOAuthResponse getOauthLoginInfo(String code) throws Exception {
        /* User 정보 */
        UserOAuthInfoDto userKakaoInfo = exchangeCodeForKakaoUser(code);
        String userToken = userKakaoInfo.getUserTokenId();
        UserAuthTbl userAuth = yummyLoginServiceImpl.getUserAuthTbl(userToken, OauthChannelStatus.kakao);

        if (userAuth == null) {
            /* 연동한적이 없거나, 가입하지 않은 경우 -> 가입유도 or 기존 아이디에 oauth2 추가 */
            return new UserOAuthResponse(PublicStatus.JOIN_TARGET_MEMBER, userToken, null);
        }

        /* 회원 정보를 Redis 에 저장해주기 위함 */
        Long userNo = userAuth.getId().getUserNo();

        /* 유저의 기본정보 */
        UserBasicInfoDto userBasicInfo = userService.getUserInfos(userNo, userKakaoInfo);

        /* Redis 에 유저 정보 저장 */
        String basicUserInfo = String.format("%s:%s", userInfoKey, userNo);
        redisService.set(basicUserInfo, userBasicInfo);

        /* 연동 이력이 존재하는 경우 -> jwt 토큰 발급 */
        return new UserOAuthResponse(PublicStatus.SUCCESS, userToken, userAuth.getUser().getUserNo());
    }

    /**
     * OAuth 에서 발생된 code 를 파싱하여 유저의 정보를 반환해주는 함수
     * @param code
     * @return
     * @throws Exception
     */
    private UserOAuthInfoDto exchangeCodeForKakaoUser(String code) throws Exception {
        /*
         * OAuth 인증과정에서 받은 code를 이용해서 access_token을 요청하고, 그 결과를 KakaoToken 으로 반환받는다.
         */
        KakaoToken kakaoToken = exchangeCodeForKakaoToken(code);
        String idToken = kakaoToken.getId_token();

        /* Kakao Payload 정보 */
        Map<String, Object> payload = decodeJwtPayload(idToken);

        /* User 정보 */
        return extractKakaoUserInfo(payload);
    }

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
     * 회원이 oauth2 를 통해 기존아이디 통합 또는 회원가입을 위해 임시 jwt 쿠키를 발급해준다.
     * @param idToken
     * @param res
     */
    @Override
    public void generateTempOauthJwtCookie(String idToken, HttpServletResponse res) {
        String jwtToken = jwtProviderService.generateOauthTempToken(idToken, String.valueOf(OauthChannelStatus.kakao));
        CookieUtil.addCookie(res, "yummy-oauth-token", jwtToken, 300);
    }

    @Override
    public PublicStatus handleOAuthLogin(OauthLoginDto loginDto, HttpServletResponse res, HttpServletRequest req) throws Exception {

        /* 로그인 시도 기록 */
        eventProducerService.produceLoginAttemptEvent(req);

        /* 유저의 Kakao Oauth에 대한 정보 */
        UserOAuthResponse userOAuthResponse = getOauthLoginInfo(loginDto.getCode());

        if (userOAuthResponse.getPublicStatus() == PublicStatus.SUCCESS) {
            /* Oauth2 인증 성공해서 유저 정보가 있는 경우 */
            return yummyLoginServiceImpl.oauthLogin(userOAuthResponse.getUserNum(), res);
        } else if (userOAuthResponse.getPublicStatus() == PublicStatus.JOIN_TARGET_MEMBER) {
            /*
             * 유저에게 신규 가입 또는 기존회원 연동 하게 시킴.
             * -> 임시 jwt 토큰 발급
             */
            generateTempOauthJwtCookie(userOAuthResponse.getIdToken(), res);
            return PublicStatus.JOIN_TARGET_MEMBER;
        } else {
            return PublicStatus.CASE_ERR;
        }
    }
}
