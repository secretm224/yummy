package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.dto.JoinMemberDto;
import com.cho_co_song_i.yummy.yummy.dto.userCache.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.repository.*;
import com.cho_co_song_i.yummy.yummy.service.UserService;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    private final UserOauthGoogleRepository userOauthGoogleRepository;

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
        user.modifyUserOauthChannel(mainOauthChannel, "modifyUserTblMainOauthC");
        userRepository.save(user);

        /* Redis 저장 */
        String redisKey = String.format("%s:%s", userOauthMainChannel, user.getUserNo());
        redisAdapter.set(redisKey, mainOauthChannel);
    }

    public void inputUserTokenId(UserTbl user, String tokenId) {
        UserTokenIdTbl userTokenIdTbl = new UserTokenIdTbl(user, tokenId, "inputUserTokenId");
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
        UserEmailTbl userEmailTbl = new UserEmailTbl(user, email, "inputUserEmail");
        userEmailRepository.save(userEmailTbl);
    }

    public UserTbl createUser(JoinMemberDto joinMemberDto) throws Exception {
        String saltValue = HashUtil.generateSalt();
        String userPwHash = HashUtil.hashWithSalt(joinMemberDto.getPassword(), saltValue);
        UserTbl user = new UserTbl(joinMemberDto, userPwHash, saltValue, "createUser");
        return userRepository.save(user);
    }

    public void inputUserPhoneNumber(UserTbl user, String phoneNumber, String telecom) {
        UserPhoneNumberTbl userPhoneNumberTbl = new UserPhoneNumberTbl(user, phoneNumber, telecom, "inputUserPhoneNumber");
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
    public Optional<UserOauthGoogleTbl> findUserOauthGoogleTblByTokenId(String tokenId) {
        return userOauthGoogleRepository.findFirstByTokenIdAndOauthBannedYn(tokenId, 'N');
    }
    public void inputUserOauthKakaoTbl(UserOauthKakaoTbl userOauthKakaoTbl) {
        userOauthKakaoRepository.save(userOauthKakaoTbl);
    }
    public void inputUserOauthGoogleTbl(UserOauthGoogleTbl userOauthGoogleTbl) {
        userOauthGoogleRepository.save(userOauthGoogleTbl);
    }
    public Optional<UserOauthKakaoTbl> findUserOauthKakaoTblByUserNo(Long userNo) {
        return userOauthKakaoRepository.findById(userNo);
    }
    public Optional<UserOauthGoogleTbl> findUserOauthGoogleTblByUserNo(Long userNo) {
        return userOauthGoogleRepository.findFirstByUserNo(userNo);
    }
}