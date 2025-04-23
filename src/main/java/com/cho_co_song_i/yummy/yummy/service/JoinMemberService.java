package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.FindIdDto;
import com.cho_co_song_i.yummy.yummy.dto.JoinMemberDto;
import com.cho_co_song_i.yummy.yummy.dto.PublicResponse;
import com.cho_co_song_i.yummy.yummy.dto.SendIdFormDto;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.JoinMemberIdStatus;
import com.cho_co_song_i.yummy.yummy.repository.UserEmailRepository;
import com.cho_co_song_i.yummy.yummy.repository.UserPhoneNumberRepository;
import com.cho_co_song_i.yummy.yummy.repository.UserRepository;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static com.cho_co_song_i.yummy.yummy.entity.QUserTbl.userTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserEmailTbl.userEmailTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserPhoneNumberTbl.userPhoneNumberTbl;

@Service
@Slf4j
public class JoinMemberService {

    private final JPAQueryFactory queryFactory;
    private final UserRepository userRepository;
    private final UserPhoneNumberRepository userPhoneNumberRepository;
    private final UserEmailRepository userEmailRepository;
    private final KafkaProducerService kafkaProducerService;


    public JoinMemberService(JPAQueryFactory queryFactory, UserRepository userRepository,
                             UserPhoneNumberRepository userPhoneNumberRepository, UserEmailRepository userEmailRepository,
                             KafkaProducerService kafkaProducerService
    ) {
        this.queryFactory = queryFactory;
        this.userRepository = userRepository;
        this.userPhoneNumberRepository = userPhoneNumberRepository;
        this.userEmailRepository = userEmailRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * 회원의 아이디를 찾아주는 함수
     * @param findIdDto
     * @return
     */
    public PublicResponse findId(FindIdDto findIdDto) {

        /* 이름 검사 */
        boolean checkUserName = checkUserName(findIdDto.getUserNm());
        if (!checkUserName) {
            return new PublicResponse("NAME_ERR", "Invalid name form");
        }

        /* 통신사 검사 */
        boolean checkUserTelecom = checkUserMobileCarrier(findIdDto.getTelecom());
        if (!checkUserTelecom) {
            return new PublicResponse("TELECOM_ERR", "Invalid birthday form");
        }

        /* 휴대폰 번호 검사 */
        boolean checkUserPhoneNumber = checkUserPhoneNumber(findIdDto.getPhoneNumber());
        if (!checkUserPhoneNumber) {
            return new PublicResponse("PHONE_ERR", "Invalid phone number form");
        }

        /* 이메일 검사 */
        boolean checkEmail = checkUserEmail(findIdDto.getEmail());
        if (!checkEmail) {
            return new PublicResponse("EMAIL_ERR", "Email does not conform to the rules");
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
                return new PublicResponse("ID_FIND_ERR", "There are no membership records.");
            }

            /* 회원정보가 존재하는 경우 -> Kafka Producing */
            SendIdFormDto sendIdFormDto = new SendIdFormDto(findUserId, findIdDto.getEmail());
            kafkaProducerService.sendMessageJson(sendIdFormDto);

        } catch(Exception e) {
            log.error("[Error][JoinMemberService->findId] {}", e.getMessage(), e);
        }

        return new PublicResponse("SUCCESS", "");
    }

    /**
     * 회원가입 해주는 서비스 함수
     * @param joinMemberDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public PublicResponse joinMember(JoinMemberDto joinMemberDto) throws Exception {

        /* UserID 확인 */
        try {
            JoinMemberIdStatus checkId = checkUserId(joinMemberDto.getUserId());

            if (checkId == JoinMemberIdStatus.NONFORMAT) {
                return new PublicResponse("ID_ERR","Id does not conform to the rule");
            } else if (checkId == JoinMemberIdStatus.DUPLICATED) {
                return new PublicResponse("ID_DUPLICATED","The ID already exists.");
            }

        } catch(Exception e) {
            log.error("[Error][JoinMemberService->joinMember] {}", e.getMessage(), e);
            return new PublicResponse("ID_ERR","Id does not conform to the rule");
        }


        /* UserPasswd 확인 */
        boolean checkPw = checkUserPw(joinMemberDto.getPassword());
        if (!checkPw) {
            return new PublicResponse("PW_ERR","Password does not conform to the rule");
        }

        /* Email 검사 */
        boolean checkEmail = checkUserEmail(joinMemberDto.getEmail());
        if (!checkEmail) {
            return new PublicResponse("EMAIL_ERR", "Email does not conform to the rules");
        }

        /* 이름 검사 */
        boolean checkUserName = checkUserName(joinMemberDto.getName());
        if (!checkUserName) {
            return new PublicResponse("NAME_ERR", "Invalid name form");
        }

        /* 생년월일 검사 */
        boolean checkUserBirthday = checkUserBirthday(joinMemberDto.getBirthDate());
        if (!checkUserBirthday) {
            return new PublicResponse("BIRTH_ERR", "Invalid birthday form");
        }

        /* 통신사 검사 */
        boolean checkUserTelecom = checkUserMobileCarrier(joinMemberDto.getTelecom());
        if (!checkUserTelecom) {
            return new PublicResponse("TELECOM_ERR", "Invalid birthday form");
        }

        /* 성별 검사 */
        boolean checkUserGender = checkUserGender(joinMemberDto.getGender());
        if (!checkUserGender) {
            return new PublicResponse("GENDER_ERR", "Invalid gender form");
        }

        /* 휴대전화번호 검사 */
        boolean checkUserPhoneNumber = checkUserPhoneNumber(joinMemberDto.getPhoneNumber());
        if (!checkUserPhoneNumber) {
            return new PublicResponse("PHONE_ERR", "Invalid phone number form");
        }

        /* 검사가 문제가 없다면 DB 에 저장을 해준다. */
        boolean saveJoinUser = saveJoinUser(joinMemberDto);

        return new PublicResponse("SUCCESS", "");
    }

    /**
     * 새로운 유저의 정보를 디비에 저장해준다.
     * @param joinMemberDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    private boolean saveJoinUser(JoinMemberDto joinMemberDto) throws Exception {

        UserTbl user = saveUser(joinMemberDto);

        Long userNo = user.getUserNo();
        if (userNo == null) {
            throw new Exception("Unable to obtain userNo after saving User.");
        }

        saveUserEmail(user, joinMemberDto.getEmail());
        //test();
        saveUserPhoneNumber(user, joinMemberDto.getPhoneNumber(), joinMemberDto.getTelecom());

        return true;
    }

    /**
     * UserTbl 생성 함수
     * @param joinMemberDto
     * @return
     */
    private UserTbl saveUser(JoinMemberDto joinMemberDto) throws Exception {
        String saltValue = HashUtil.generateSalt();
        String userIdHash = HashUtil.hashWithSalt(joinMemberDto.getUserId(), saltValue);
        String userPwHash = HashUtil.hashWithSalt(joinMemberDto.getPassword(), saltValue);

        UserTbl user = new UserTbl();
        user.setUserId(joinMemberDto.getUserId());
        user.setUserIdHash(userIdHash);
        user.setUserPw(userPwHash);
        user.setUserPwSalt(saltValue);
        user.setUserNm(joinMemberDto.getName());
        user.setUserBirth(joinMemberDto.getBirthDate());
        user.setUserGender(joinMemberDto.getGender());
        user.setRegDt(new Date());
        user.setRegId("system");
        user.setChgDt(null);
        user.setChgId(null);

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
     * 아이디가 존재하는지 확인
     * @param userId
     * @return
     */
    private JoinMemberIdStatus checkUserId(String userId) throws Exception {

        if (userId == null || userId.isEmpty()) {
            return JoinMemberIdStatus.NONFORMAT;
        }

        /* 특수문자가 포함되면 false (문자 + 숫자만 허용) */
        if (!userId.matches("^(?=.*[a-z]{2,})(?=.*[0-9]{2,})[a-z0-9]+$")) {
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
     */
    private boolean checkUserPhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        return phoneNumber.matches("^010\\d{8}$");
    }



}
