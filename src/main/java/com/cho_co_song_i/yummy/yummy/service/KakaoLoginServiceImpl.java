package com.cho_co_song_i.yummy.yummy.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.dto.UserProfileDto;
import com.cho_co_song_i.yummy.yummy.entity.UserAuthTbl;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cho_co_song_i.yummy.yummy.utils.CookieUtil.*;
import static com.cho_co_song_i.yummy.yummy.utils.JwtUtil.decodeJwtPayload;

import static com.cho_co_song_i.yummy.yummy.entity.QUserAuthTbl.userAuthTbl;


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

    private final RestTemplate restTemplate;
    private final JPAQueryFactory queryFactory;

    private final RedisService redisService;

    private final JwtProviderService jwtProviderService;


    public KakaoLoginServiceImpl(RestTemplate restTemplate, JPAQueryFactory queryFactory,
                                 RedisService redisService, JwtProviderService jwtProviderService) {
        this.restTemplate = restTemplate;
        this.queryFactory = queryFactory;
        this.redisService = redisService;
        this.jwtProviderService = jwtProviderService;
    }

    /**
     * Kakao 로그인 처리
     * @param code
     * @param res
     * @return
     */
    @Override
    public UserOAuthResponse handleOAuthLogin(String code, HttpServletResponse res) throws Exception {
        /*
         * OAuth 인증과정에서 받은 code를 이용해서 access_token을 요청하고, 그 결과를 KakaoToken 으로 반환받는다.
         */
        KakaoToken kakaoToken = getKakaoToken(code);

        if (kakaoToken == null) {
            /* 카카오 토큰이 유효하지 않은 경우 */
            return new UserOAuthResponse(PublicStatus.TOKEN_ERR, null, null);
        }

        //String accessToken = kakaoToken.getAccess_token();
        //String refreshToken = kakaoToken.getRefresh_token();
        String idToken = kakaoToken.getId_token();

//        if (accessToken == null || accessToken.isEmpty()) {
//            return null;
//        }

        /* Kakao Payload 정보 */
        Map<String, Object> payload = decodeJwtPayload(idToken);

        /* User 정보 */
        UserOAuthInfoDto userKakaoInfo = getKakaoUserInfo(payload);

        /* user_auth_tbl 테이블을 탐색 */
        UserAuthTbl userAuth = queryFactory
                .selectFrom(userAuthTbl)
                .where(
                        userAuthTbl.id.loginChannel.eq("kakao"),
                        userAuthTbl.id.tokenId.eq(userKakaoInfo.getUserTokenId())
                )
                .fetchFirst();

        if (userAuth == null) {
            /* 연동한적이 없거나, 가입하지 않은 경우 */
            return new UserOAuthResponse(PublicStatus.JOIN_TARGET_MEMBER, idToken, null);
        }

        /* 연동 이력이 존재하는 경우 -> jwt 토큰 발급 */
        return new UserOAuthResponse(PublicStatus.SUCCESS, idToken, userAuth.getUser().getUserNo());
    }

    /**
     * 카카오 토큰에서 유저의 정보를 빼오는 함수
     * @param payload
     * @return
     */
    private UserOAuthInfoDto getKakaoUserInfo(Map<String, Object> payload) {

        try {

            String userTokenId = (String)payload.get("sub");
            String nickName = (String)payload.get("nickname");
            String userPicture = (String)payload.get("picture");

            return new UserOAuthInfoDto(userTokenId, nickName, userPicture);
        } catch(Exception e) {
            log.error("[Error][LoginService->getKakaoUserInfo] {}",  e.getMessage(), e);
            return null;
        }
    }

    /**
     * 카카오 OAuth 인증과정에서 받은 code를 이용해서 access_token을 요청하고, 그 결과를 KakaoToken 으로 반환하는 메서드.
     * @param code
     * @return
     */
    private KakaoToken getKakaoToken(String code){

        try {
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
            params.add("grant_type","authorization_code");
            params.add("client_id",kakaoApiKey);
            params.add("redirect_url",kakaoRedirectUrl);
            params.add("code",code);

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

        } catch (Exception e){
            log.error("kakao get token error",e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error during Kakao Token retrieval",e);
        }
    }

    /**
     * 해당 토큰이 문제 없는 토큰인지 검증하는 메서드
     * @param CheckToken
     * @return
     */
    public boolean CheckKakaoTokens(String CheckToken){

        boolean is_token = false;

        try {
            String url = kakaoApiUrl+"/v1/user/access_token_info";

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization","Bearer " + CheckToken);

            HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(url,HttpMethod.GET,requestEntity, JsonNode.class);

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
                is_token = true;
            }

        } catch (Exception e){
            log.error("[Error][LoginService->CheckKakaoTokens] {}", e.getMessage(), e);
            return false;
        }

        return is_token;
    }


    public JsonNode GetKaKaoUser(String access_token){

        if(access_token.isEmpty()){
            return null;
        }

        ObjectMapper om = new ObjectMapper();
        ObjectNode user_obj = om.createObjectNode();

        user_obj.put("nickname","");
        user_obj.put("picture","");
        user_obj.put("token_id","");

        try{
            String url = kakaoApiUrl+"/v2/user/me";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("Authorization","Bearer "+access_token);

//            const data = new URLSearchParams({
//                    property_keys: '["kakao_account.profile.nickname","kakao_account.profile.thumbnail_image"]'
//            });

            MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
            // JSON 배열 문자열로 property_keys 값을 설정
            String propertyKeysJson = "[\"kakao_account.profile.nickname\",\"kakao_account.profile.thumbnail_image\"]";
            requestBody.add("property_keys", propertyKeysJson);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestEntity, JsonNode.class);
            JsonNode json = response.getBody();

            String nickName =  json.path("kakao_account")
                                    .path("profile")
                                    .path("nickname")
                                    .asText();

            String image = json.path("kakao_account")
                                .path("profile")
                                .path("thumbnail_image_url")
                                .asText();
            long tokenId = json.path("id").asLong();

            user_obj.put("nickname", nickName);
            user_obj.put("picture", image);
            user_obj.put("token_id", tokenId);
//test
        }catch(Exception e){
            e.printStackTrace();
        }
        return user_obj;
    }

    public JsonNode GetAccessTokenByRefreshToken(String refresh_token){

        ObjectMapper om = new ObjectMapper();
        ObjectNode token_obj = om.createObjectNode();

        if(!refresh_token.isEmpty()){
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                Map<String,Object> request_body = new HashMap<>();
                request_body.put("grant_type","refresh_token");
                request_body.put("client_id",kakaoApiKey);
                request_body.put("refresh_token",refresh_token);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request_body, headers);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<JsonNode> response = restTemplate.postForEntity(kakaoAuthUrl, requestEntity, JsonNode.class);
                JsonNode json = response.getBody();

                if(json != null){
                    String n_access_token = json.get("access_token").asText();
                    String n_refresh_token = json.get("refresh_token").asText();
                    String n_id_token = json.get("id_token").asText();

                    token_obj.put("access_token",n_access_token);
                    token_obj.put("refresh_token",n_refresh_token);
                    token_obj.put("id_token",n_id_token);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        return token_obj;
    }

//    public List<UserProfileDto> getUserDetailInfo(String loginChannel, String tokenId) {
//        return userCustomRepository.GetUserInfo(loginChannel, tokenId);
//    }

    /**
     * 토큰을 쿠키, Redis 모두에서 삭제해주는 함수
     * @param res
     * @param req
     * @param tokenKeyName
     * @return
     */
    private Boolean removeTokenAndCookie(HttpServletResponse res , HttpServletRequest req, String tokenKeyName) {
        /* 쿠키에서 토큰 키 추출 */
        String tokenKey = getCookieValue(req, tokenKeyName);

        if (tokenKey != null) {
            clearCookie(res, tokenKeyName);
            Boolean result = redisService.deleteKey(tokenKey);

            if (!result) {
                log.error("[Error][LoginService->removeTokenAndCookie] Failed to delete {}.", tokenKeyName);
                return false;
            }
        }

        return true;
    }

    /**
     * 유저 로그인 관련 토큰을 삭제해주는 함수
     * @param res
     * @param req
     * @return
     */
    public Boolean clearUserToken(HttpServletResponse res , HttpServletRequest req) {
        Boolean accessRes = removeTokenAndCookie(res, req, "accessTokenKey");
        Boolean refreshRes = removeTokenAndCookie(res, req, "refreshTokenKey");

        return accessRes && refreshRes;
    }


//    public Boolean validateServer(String access_token) {
//
//        /* 해당 access 토큰이 유효한지 검증 */
//        try {
//
//            jwtProviderService.validateTokenAndGetSubject(access_token);
//
//        } catch (ExpiredJwtException e) {
//            log.warn("[Warn][LoginService->handleServerLogin] JWT token expired: {}", e.getMessage(), e);
//        } catch (UnsupportedJwtException e) {
//            log.error("[Error][LoginService->handleServerLogin] JWT format not supported: {}", e.getMessage(), e);
//            return false;
//        } catch (MalformedJwtException e) {
//            log.error("[Error][LoginService->handleServerLogin] Invalid JWT format: {}", e.getMessage(), e);
//        } catch (SecurityException e) {
//            log.error("[Error][LoginService->handleServerLogin] Signature Verification Failed: {}", e.getMessage(), e);
//        } catch (IllegalArgumentException e) {
//            log.error("[Error][LoginService->handleServerLogin] Token is empty or null: {}", e.getMessage(), e);
//        } catch (Exception e) {
//            log.error("[Error][LoginService->handleServerLogin] {}",e.getMessage(), e);
//        }
//
//
//    }

}
