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

import java.util.Arrays;
import java.util.HashMap;
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
            headers.add("Authorization","Bearer"+CheckToken);

            HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestEntity, JsonNode.class);

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

            Map<String, Object> requestBody = new HashMap<>();
            List<String> propertyKeys = Arrays.asList(
                    "kakao_account.profile.nickname",
                    "kakao_account.profile.thumbnail_image"
            );
            requestBody.put("property_keys", propertyKeys);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

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

        }catch(Exception e){
            e.printStackTrace();
        }
        return user_obj;
    }



//    async GetKakaoUserInfo(access_token:string){
//        if(!access_token){
//            throw new HttpException('accesss tokens is empty',HttpStatus.BAD_REQUEST);
//        }
//
//        try {
//            const url = `${process.env.KAKAO_API_URL ?? ""}/v2/user/me`;
////            const header = {headers:{
////                'Authorization': `Bearer ${access_token}`,
////                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8'
////            }};
////
////            const data = new URLSearchParams({
////                    property_keys: '["kakao_account.profile.nickname","kakao_account.profile.thumbnail_image"]'
////            });
//
//            const userinfo = await axios.post(url, data, header);
//            const nickname = userinfo.data.kakao_account.profile.nickname;
//            const image = userinfo.data.kakao_account.profile.thumbnail_image_url;
//            const token_id = userinfo.data.id;
//
//            if(!!nickname && !!image){
//                return {nickname:nickname,picture:image,token_id:token_id};
//            }
//
//        }catch(error){
//            console.error('get userinfo by access toekn'+error);
//            console.error(error.response?.data);
//        }
//    }





}
