package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.JoinMemberIdStatus;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.repository.*;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import com.cho_co_song_i.yummy.yummy.utils.PasswdUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.cho_co_song_i.yummy.yummy.entity.QUserTbl.userTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserEmailTbl.userEmailTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserPhoneNumberTbl.userPhoneNumberTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserTokenIdTbl.userTokenIdTbl;
import static com.cho_co_song_i.yummy.yummy.utils.JwtUtil.decodeJwtPayload;

@Service
@Slf4j
public class JoinMemberService {
    private final JPAQueryFactory queryFactory;
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserPhoneNumberRepository userPhoneNumberRepository;
    private final UserEmailRepository userEmailRepository;
    private final UserTempPwHistoryRepository userTempPwHistoryRepository;
    private final KafkaProducerService kafkaProducerService;
    private final JwtProviderService jwtProviderService;
    private final RedisService redisService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Value("${spring.redis.refresh-key-prefix}")
    private String refreshKeyPrefix;

    /* 회원의 아이디 찾기 관련 Kafka Topic */
    @Value("${spring.topic.kafka.find-user-id-info}")
    private String findIdTopic;

    /* 회원의 비밀번호 찾기 관련 Kafka Topic */
    @Value("${spring.topic.kafka.find-user-pw-info}")
    private String findPwTopic;

    public JoinMemberService(JPAQueryFactory queryFactory, UserRepository userRepository,
                             UserPhoneNumberRepository userPhoneNumberRepository, UserEmailRepository userEmailRepository,
                             KafkaProducerService kafkaProducerService, UserTempPwHistoryRepository userTempPwHistoryRepository,
                             RedisService redisService, EntityManager entityManager, JwtProviderService jwtProviderService,
                             UserAuthRepository userAuthRepository
    ) {
        this.queryFactory = queryFactory;
        this.userRepository = userRepository;
        this.userPhoneNumberRepository = userPhoneNumberRepository;
        this.userEmailRepository = userEmailRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.userTempPwHistoryRepository = userTempPwHistoryRepository;
        this.redisService = redisService;
        this.entityManager = entityManager;
        this.jwtProviderService = jwtProviderService;
        this.userAuthRepository = userAuthRepository;
    }

    /**
     * 회원의 비밀번호를 찾아주는 함수
     * @param findPwDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public PublicStatus findPw(FindPwDto findPwDto) throws Exception {

        /* 1. 유효성 검사 */
        /* 이름 검사 */
        boolean checkUserName = checkUserName(findPwDto.getUserNm());
        if (!checkUserName) {
            return PublicStatus.NAME_ERR;
        }

        /* 아이디 검사 */
        boolean checkId = checkIdFormat(findPwDto.getUserId());
        if (!checkId) {
            return PublicStatus.ID_ERR;
        }

        /* 이메일 검사 */
        boolean checkEmail = checkUserEmail(findPwDto.getEmail());
        if (!checkEmail) {
            return PublicStatus.EMAIL_ERR;
        }

        /* 2.사용자 조회 */
        UserTbl userTbl = fetchUserInfo(findPwDto);

        if (userTbl == null) {
            return PublicStatus.PW_FIND_ERR;
        }

        /* 3. 임시 비밀번호 생성 및 기존 토큰 모두 제거 */
        String tempPw = issueAndSaveTempPassword(userTbl);

        /* 4. 기존 유저의 토큰 아이디 모두 제거 */
        deleteUserTokenIds(userTbl.getUserNo());

        /* 5. Kafka를 통해 전송 */
        kafkaProducerService.sendMessageJson(
                findPwTopic,
                new SendPwFormDto(LocalDateTime.now(), findPwDto.getUserId(), findPwDto.getEmail(), tempPw)
        );


        return PublicStatus.SUCCESS;
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

        /* 레디스에 존재하는 토큰 모두 제거 */
        for (UserTokenIdTbl tokenTbl : userTokens) {
            Optional.ofNullable(tokenTbl.getId().getTokenId())
                    .ifPresent(token -> {
                        String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, userNo.toString(), token);
                        redisService.deleteKey(refreshKey);
                        entityManager.remove(tokenTbl);
                    });
        }
    }


    /**
     * 유저정보를 가져와주는 함수
     * @param dto
     * @return
     */
    private UserTbl fetchUserInfo(FindPwDto dto) {

        return queryFactory
                .selectFrom(userTbl)
                .join(userEmailTbl).on(userEmailTbl.user.eq(userTbl))
                .where(
                        userTbl.userNm.eq(dto.getUserNm()),
                        userTbl.userId.eq(dto.getUserId()),
                        userEmailTbl.id.userEmailAddress.eq(dto.getEmail())
                )
                .fetchFirst();
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

        UserTempPwTbl userTempPwTbl = new UserTempPwTbl();
        userTempPwTbl.setUserNo(userTbl.getUserNo());
        userTempPwTbl.setUserId(userTbl.getUserId());
        userTempPwTbl.setRegDt(new Date());
        userTempPwTbl.setRegId("system");

        userTempPwHistoryRepository.save(userTempPwTbl);

        userTbl.setUserPwSalt(pwSalt);
        userTbl.setUserPw(hashedPw);

        return tempPw;
    }


    /**
     * 유저 리프레시 토큰을 삭제해주는 함수
     * @param userNo
     */
    private void deleteRefreshToken(String userNo) {
        //String refreshKey = String.format("%s:%s:%s", refreshKeyPrefix, userNo, tokenId);
        //redisService.deleteKey(refreshKey);
    }

    /**
     * 회원의 아이디를 찾아주는 함수
     * @param findIdDto
     * @return
     */
    public PublicStatus findId(FindIdDto findIdDto) {

        /* 이름 검사 */
        boolean checkUserName = checkUserName(findIdDto.getUserNm());
        if (!checkUserName) {
            return PublicStatus.NAME_ERR;
        }

        /* 통신사 검사 */
        boolean checkUserTelecom = checkUserMobileCarrier(findIdDto.getTelecom());
        if (!checkUserTelecom) {
            return PublicStatus.TELECOM_ERR;
        }


        try {
            /* 휴대폰 번호 검사 */
            PublicStatus checkUserPhoneNumber = checkUserPhoneNumber(findIdDto.getPhoneNumber());
            if (checkUserPhoneNumber != PublicStatus.SUCCESS) {
                return checkUserPhoneNumber;
            }

        } catch(Exception e) {
            log.error("[Error][JoinMemberService->findId] {}", e.getMessage(), e);
            return PublicStatus.SERVER_ERR;
        }


        /* 이메일 검사 */
        boolean checkEmail = checkUserEmail(findIdDto.getEmail());
        if (!checkEmail) {
            return PublicStatus.EMAIL_ERR;
        }

        /**
         * 문제가 없다면, 회원 아이디를 회원의 이메일에 전송하는 로직을 짜준다.
         * 여기서 JAVA에서 SMTP 로 바로 쏴주는게 아닌, Kafka 토픽으로 회원의 아이디 정보를 보내주면 된다.
         */
        try {

            String findUserId = queryFactory
                    .select(userTbl.userId)
                    .from(userTbl)
                    .join(userEmailTbl).on(userEmailTbl.user.eq(userTbl))
                    .join(userPhoneNumberTbl).on(userPhoneNumberTbl.user.eq(userTbl))
                    .where(
                            userTbl.userNm.eq(findIdDto.getUserNm()),
                            userPhoneNumberTbl.id.phoneNumber.eq(findIdDto.getPhoneNumber()),
                            userPhoneNumberTbl.telecomName.eq(findIdDto.getTelecom()),
                            userEmailTbl.id.userEmailAddress.eq(findIdDto.getEmail())
                    )
                    .fetchFirst();

            /* 입력한 정보를 토대로 회원정보가 존재하지 않음 */
            if (findUserId == null || findUserId.isEmpty()) {
                return PublicStatus.ID_FIND_ERR;
            }

            /* 회원정보가 존재하는 경우 -> Kafka Producing */
            SendIdFormDto sendIdFormDto = new SendIdFormDto(LocalDateTime.now(), findUserId, findIdDto.getEmail());
            kafkaProducerService.sendMessageJson(findIdTopic, sendIdFormDto);

        } catch(Exception e) {
            log.error("[Error][JoinMemberService->findId] {}", e.getMessage(), e);
        }

        return PublicStatus.SUCCESS;
    }

    /**
     * 회원가입 해주는 서비스 함수
     * @param joinMemberDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public PublicStatus joinMember(HttpServletResponse res, HttpServletRequest req, JoinMemberDto joinMemberDto) throws Exception {

        /* UserID 확인 */
        try {
            JoinMemberIdStatus checkId = checkUserId(joinMemberDto.getUserId());

            if (checkId == JoinMemberIdStatus.NONFORMAT) {
                return PublicStatus.ID_ERR;
            } else if (checkId == JoinMemberIdStatus.DUPLICATED) {
                return PublicStatus.ID_DUPLICATED;
            }

        } catch(Exception e) {
            log.error("[Error][JoinMemberService->joinMember] {}", e.getMessage(), e);
            return PublicStatus.ID_ERR;
        }


        /* UserPasswd 확인 */
        boolean checkPw = checkUserPw(joinMemberDto.getPassword());
        if (!checkPw) {
            return PublicStatus.PW_ERR;
        }

        /* UserPasswd 비밀번호 확인 */
        boolean checkPwCheck = checkUserPwCheck(joinMemberDto.getPassword(), joinMemberDto.getPasswordCheck());
        if (!checkPwCheck) {
            return PublicStatus.PW_CHECK_ERR;
        }

        /* Email 검사 */
        boolean checkEmail = checkUserEmail(joinMemberDto.getEmail());
        if (!checkEmail) {
            return PublicStatus.EMAIL_ERR;
        }

        /* 이름 검사 */
        boolean checkUserName = checkUserName(joinMemberDto.getName());
        if (!checkUserName) {
            return PublicStatus.NAME_ERR;
        }

        /* 생년월일 검사 */
        boolean checkUserBirthday = checkUserBirthday(joinMemberDto.getBirthDate());
        if (!checkUserBirthday) {
            return PublicStatus.BIRTH_ERR;
        }

        /* 통신사 검사 */
        boolean checkUserTelecom = checkUserMobileCarrier(joinMemberDto.getTelecom());
        if (!checkUserTelecom) {
            return PublicStatus.TELECOM_ERR;
        }

        /* 성별 검사 */
        boolean checkUserGender = checkUserGender(joinMemberDto.getGender());
        if (!checkUserGender) {
            return PublicStatus.GENDER_ERR;
        }

        /* 휴대전화번호 검사 */
        PublicStatus checkUserPhoneNumber = checkUserPhoneNumber(joinMemberDto.getPhoneNumber());
        if (checkUserPhoneNumber != PublicStatus.SUCCESS) {
            return checkUserPhoneNumber;
        }

        /* 신규회원 저장 */
        return checkOauthAndSaveUser(res, req, joinMemberDto);
    }


    /**
     * Oauth2 연동 회원가입 유저인지 체크하고 유저의 정보를 디비에 저장해주는 함수
     * @param res
     * @param req
     * @param joinMemberDto
     * @return
     * @throws Exception
     */
    private PublicStatus checkOauthAndSaveUser(HttpServletResponse res, HttpServletRequest req, JoinMemberDto joinMemberDto) throws Exception {

        /* Oauth2 와 연동할 아이디인지 체크 */
        String oauthToken = CookieUtil.getCookieValue(req, "yummy-oauth-token");

        if (oauthToken != null) {
            JwtValidationResult jwtResult = jwtProviderService.validateTokenAndGetSubject(oauthToken);

            if (jwtResult.getStatus() == JwtValidationStatus.SUCCESS) {
                /* JWT 검증 성공 */

                /* oauth2 토큰 아이디 */
                String idToken = jwtResult.getClaims().getSubject();

                /* oauth2 채널 - 카카오,네이버,구글...등등 */
                String oauthChannel = jwtResult.getClaims().get("oauthChannel", String.class);

                /* user 정보부터 저장 */
                UserTbl joinUser = saveJoinUser(joinMemberDto);

                /* oauth2 정보도 저장 */
                UserAuthTbl userAuthTbl = new UserAuthTbl();
                UserAuthTblId userAuthTblId = new UserAuthTblId(joinUser.getUserNo(), oauthChannel, idToken);
                userAuthTbl.setUser(joinUser);
                userAuthTbl.setId(userAuthTblId);
                userAuthTbl.setReg_dt(new Date());
                userAuthTbl.setReg_id("system");

                userAuthRepository.save(userAuthTbl);

                CookieUtil.clearCookie(res, "yummy-oauth-token");

            } else {
                /* JWT 검증 실패 */
                /* 해당 쿠키를 제거해주고 재가입 요청 보내준다. */
                CookieUtil.clearCookie(res, "yummy-oauth-temp-token");
                return PublicStatus.REJOIN_CHECK;
            }
        } else {
            saveJoinUser(joinMemberDto);
        }

        return PublicStatus.SUCCESS;
    }


    /**
     * 새로운 유저의 정보를 디비에 저장해준다.
     * @param joinMemberDto
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    private UserTbl saveJoinUser(JoinMemberDto joinMemberDto) throws Exception {

        UserTbl user = saveUser(joinMemberDto);

        Long userNo = user.getUserNo();
        if (userNo == null) {
            throw new Exception("Unable to obtain userNo after saving User.");
        }

        saveUserEmail(user, joinMemberDto.getEmail());
        saveUserPhoneNumber(user, joinMemberDto.getPhoneNumber(), joinMemberDto.getTelecom());

        return user;
    }

    /**
     * UserTbl 생성 함수
     * @param joinMemberDto
     * @return
     */
    private UserTbl saveUser(JoinMemberDto joinMemberDto) throws Exception {
        String saltValue = HashUtil.generateSalt();
        String userPwHash = HashUtil.hashWithSalt(joinMemberDto.getPassword(), saltValue);

        UserTbl user = new UserTbl();
        user.setUserId(joinMemberDto.getUserId());
        user.setUserPw(userPwHash);
        user.setUserPwSalt(saltValue);
        user.setUserNm(joinMemberDto.getName());
        user.setUserBirth(joinMemberDto.getBirthDate());
        user.setUserGender(joinMemberDto.getGender());
        user.setRegDt(new Date());
        user.setRegId("system");

        return userRepository.save(user);
    }

    /**
     * UserEmailTbl 생성 함수
     * @param user
     * @param email
     */
    private void saveUserEmail(UserTbl user, String email) {
        UserEmailTblId userEmailTblId = new UserEmailTblId(user.getUserNo(), email);
        UserEmailTbl userEmailTbl = new UserEmailTbl();
        userEmailTbl.setUser(user);
        userEmailTbl.setId(userEmailTblId);
        userEmailTbl.setRegDt(new Date());
        userEmailTbl.setRegId("system");
        userEmailTbl.setChgDt(null);
        userEmailTbl.setChgId(null);

        userEmailRepository.save(userEmailTbl);
    }

    /**
     * userPhoneNumberTbl 생성 함수
     * @param user
     * @param phoneNumber
     * @param telecom
     */
    private void saveUserPhoneNumber(UserTbl user, String phoneNumber, String telecom) {
        UserPhoneNumberTblId userPhoneNumberTblId = new UserPhoneNumberTblId(user.getUserNo(), phoneNumber);
        UserPhoneNumberTbl userPhoneNumberTbl = new UserPhoneNumberTbl();
        userPhoneNumberTbl.setUser(user);
        userPhoneNumberTbl.setId(userPhoneNumberTblId);
        userPhoneNumberTbl.setTelecomName(telecom);
        userPhoneNumberTbl.setRegDt(new Date());
        userPhoneNumberTbl.setRegId("system");
        userPhoneNumberTbl.setChgDt(null);
        userPhoneNumberTbl.setChgId(null);

        userPhoneNumberRepository.save(userPhoneNumberTbl);
    }


    /* 트랜잭션 테스트용 */
    private void test() throws Exception {
        int a = 1 / 0;
    }

    /**
     * 유저의 아이디 포멧을 확인해주는 함수
     * @param userId
     * @return
     */
    private Boolean checkIdFormat(String userId) {

        if (userId == null || userId.isEmpty()) {
            return false;
        }

        /* 특수문자가 포함되면 false */
        return userId.matches("^(?!\\d+$)[a-z0-9]+$");
    }

    /**
     * 아이디가 존재하는지 확인
     * @param userId
     * @return
     */
    private JoinMemberIdStatus checkUserId(String userId) throws Exception {

        if (!checkIdFormat(userId)) {
            return JoinMemberIdStatus.NONFORMAT;
        }

        try {

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

        } catch(Exception e) {
            throw new Exception("Error in checkUserId: {}" + e.getMessage(), e);
        }
    }

    /**
     * 비밀번호 검증
     * @param userPw
     * @return
     */
    private boolean checkUserPw(String userPw) {

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
    private boolean checkUserPwCheck(String userPw, String userPwCheck) {
        return userPw.equals(userPwCheck);
    }


    /**
     * 이메일 주소 검증
     * @param email
     * @return
     */
    private boolean checkUserEmail(String email) {

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
    private boolean checkUserName(String name) {

        if (name == null || name.isEmpty()) {
            return false;
        }

        if (name.matches(".*\\d.*")) {
            return false;
        }

        /* 특수문자가 포함되면 false (문자만 허용) */
        if (!name.matches("^[\\p{L}]+$")) {
            return false;
        }

        return true;
    }

    /**
     * 유저의 생년월일 검증
     * @param birthDate
     * @return
     */
    private boolean checkUserBirthday(String birthDate) {

        if (birthDate == null || birthDate.isEmpty()) {
            return false;
        }

        /* 8자리 숫자가 아니면 false */
        if (!birthDate.matches("\\d{8}")) {
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try {
            LocalDate parsedDate = LocalDate.parse(birthDate, formatter);
            return true;
        } catch(Exception e) {
            /* 날짜 파싱 실패 - 잘못된 날짜 */
            return false;
        }
    }

    /**
     * 유저의 통신사 검증
     * @param telecom
     * @return
     */
    private boolean checkUserMobileCarrier(String telecom) {

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
    private boolean checkUserGender(String gender) {

        if (gender == null || gender.isEmpty()) {
            return false;
        }

        return switch (gender.toLowerCase()) {
            case "m", "f", "i", "o" -> true;
            default -> false;
        };
    }


    /**
     * 유저의 핸드폰번호 검증
     * @param phoneNumber
     * @return
     * @throws Exception
     */
    private PublicStatus checkUserPhoneNumber(String phoneNumber) throws Exception {

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return PublicStatus.PHONE_ERR;
        }

        if (!phoneNumber.matches("^010\\d{8}$")) {
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



}
