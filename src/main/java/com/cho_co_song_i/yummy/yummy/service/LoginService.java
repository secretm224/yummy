package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;


@Service
@Slf4j
public class LoginService {

    @Value("${kakao.auth.url}")
    private String kakaoAuthUrl;

    @Value("${kakao.redirect.url}")
    private String kakaoRedirectUrl;

    @Value("${kakao.client.id}")
    private String kakaoApiKey;

    @Value("${kakao.api.url}")
    private String kakaoApiUrl;

    private final RestTemplate restTemplate;

    public LoginService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public KakaoToken GetKakaoToken(String code){
        if(!code.isEmpty()){
            try{
                MultiValueMap<String,Object> params = new LinkedMultiValueMap<>();
                params.add("grant_type","authorization_code");
                params.add("client_id",kakaoApiKey);
                params.add("redirect_url",kakaoRedirectUrl);
                params.add("code",code);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params,headers);
                //api 요청 데이터 및 헤더 세팅
                log.info("kakao api request data",entity);
                ResponseEntity<KakaoToken> response = restTemplate.postForEntity(kakaoAuthUrl,entity,KakaoToken.class);
                log.info("kakao api response data",response);

                if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
                    return response.getBody();
                }else{
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"kakao token data is empty");
                }

            }catch (Exception e){
                log.error("kakao get token error",e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error during Kakao Token retrieval",e);
            }
        }

        return null;
    }

    public boolean CheckKakaoTokens(String CheckToken){
        boolean is_token = false;
        try{
            String url=kakaoApiUrl+"/v1/user/access_token_info";

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization","Bearer "+CheckToken);

            HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(url,HttpMethod.GET,requestEntity, JsonNode.class);

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
                is_token = true;
            }

        }catch (Exception e){
            is_token = false;
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
            try{

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
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return token_obj;
    }


//QueryDSL
//    async GetUserDetailInfo(login_channel:string,token_id:string):Promise<UserProfileDto | null>{
//        if(!!login_channel && !!token_id){
//            const auth_info = await this.auth_repository.findOneBy({
//                    login_channel:login_channel,
//                    token_id:token_id
//                                    });
//            if(auth_info){
//                const detail_info = await this.detail_repository.findOneBy({user_no:auth_info.user_no});
//                if(detail_info){
//                    const user_info = await this.user_repository.findOneBy({user_no:detail_info.user_no});
//                    if(user_info){
//                        const user_profile:UserProfileDto={
//                                user_no:user_info.user_no,
//                                user_nm:user_info.user_nm,
//                                login_channel:auth_info.login_channel,
//                                token_id:auth_info.token_id,
//                                addr_type:detail_info.addr_type,
//                                addr:detail_info.addr,
//                                lngx:detail_info.lng_x,
//                                laty:detail_info.lat_y,
//                                reg_dt:user_info.reg_dt,
//                        }
//
//                        return user_profile;
//                    }
//                }
//            }
//        }
//
//        return null;
//    }





}
