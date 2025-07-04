package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.component.JwtProvider;
import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.JoinMemberIdStatus;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.*;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import com.cho_co_song_i.yummy.yummy.utils.PasswdUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.cho_co_song_i.yummy.yummy.entity.QUserTbl.userTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserEmailTbl.userEmailTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserPhoneNumberTbl.userPhoneNumberTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserTokenIdTbl.userTokenIdTbl;

@Service
@Slf4j
@RequiredArgsConstructor
public class JoinMemberServiceImpl implements JoinMamberService {
    private final JPAQueryFactory queryFactory;
    private final LoginServiceFactory loginServiceFactory;

    private final UserService userService;
    private final EventProducerService eventProducerService;
    private final YummyLoginService yummyLoginService;

    private final RedisAdapter redisAdapter;
    private final JwtProvider jwtProvider;

    @PersistenceContext
    private final EntityManager entityManager;

    @Value("${spring.redis.refresh-key-prefix}")
    private String refreshKeyPrefix;

    @Value("${spring.redis.join-email-code}")
    private String redisJoinEmailCode;

    @Value("${spring.redis.email-verified}")
    private String redisJoinEmailVerifiedYn;

    @Value("${spring.redis.oauth-temp-info}")
    private String oauthTempInfo;

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus connectExistUser(StandardLoginDto standardLoginDto, HttpServletResponse res, HttpServletRequest req) throws Exception {
        return PublicStatus.SUCCESS;
    }

    /**
     * 입력된 아이디가 DB 유저 테이블에 존재하는지 확인해주는 함수
     * @param userId
     * @return
     */
    private JoinMemberIdStatus checkUserId(String userId) {

        if (!isValidUserIdFormat(userId)) {
            return JoinMemberIdStatus.NONFORMAT;
        }

        var queryCount = queryFactory
                .selectOne()
                .from(userTbl)
                .where(userTbl.userId.eq(userId))
                .fetchFirst();

        if (queryCount == null) {
            return JoinMemberIdStatus.SUCCESS;
        } else {
            return JoinMemberIdStatus.DUPLICATED;
        }
    }


    /**
     * 아이디 찾기를 위해서 유저정보를 디비에서 가져와주는 함수
     * @param findIdDto
     * @return
     */
    private String findUserIdByFindId(FindIdDto findIdDto) {
        return queryFactory
                .select(userTbl.userId)
                .from(userTbl)
                .join(userEmailTbl).on(userEmailTbl.user.eq(userTbl))
                .join(userPhoneNumberTbl).on(userPhoneNumberTbl.user.eq(userTbl))
                .where(
                        userTbl.userNm.eq(findIdDto.getUserNm()),
                        userPhoneNumberTbl.id.phoneNumber.eq(findIdDto.getPhoneNumber()),
                        userPhoneNumberTbl.telecomName.eq(findIdDto.getTelecom()),
                        userEmailTbl.userEmailAddress.eq(findIdDto.getEmail())
                )
                .fetchFirst();
    }

    /**
     * DB 에서 유저정보를 가져와주는 함수
     * @param dto
     * @return
     */
    private UserTbl findUserInfoJoinUserEmailAndPhoneNumberTbl(FindPwDto dto) {

        return queryFactory
                .selectFrom(userTbl)
                .join(userEmailTbl).on(userEmailTbl.user.eq(userTbl))
                .join(userPhoneNumberTbl).on(userPhoneNumberTbl.user.eq(userTbl))
                .where(
                        userTbl.userNm.eq(dto.getUserNm()),
                        userTbl.userId.eq(dto.getUserId()),
                        userEmailTbl.userEmailAddress.eq(dto.getEmail()),
                        userPhoneNumberTbl.telecomName.eq(dto.getTelecom()),
                        userPhoneNumberTbl.id.phoneNumber.eq(dto.getPhoneNumber())
                )
                .fetchFirst();
    }

    /**
     * 특정 유저가 가지고 있는 토큰 키 모두 제거
     * @param userNo
     */
    private void deleteUserTokenIds(Long userNo) {

        List<UserTokenIdTbl> userTokens = queryFactory
                .selectFrom(userTokenIdTbl)
                .where(userTokenIdTbl.id.userNo.eq(userNo))
                .fetch();

        if (userTokens == null || userTokens.isEmpty()) {
            return;
        }

        /* 레디스에 존재하는 토큰 모두 제거 && DB 에서도 해당 정보 제거*/
        for (UserTokenIdTbl tokenTbl : userTokens) {
            Optional.ofNullable(tokenTbl.getId().getTokenId())
                    .ifPresent(token -> {
                        String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, userNo.toString(), token);
                        redisAdapter.deleteKey(refreshKey);
                        entityManager.remove(tokenTbl);
                    });
        }
    }
    /**
     * 새로운 비밀번호를 생성하고 저장해주는 함수
     * @param userTbl
     * @return
     * @throws Exception
     */
    private String issueAndSaveTempPassword(UserTbl userTbl) throws Exception {

        String tempPw = PasswdUtil.makeTempPw();
        String pwSalt = HashUtil.generateSalt();
        String hashedPw = HashUtil.hashWithSalt(tempPw, pwSalt);

        UserTempPwTbl userTempPwTbl =
                new UserTempPwTbl(userTbl.getUserNo(), userTbl.getUserId(), "issueAndSaveTempPass");

        userService.inputUserTempPwTbl(userTempPwTbl);

        userTbl.modifyUserPwAndSalt(hashedPw, pwSalt);

        return tempPw;
    }
    /**
     * 새로운 유저의 정보를 디비에 저장해준다.
     * @param joinMemberDto
     * @return
     * @throws Exception
     */
    private UserTbl createJoinUser(JoinMemberDto joinMemberDto) throws Exception {

        UserTbl user = userService.createUser(joinMemberDto);

        Long userNo = user.getUserNo();
        if (userNo == null) {
            throw new Exception("Unable to obtain userNo after saving User.");
        }

        userService.inputUserEmail(user, joinMemberDto.getEmail());
        userService.inputUserPhoneNumber(user, joinMemberDto.getPhoneNumber(), joinMemberDto.getTelecom());

        return user;
    }



    /* 에러 테스트용 */
    private void test() throws Exception {
        int a = 1 / 0;
    }

    /**
     * 유저의 아이디 포멧을 확인해주는 함수
     * @param userId
     * @return
     */
    private Boolean isValidUserIdFormat(String userId) {

        if (userId == null || userId.isEmpty()) {
            return false;
        }

        /* 특수문자가 포함되면 false */
        return userId.matches("^(?!\\d+$)[a-z0-9]+$");
    }


    /**
     * 비밀번호 검증
     * @param userPw
     * @return
     */
    private boolean isValidUserPw(String userPw) {

        if (userPw == null || userPw.length() < 8) {
            return false;
        }

        boolean hasUppercase = userPw.matches(".*[A-Z].*");
        boolean hasLowercase = userPw.matches(".*[a-z].*");
        boolean hasDigit = userPw.matches(".*[0-9].*");
        boolean hasSpecial = userPw.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        boolean onlyAllowedChars = userPw.matches("^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>]+$");

        return hasUppercase && hasLowercase && hasDigit && hasSpecial && onlyAllowedChars;
    }

    /**
     * 확인비밀번호가 비밀번호와 같은지 확인해주는 함수.
     * @param userPw
     * @param userPwCheck
     * @return
     */
    private boolean isValidUserPwCheck(String userPw, String userPwCheck) {
        return userPw.equals(userPwCheck);
    }

    /**
     * 유저가 입력한 이메일을 검증해주는 함수
     * @param email
     * @return
     */
    private PublicStatus isValidUserEmail(String email) {

        boolean isEmailFormat = isValidUserEmailFormat(email);
        if (!isEmailFormat) {
            return PublicStatus.EMAIL_FORMAT_ERR;
        }

        boolean isDupEmail = userService.isDuplicatedUserEmail(email);
        if (!isDupEmail) {
            return PublicStatus.EMIL_DUPLICATED;
        }

        return PublicStatus.SUCCESS;
    }

    /**
     * 이메일 주소 양식 검증
     * @param email
     * @return
     */
    private boolean isValidUserEmailFormat(String email) {

        if (email == null || email.isEmpty()) {
            return false;
        }

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

        return email.matches(emailRegex);
    }

    /**
     * 유저의 이름 검증
     * @param name
     * @return
     */
    private boolean isValidUserName(String name) {

        if (name == null || name.isEmpty()) {
            return false;
        }

        if (name.matches(".*\\d.*")) {
            return false;
        }

        /* 특수문자가 포함되면 false (문자만 허용) */
        return name.matches("^[\\p{L}]+$");
    }

    /**
     * 유저의 생년월일 검증
     * @param birthDate
     * @return
     */
    private boolean isValidUserBirthday(String birthDate) {

        if (birthDate == null || birthDate.isEmpty()) {
            return false;
        }

        /* 8자리 숫자가 아니면 false */
        if (!birthDate.matches("\\d{8}")) {
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        /* 생년월일 포멧이 문제가 생기면 에러를 내서 boolean 으로 답해야 하므로 try-catch 사용*/
        try {
            LocalDate.parse(birthDate, formatter);
        } catch(Exception e) {
            log.warn("[WARN][joinMemberService->isValidUserBirthday {}", e.getMessage(), e);
            return false;
        }

        return true;
    }

    /**
     * 유저의 통신사 검증
     * @param telecom
     * @return
     */
    private boolean isValidUserMobileCarrier(String telecom) {

        if (telecom == null || telecom.isEmpty()) {
            return false;
        }

        return switch (telecom.toLowerCase()) {
            case "skt", "kt", "lg", "etc" -> true;
            default -> false;
        };
    }

    /**
     * 유저의 성별 & 내국인/외국인 검증
     * @param gender
     * @return
     */
    private boolean isValidUserGender(String gender) {

        if (gender == null || gender.isEmpty()) {
            return false;
        }

        return switch (gender.toLowerCase()) {
            case "m", "f", "i", "o" -> true;
            default -> false;
        };
    }

    /**
     * 유저가 입력한 전화번호가 휴대폰 번호 양식에 맞는지 확인해준다.
     * @param phoneNumber
     * @return
     */
    private Boolean isValidUserPhoneNumberFormat(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        return phoneNumber.matches("^010\\d{8}$");
    }

    /**
     * 유저의 핸드폰번호 검증
     * @param phoneNumber
     * @return
     */
    private PublicStatus isValidUserPhoneNumber(String phoneNumber) {

        if (!isValidUserPhoneNumberFormat(phoneNumber)) {
            return PublicStatus.PHONE_ERR;
        }

        /* 핸드폰 번호가 중복되는지 확인 */
        var queryCount = queryFactory
                .selectOne()
                .from(userPhoneNumberTbl)
                .where(userPhoneNumberTbl.id.phoneNumber.eq(phoneNumber))
                .fetchFirst();

        if (queryCount == null) {
            return PublicStatus.SUCCESS;
        } else {
            return PublicStatus.PHONE_DUPLICATED;
        }
    }

    /**
     * Oauth2 연동 회원가입 유저인지 체크하고 유저의 정보를 디비에 저장해주는 함수
     * @param res
     * @param req
     * @param joinMemberDto
     * @return
     * @throws Exception
     */
    private PublicStatus validateOauthAndInputUser(HttpServletResponse res, HttpServletRequest req, JoinMemberDto joinMemberDto) throws Exception {

        /* Oauth2 와 연동할 아이디인지 체크 */
        String oauthToken = CookieUtil.getCookieValue(req, "yummy-oauth-token");

        if (oauthToken != null) {
            JwtValidationResult jwtResult = jwtProvider.validateJwtAndCleanIfInvalid("yummy-oauth-token", res, req);

            if (jwtResult.getStatus() == JwtValidationStatus.SUCCESS) {
                /* ============ JWT 검증 성공 ============ */
                /* oauth2 토큰 아이디 */
                String idToken = jwtResult.getClaims().getSubject();

                /* oauth2 채널 - 카카오,네이버,구글...등등 */
                OauthChannelStatus oauthChannel = OauthChannelStatus.valueOf(
                        jwtResult.getClaims().get("oauthChannel", String.class));

                /* user 정보부터 저장 */
                UserTbl joinUser = createJoinUser(joinMemberDto);

                /* Oauth 채널별 로그인 서비스 */
                LoginService loginService = loginServiceFactory.getService(oauthChannel);

                /* oauth2 정보도 저장 */
                loginService.inputUserOauth(joinUser, idToken);

                CookieUtil.clearCookie(res, "yummy-oauth-token");

                /* user_tbl에 main_oauth_channel 로 입력 -> 향후에 수정해야 할 듯. */
                userService.modifyUserTblMainOauthChannel(oauthChannel, joinUser);

            } else {
                /* JWT 검증 실패 */
                return PublicStatus.REJOIN_CHECK;
            }

        } else {
            createJoinUser(joinMemberDto);
        }

        return PublicStatus.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus changePasswd(HttpServletResponse res, HttpServletRequest req, ChangePwDto changePwDto) throws Exception {

        /* 비밀번호 검증 */
        /* UserPasswd 확인 */
        boolean checkPw = isValidUserPw(changePwDto.getUserChangePw());
        if (!checkPw) {
            return PublicStatus.PW_ERR;
        }

        /* UserPasswd 비밀번호 확인 */
        boolean checkPwCheck = isValidUserPwCheck(changePwDto.getUserChangePw(), changePwDto.getUserChangePwCheck());
        if (!checkPwCheck) {
            return PublicStatus.PW_CHECK_ERR;
        }

        /* 액세스 토큰 확인 */
        JwtValidationResult jwtResult = jwtProvider.validateJwtAndCleanIfInvalid("yummy-access-token", res, req);

        if (jwtResult.getStatus() == JwtValidationStatus.SUCCESS) {
            String userNo = jwtResult.getClaims().getSubject();

            String saltValue = HashUtil.generateSalt();
            String userPwHash = HashUtil.hashWithSalt(changePwDto.getUserChangePw(), saltValue);

            Optional<UserTbl> userOptional = userService.findUserByUserNo(Long.parseLong(userNo));

            if (userOptional.isPresent()) {
                UserTbl user = userOptional.get();
                user.modifyUserPwAndSalt(userPwHash, saltValue);
            }

            Optional<UserTempPwTbl> userTempPwOptional =
                    userService.findUserTempPwTblByUserNo(Long.parseLong(userNo));

            if (userTempPwOptional.isPresent()) {
                UserTempPwTbl userTempPw = userTempPwOptional.get();
                userService.deleteUserTempPwTbl(userTempPw);
            }

            /* 기존 jwt를 제거해준다. -> 재 로그인 유도하기 위함. */
            CookieUtil.clearCookie(res, "yummy-access-token");

        } else if (jwtResult.getStatus() == JwtValidationStatus.EXPIRED) {
            /* 재 로그인 후 비밀번호 수정 필요 -> 이건 뭔가 수정이 필요해 보임. */
            return PublicStatus.LOGIN_AGAIN;
        } else {
            /* 토큰이 유효하지 않음 */
            return PublicStatus.AUTH_ERROR;
        }

        return PublicStatus.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus recoverUserPw(FindPwDto findPwDto) throws Exception {

        /* 1. 유효성 검사 */
        /* 아이디 검사 */
        boolean checkId = isValidUserIdFormat(findPwDto.getUserId());
        if (!checkId) {
            return PublicStatus.ID_ERR;
        }

        /* 이름 검사 */
        boolean checkUserName = isValidUserName(findPwDto.getUserNm());
        if (!checkUserName) {
            return PublicStatus.NAME_ERR;
        }

        /* 통신사 검사 */
        boolean checkUserTelecom = isValidUserMobileCarrier(findPwDto.getTelecom());
        if (!checkUserTelecom) {
            return PublicStatus.TELECOM_ERR;
        }

        /* 휴대폰 번호 양식 검사*/
        boolean phoneNumCheck = isValidUserPhoneNumberFormat(findPwDto.getPhoneNumber());
        if (!phoneNumCheck) {
            return PublicStatus.PHONE_ERR;
        }

        /* 이메일 검사 */
        boolean checkEmail = isValidUserEmailFormat(findPwDto.getEmail());
        if (!checkEmail) {
            return PublicStatus.EMAIL_FORMAT_ERR;
        }

        /* 2.사용자 조회 */
        UserTbl userTbl = findUserInfoJoinUserEmailAndPhoneNumberTbl(findPwDto);

        if (userTbl == null) {
            return PublicStatus.PW_FIND_ERR;
        }

        /* 3. 임시 비밀번호 생성 및 기존 토큰 모두 제거 -> 이 작업을 항후에는 배치로 옮겨서 해야될수도 있을듯 */
        String tempPw = issueAndSaveTempPassword(userTbl);

        /* 4. 기존 유저의 토큰 아이디 모두 제거 */
        deleteUserTokenIds(userTbl.getUserNo());

        /* 5. Kafka를 통해 전송 */
        eventProducerService.produceUserTempPw(findPwDto.getUserId(), findPwDto.getEmail(), tempPw);

        return PublicStatus.SUCCESS;
    }

    public PublicStatus recoverUserId(FindIdDto findIdDto) throws Exception {

        /* 이름 검사 */
        boolean checkUserName = isValidUserName(findIdDto.getUserNm());
        if (!checkUserName) {
            return PublicStatus.NAME_ERR;
        }

        /* 통신사 검사 */
        boolean checkUserTelecom = isValidUserMobileCarrier(findIdDto.getTelecom());
        if (!checkUserTelecom) {
            return PublicStatus.TELECOM_ERR;
        }

        /* 휴대폰 번호 검사 */
        boolean checkUserPhoneNumber = isValidUserPhoneNumberFormat(findIdDto.getPhoneNumber());
        if (!checkUserPhoneNumber) {
            return PublicStatus.PHONE_ERR;
        }

        /* 이메일 검사 */
        boolean checkEmail = isValidUserEmailFormat(findIdDto.getEmail());
        if (!checkEmail) {
            return PublicStatus.EMAIL_FORMAT_ERR;
        }

        /**
         * 문제가 없다면, 회원 아이디를 회원의 이메일에 전송하는 로직을 짜준다.
         * 여기서 JAVA에서 SMTP 로 바로 쏴주는게 아닌, Kafka 토픽으로 회원의 아이디 정보를 보내주면 된다.
         */
        String findUserId = findUserIdByFindId(findIdDto);

        /* 입력한 정보를 토대로 회원정보가 존재하지 않음 */
        if (findUserId == null || findUserId.isEmpty()) {
            return PublicStatus.ID_FIND_ERR;
        }

        /* 회원정보가 존재하는 경우 -> Kafka Producing */
        eventProducerService.produceUserIdInfo(findUserId, findIdDto.getEmail());

        return PublicStatus.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus joinMember(HttpServletResponse res, HttpServletRequest req, JoinMemberDto joinMemberDto) throws Exception {

        /* UserID 확인 */
        JoinMemberIdStatus checkId = checkUserId(joinMemberDto.getUserId());

        if (checkId == JoinMemberIdStatus.NONFORMAT) {
            return PublicStatus.ID_ERR;
        } else if (checkId == JoinMemberIdStatus.DUPLICATED) {
            return PublicStatus.ID_DUPLICATED;
        }

        /* UserPasswd 확인 */
        boolean checkPw = isValidUserPw(joinMemberDto.getPassword());
        if (!checkPw) {
            return PublicStatus.PW_ERR;
        }

        /* UserPasswd 비밀번호 확인 */
        boolean checkPwCheck = isValidUserPwCheck(joinMemberDto.getPassword(), joinMemberDto.getPasswordCheck());
        if (!checkPwCheck) {
            return PublicStatus.PW_CHECK_ERR;
        }
        
        /* Email 검사 */
        PublicStatus checkEmail = isValidUserEmail(joinMemberDto.getEmail());
        if (checkEmail != PublicStatus.SUCCESS) {
            return checkEmail;
        }

        /* 이메일 통신 검증 일단 주석 */
//        String  verifiedKey = String.format("%s:%s",redisJoinEmailVerifiedYn,joinMemberDto.getEmail());
//        Object value = redisAdapter.get(verifiedKey);
//        if(value == null ||(value != null && !("Y".equals(value.toString())))){
//            return PublicStatus.EMAIL_NOT_VERIFIED;
//        }

        /* 이름 검사 */
        boolean checkUserName = isValidUserName(joinMemberDto.getName());
        if (!checkUserName) {
            return PublicStatus.NAME_ERR;
        }

        /* 생년월일 검사 */
        boolean checkUserBirthday = isValidUserBirthday(joinMemberDto.getBirthDate());
        if (!checkUserBirthday) {
            return PublicStatus.BIRTH_ERR;
        }

        /* 통신사 검사 */
        boolean checkUserTelecom = isValidUserMobileCarrier(joinMemberDto.getTelecom());
        if (!checkUserTelecom) {
            return PublicStatus.TELECOM_ERR;
        }

        /* 성별 검사 */
        boolean checkUserGender = isValidUserGender(joinMemberDto.getGender());
        if (!checkUserGender) {
            return PublicStatus.GENDER_ERR;
        }

        /* 휴대전화번호 검사 */
        PublicStatus checkUserPhoneNumber = isValidUserPhoneNumber(joinMemberDto.getPhoneNumber());
        if (checkUserPhoneNumber != PublicStatus.SUCCESS) {
            return checkUserPhoneNumber;
        }

        /* 신규회원 저장 */
        return validateOauthAndInputUser(res, req, joinMemberDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus linkMemberByOauth(StandardLoginDto standardLoginDto, HttpServletResponse res, HttpServletRequest req) throws Exception {

        /* 로그인 정보 검증 */
        StandardLoginBasicResDto loginInfo = yummyLoginService.verifyAndGetLoginUserInfo(standardLoginDto);

        if (loginInfo.getPublicStatus() != PublicStatus.SUCCESS) {
            return PublicStatus.AUTH_ERROR;
        }

        UserTbl user = loginInfo.getUserTbl();

        /* 해당 유저 Oauth2 정보 검증 */
        JwtValidationResult jwtRes = jwtProvider.validateJwtAndCleanIfInvalid("yummy-oauth-token", res, req);

        if (jwtRes.getStatus() != JwtValidationStatus.SUCCESS) {
            return PublicStatus.REJOIN_CHECK;
        }

        /* Oauth id token & Login 채널 */
        String idToken = jwtProvider.getSubjectFromJwt(jwtRes);
        OauthChannelStatus loginChannel = OauthChannelStatus.valueOf(
                jwtProvider.getClaimFromJwt(jwtRes, "oauthChannel", String.class));

        /* Oauth 채널별 로그인 서비스 */
        LoginService loginService = loginServiceFactory.getService(loginChannel);

        /* Oauth 채널당 하나의 아이디만 연동이 가능함. -> 해당 부분을 확인해주는 로직 */
        boolean userOauthCheck = loginService.isUserAuthChannelNotExists(user.getUserNo());
        if (!userOauthCheck) {
            return PublicStatus.OAUTH_DUPLICATED;
        }

        /* idToken 유저와 매칭시켜서 디비에 저장해준다. */
        loginService.inputUserOauth(user, idToken);

        /* 그외 로그인 완료처리 진행... */
        yummyLoginService.processCommonLogin(res, loginInfo, loginChannel);

        /* user_tbl에 main_oauth_channel 로 입력 -> 향후에 수정해야 할 듯. */
        userService.modifyUserTblMainOauthChannel(loginChannel, user);

        /* Oauth 유저 연동을 위한 임시 jwt 쿠키 제거 */
        CookieUtil.clearCookie(res, "yummy-oauth-token");

        return PublicStatus.SUCCESS;
    }

    public PublicStatus generateVerificationCode(String userEmail) throws Exception {
        /* 6자리 숫자 이메일 검증 코드 발급 */
        String code = String.format("%06d", new Random().nextInt(999999)); 

        if(!code.isEmpty()){
            /* 발급된 코드 정보를 e-mail 정보와 함께 key 생성 후 이메일 발송 */
            String key = String.format("%s:%s:%s",redisJoinEmailCode,userEmail,code);
            eventProducerService.produceJoinEmailCode(userEmail,code);
            /* 이메일 발송 후 3분 유효 기간으로 코드 정보 저장 */
            boolean isVerificationCode = redisAdapter.set(key, code, Duration.ofMinutes(3));

            if(isVerificationCode)
                return PublicStatus.SUCCESS;
            else
                return PublicStatus.EMAIL_ERR;

        }else{
            return PublicStatus.EMAIL_ERR;
        }
    }

    public PublicStatus checkVerificationCode(String userEmail,int code) {
        if(userEmail.isEmpty() || code <=0){
            return PublicStatus.EMAIL_ERR;
        }
        /*
           코드 유효성 검증 추가
           이메일 , 코드 정보로 저장된 데이터가 있는지 확인
        */
        String key = String.format("%s:%s:%s",redisJoinEmailCode,userEmail,code);
        Object value = redisAdapter.get(key);

        if(value != null){
            /*
                저장 된 코드 값이 있으면 유효한 인증으로 판단하고 인증 여부 기록
                30분의 인증 여유 기간이 있으며 회원 가입 유효 기간으로 판단
                인증 완료 후 30분이 지나면 재 인증 시도 진행
            */
            String  verifiedKey = String.format("%s:%s", redisJoinEmailVerifiedYn,userEmail);
            boolean isVerifieded = redisAdapter.set(verifiedKey,
                                              "Y",
                                                    Duration.ofMinutes(30));
            if(isVerifieded)
                return PublicStatus.SUCCESS;
            else
                return PublicStatus.EMAIL_ERR;
        } else{
            return PublicStatus.EMAIL_ERR;
        }
    }
}
