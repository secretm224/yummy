package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.JoinMemberDto;
import com.cho_co_song_i.yummy.yummy.dto.PublicResponse;
import com.cho_co_song_i.yummy.yummy.entity.*;
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

@Service
@Slf4j
public class JoinMemberService {

    private final JPAQueryFactory queryFactory;
    private final UserRepository userRepository;
    private final UserPhoneNumberRepository userPhoneNumberRepository;
    private final UserEmailRepository userEmailRepository;


    public JoinMemberService(JPAQueryFactory queryFactory, UserRepository userRepository,
                             UserPhoneNumberRepository userPhoneNumberRepository, UserEmailRepository userEmailRepository
    ) {
        this.queryFactory = queryFactory;
        this.userRepository = userRepository;
        this.userPhoneNumberRepository = userPhoneNumberRepository;
        this.userEmailRepository = userEmailRepository;
    }

    //private

    /**
     * 회원가입 해주는 서비스 함수
     * @param joinMemberDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public PublicResponse joinMember(JoinMemberDto joinMemberDto) throws Exception {

        /* UserID 확인 */
        try {
            boolean checkId = checkUserId(joinMemberDto.getUserId());
            if (!checkId) {
                return new PublicResponse("ID_ERR","Id does not conform to the rule");
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

        userRepository.save(user);

        Long userNo = user.getUserNo();
        if (userNo == null) {
            throw new Exception("Unable to obtain userNo after saving User.");
        }

        UserEmailTblId userEmailTblId = new UserEmailTblId(userNo, joinMemberDto.getEmail());
        UserEmailTbl userEmailTbl = new UserEmailTbl();
        userEmailTbl.setUser(user);
        userEmailTbl.setId(userEmailTblId);
        userEmailTbl.setRegDt(new Date());
        userEmailTbl.setRegId("system");
        userEmailTbl.setChgDt(null);
        userEmailTbl.setChgId(null);

        userEmailRepository.save(userEmailTbl);

        test();

        UserPhoneNumberTblId userPhoneNumberTblId = new UserPhoneNumberTblId(userNo, joinMemberDto.getPhoneNumber());
        UserPhoneNumberTbl userPhoneNumberTbl = new UserPhoneNumberTbl();
        userPhoneNumberTbl.setUser(user);
        userPhoneNumberTbl.setId(userPhoneNumberTblId);
        userPhoneNumberTbl.setTelecomName(joinMemberDto.getTelecom());
        userPhoneNumberTbl.setRegDt(new Date());
        userPhoneNumberTbl.setRegId("system");
        userPhoneNumberTbl.setChgDt(null);
        userPhoneNumberTbl.setChgId(null);

        userPhoneNumberRepository.save(userPhoneNumberTbl);

        return true;
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
    private boolean checkUserId(String userId) throws Exception {

        if (userId == null || userId.isEmpty()) {
            return false;
        }

        /* 특수문자가 포함되면 false (문자만 허용) */
        if (!userId.matches("^[\\p{L}]+$")) {
            return false;
        }

        try {
           return queryFactory
                    .selectOne()
                    .from(userTbl)
                    .where(userTbl.userId.eq(userId))
                    .fetchFirst() == null;
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
