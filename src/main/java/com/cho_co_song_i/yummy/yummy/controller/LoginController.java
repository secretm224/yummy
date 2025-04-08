package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.ErrorResponse;
import com.cho_co_song_i.yummy.yummy.dto.UserProfileDto;
import com.cho_co_song_i.yummy.yummy.service.*;


import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    private final LoginService kakoLoginService;
    private final LoginService naverLoginService;
    private final LoginService googleLoginService;
    private final YummyLoginService yummyLoginService;

    public LoginController(KakaoLoginServiceImpl kakoLoginService, NaverLoginServiceImpl naverLoginService,
                           GoogleLoginServiceImpl googleLoginService, YummyLoginService yummyLoginService){
        this.kakoLoginService = kakoLoginService;
        this.naverLoginService = naverLoginService;
        this.googleLoginService = googleLoginService;
        this.yummyLoginService = yummyLoginService;
    }

    @GetMapping
    public String Login(Model model) {
        model.addAttribute("title", "로그인페이지");
        return "login";
    }

    @GetMapping("/hashtest")
    public ResponseEntity<?> HashTest(
            @RequestParam(value = "text", required = false) String text
    ) {

        try {

            String saltValue = HashUtil.generateSalt();
            String hashVal = HashUtil.hashWithSalt(text, saltValue);

            return ResponseEntity.ok(hashVal);

        } catch(Exception e) {
            return ResponseEntity.ok(false);
        }

    }

    // TODO: 4/5/25
    @PostMapping("/auth/loginCheck")
    @ResponseBody
    public ResponseEntity<?> LoginCheck(HttpServletResponse res , HttpServletRequest req) {

        /* 로그인 체크 처리 */
        Optional<UserProfileDto> result = yummyLoginService.checkLoginUser(res, req);

        if (result.isEmpty()) {
            /* 로그인 안 된 경우 → 401 Unauthorized + 에러 메시지 */
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("AUTH_ERROR", "Your login information is invalid."));
        }

        return ResponseEntity.ok(result.get());
    }


    // TODO: 4/5/25
    @PostMapping("/auth/callback")
    @ResponseBody
    public ResponseEntity<?> OAuthLogin(HttpServletResponse res , HttpServletRequest req) {
        //@RequestBody LoginDto loginDto,

        return ResponseEntity.ok(true);
        
//        if (loginDto == null || loginDto.getOauthType() == null) {
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body(new ErrorResponse("LOGINDTO_EMPTY", "Login information is null."));
//        } else {
//
//            if (loginDto.getOauthType().equals("kakao")) {
//
//            } else if (loginDto.getOauthType().equals("naver")) {
//
//            } else if (loginDto.getOauthType().equals("google")) {
//
//            }
//
//            /* 프로젝트에서 지원하지 않는 Oauth 를 사용한 경우 */
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body(new ErrorResponse("OAUTH_UNSUPPORTED", "Unsupported OAuth type."));
//        }
    }
    

//    @PostMapping("/kakao/callback")
//    @ResponseBody
//    public ResponseEntity<UserOAuthInfoDto> KaKaoCode(@RequestBody LoginDto loginDto, HttpServletResponse res , HttpServletRequest req){
//        return ResponseEntity.ok(kakoLoginService.handleOAuthLogin(loginDto.getCode(), res));
//    }

//     @PostMapping("/kakao/GetUserInfoByToken")
//     public ResponseEntity<?> GetUserinfoByToken(@RequestBody String access_token ,
//                                                 @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
//                                                 @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
//                                                 HttpServletResponse res)
//     {
//         Map<String,Object> responseBody = new HashMap<>();
//         if(access_token.isEmpty() && !accessTokenCookie.isEmpty()){
//             access_token = accessTokenCookie;
//         }
//
//         boolean is_access_token = loginService.CheckKakaoTokens(access_token);
//         if(is_access_token){
//             JsonNode user_info = loginService.GetKaKaoUser(access_token);
//             if(user_info != null){
//                   String nick_name = user_info.get("nickname").asText();
//                   String picture = user_info.get("picture").asText();
////                 ObjectMapper om = new ObjectMapper();
//                 //responseBody = om.convertValue(user_info, new TypeReference<Map<String, Object>>() {});
//                 responseBody.put("access_token",access_token);
//                 responseBody.put("nickname",nick_name);
//                 responseBody.put("picture",picture);
//
//             }
//         }else{
////             reflesh token
//             JsonNode token_info = loginService.GetAccessTokenByRefreshToken(refreshTokenCookie);
//             if(token_info != null){
//                 String n_access_token = token_info.get("access_token").asText();
//                 String n_refresh_token = token_info.get("refresh_token").asText();
//                 String n_id_token = token_info.get("id_token").asText();
//
//                 if(!n_access_token.isEmpty()){
//                     Cookie accessCookie  = new Cookie("accessToken",n_access_token);
//                     accessCookie.setHttpOnly(true);
//                     accessCookie.setSecure(false);
//                     accessCookie.setPath("/");
//                     res.addCookie(accessCookie);
//                 }
//
//                 if(!n_refresh_token.isEmpty()){
//                     Cookie refreshCookie = new Cookie("refreshToken",n_refresh_token);
//                     refreshCookie.setHttpOnly(true);
//                     refreshCookie.setSecure(false);
//                     refreshCookie.setPath("/");
//                     res.addCookie(refreshCookie);
//                 }
//
//                 if(!n_id_token.isEmpty()){
//                     DecodedJWT _decodejwt = JWT.decode(n_id_token);
//                     Map<String,Object> payload = _decodejwt.getClaims().entrySet().stream()
//                                                  .collect(Collectors.toMap(Map.Entry::getKey, e->e.getValue().as(Object.class)));
//
//                     if(payload != null)
//                     {
//                        String nick_name = payload.get("nickname").toString();
//                        String picture = payload.get("picture").toString();
//
//                         responseBody.put("access_token",access_token);
//                         responseBody.put("nickname",nick_name);
//                         responseBody.put("picture",picture);
//                     }
//                 }
//             }
//         }
//
//         return ResponseEntity.ok(responseBody);
//     }
//
//    @GetMapping("/userinfo")
//    @ResponseBody
//    public ResponseEntity<List<UserProfileDto>> getUserDetailInfo(
//            @RequestParam("loginChannel") String loginChannel,
//            @RequestParam("tokenId") String tokenId) {
//
//        List<UserProfileDto> userProfile = loginService.getUserDetailInfo(loginChannel, tokenId);
//
//        if (userProfile != null) {
//            return ResponseEntity.ok(userProfile);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @GetMapping("/logOut")
//    public ResponseEntity<Boolean> logOutUser(HttpServletResponse res , HttpServletRequest req) {
//        /* Session 을 쓰지 않을 것이므로 Session 관련 삭제 로직은 생략 */
//        return ResponseEntity.ok(loginService.clearUserToken(res,req));
//    }


    /*
    **********************************************************
    ****************** Server 관련 인증 ************************
    **********************************************************
    * */

    /* 서버 토큰 발급 */

    /* 서버 토큰 확인 */


    @GetMapping("/test")
    @ResponseBody
    public String Test() {
        return "Call Method Test";
    }

    @GetMapping("/deploytest")
    @ResponseBody
    public String deployTest(){
        return "배포 테스트";
    }
}
