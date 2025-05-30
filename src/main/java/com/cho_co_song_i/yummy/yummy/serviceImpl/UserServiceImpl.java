package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.component.JwtProvider;
import com.cho_co_song_i.yummy.yummy.dto.JoinMemberDto;
import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.dto.userCache.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.repository.*;
import com.cho_co_song_i.yummy.yummy.service.UserService;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static com.cho_co_song_i.yummy.yummy.entity.QUserLocationDetailTbl.userLocationDetailTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserPictureTbl.userPictureTbl;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserLocationDetailRepository userLocationDetailRepository;
    private final UserTokenIdRepository userTokenIdRepository;
    private final UserTempPwHistoryRepository userTempPwHistoryRepository;
    private final UserEmailRepository userEmailRepository;
    private final UserPhoneNumberRepository userPhoneNumberRepository;
    private final UserOauthKakaoRepository userOauthKakaoRepository;

    private final RedisAdapter redisAdapter;

    @Value("${spring.redis.oauth_main_channel}")
    private String userOauthMainChannel;

    public UserBasicInfoDto getUserBasicInfos(UserTbl user) {
        return userLocationDetailRepository.findFirstByUserNo(user.getUserNo())
                .map(locationDetail -> new UserBasicInfoDto(
                        user.getUserId(),
                        user.getUserNm(),
                        user.getUserBirth(),
                        null,
                        locationDetail.getLng(),
                        locationDetail.getLat()
                ))
                .orElseGet(() -> new UserBasicInfoDto(
                        user.getUserId(),
                        user.getUserNm(),
                        user.getUserBirth(),
                        null,
                        null,
                        null
                ));
    }

    public void modifyUserTblMainOauthChannel(OauthChannelStatus loginChannel, UserTbl user) {
        String mainOauthChannel = loginChannel.toString();

        /* DB 저장 */
        user.setMainOauthChannel(mainOauthChannel);
        userRepository.save(user);

        /* Redis 저장 */
        String redisKey = String.format("%s:%s", userOauthMainChannel, user.getUserNo());
        redisAdapter.set(redisKey, mainOauthChannel);
    }

    public void inputUserTokenId(UserTbl user, String tokenId) {
        UserTokenIdTbl userTokenIdTbl = new UserTokenIdTbl();
        userTokenIdTbl.setUser(user);

        UserTokenIdTblId userTokenIdTblId = new UserTokenIdTblId(tokenId, user.getUserNo());
        userTokenIdTbl.setId(userTokenIdTblId);
        userTokenIdTbl.setRegDt(new Date());
        userTokenIdTbl.setRegId("system");

        userTokenIdRepository.save(userTokenIdTbl);
    }

    public Boolean isTempLoginUser(UserTbl userTbl) {
        return userTempPwHistoryRepository.existsByUserNo(userTbl.getUserNo()) != 0;
    }

    public UserTbl findUserByLoginId(String userId) {
        return userRepository.findUserByLoginId(userId);
    }

    public Optional<UserTbl> findUserByUserNo(Long userNo) {
        return userRepository.findById(userNo);
    }

    public void inputUserTempPwTbl(UserTempPwTbl userTempPwTbl) {
        userTempPwHistoryRepository.save(userTempPwTbl);
    }

    public void inputUserEmail(UserTbl user, String email) {
        UserEmailTbl userEmailTbl = new UserEmailTbl();
        userEmailTbl.setUser(user);
        userEmailTbl.setUserNo(user.getUserNo());
        userEmailTbl.setUserEmailAddress(email);
        userEmailTbl.setRegDt(new Date());
        userEmailTbl.setRegId("system");
        userEmailTbl.setChgDt(null);
        userEmailTbl.setChgId(null);

        userEmailRepository.save(userEmailTbl);
    }

    public UserTbl createUser(JoinMemberDto joinMemberDto) throws Exception {
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

    public void inputUserPhoneNumber(UserTbl user, String phoneNumber, String telecom) {
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

    public Optional<UserTempPwTbl> findUserTempPwTblByUserNo(Long userNo) {
        return userTempPwHistoryRepository.findById(userNo);
    }

    public void deleteUserTempPwTbl(UserTempPwTbl userTempPwTbl) {
        userTempPwHistoryRepository.delete(userTempPwTbl);
    }

    public boolean isDuplicatedUserEmail(String email) {
        return userEmailRepository.existsByEmail(email) == 0;
    }

    public Optional<UserOauthKakaoTbl> findUserOauthKakaoTblByTokenId(String tokenId) {
        return userOauthKakaoRepository.findFirstByTokenIdAndOauthBannedYn(tokenId, 'N');
    }

    public void inputUserOauthKakaoTbl(UserOauthKakaoTbl userOauthKakaoTbl) {
        userOauthKakaoRepository.save(userOauthKakaoTbl);
    }

    public Optional<UserOauthKakaoTbl> findUserOauthKakaoTblByUserNo(Long userNo) {
        return userOauthKakaoRepository.findById(userNo);
    }
}