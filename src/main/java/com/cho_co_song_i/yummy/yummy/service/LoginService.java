package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;


@Service
@Slf4j
public class LoginService {

    @Value("${kakao.auth.url}")
    private String kakaoAuthUrl;

    @Value("${kakao.redirect.url}")
    private String kakaoRedirectUrl;

    @Value("${kakao.client.id}")
    private String kakaoApiKey;

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
}
