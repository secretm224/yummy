package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.dto.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.UserLocationDetailTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserPictureTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserPictureTblId;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.repository.UserPictureRepository;
import com.cho_co_song_i.yummy.yummy.repository.UserRepository;
import com.cho_co_song_i.yummy.yummy.service.JwtProviderService;
import com.cho_co_song_i.yummy.yummy.service.UserService;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

import static com.cho_co_song_i.yummy.yummy.entity.QUserLocationDetailTbl.userLocationDetailTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserPictureTbl.userPictureTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserTbl.userTbl;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JPAQueryFactory queryFactory;
    private final JwtProviderService jwtProviderService;
    private final UserRepository userRepository;
    private final UserPictureRepository userPictureRepository;

    /**
     * Entity -> DTO 변환 (UserTbl)
     * @param userTbl
     * @param userLocationDetail
     * @return
     */
    private UserBasicInfoDto convertUserToBasicInfo(UserTbl userTbl, UserLocationDetailTbl userLocationDetail) {
        return new UserBasicInfoDto(
                userTbl.getUserId(),
                userTbl.getUserNm(),
                userTbl.getUserBirth(),
                "",
                Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLng).orElse(null),
                Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLat).orElse(null)
        );
    }

    /**
     * 유저번호를 이용해서 유저의 정보를 가져와주는 함수
     * @param userNo
     * @return
     * @throws Exception
     */
    private UserTbl findLoginUser(Long userNo) throws Exception {
        return userRepository.findById(userNo)
                .orElseThrow(() -> new Exception(
                        String.format("[Error][UserService->getUserInfoAndModifyUserPic] This user does not exist. userNo: %d", userNo)
                ));
    }

    /**
     * 유저번호를 이용해서 유저의 지역정보를 가져와준다.
     * @param userNo
     * @return
     */
    private UserLocationDetailTbl findLoginUserLocationDetail(Long userNo) {
        return queryFactory
                .selectFrom(userLocationDetailTbl)
                .where(userLocationDetailTbl.id.userNo.eq(userNo))
                .fetchFirst();
    }

    /**
     * 새로운 유저의 프로필 사진 정보를 저장해준다.
     * @param userNo
     * @param loginChannel
     * @param userPicUrl
     */
    private void inputUserPicTbl(Long userNo, OauthChannelStatus loginChannel, String userPicUrl) {
        UserPictureTblId userPictureTblId = new UserPictureTblId(userNo, loginChannel.toString());
        UserPictureTbl saveUserPicture = new UserPictureTbl();
        saveUserPicture.setId(userPictureTblId);
        saveUserPicture.setPicUrl(userPicUrl);
        saveUserPicture.setActiveYn('Y');
        saveUserPicture.setRegDt(new Date());
        saveUserPicture.setRegId("system");

        userPictureRepository.save(saveUserPicture);
    }

    // 여기를 바꿔야겠는데? -> 회원의 사진정보도 같이 넣어야 된다...
    public UserBasicInfoDto getUserBasicInfos(UserTbl user) {

        UserLocationDetailTbl userLocationDetail = queryFactory
                .selectFrom(userLocationDetailTbl)
                .where(userLocationDetailTbl.id.userNo.eq(user.getUserNo()))
                .fetchFirst();

        return convertUserToBasicInfo(user, userLocationDetail);
    }

    public void modifyUserPic(Long userNo, UserOAuthInfoDto userOAuthInfoDto, OauthChannelStatus loginChannel) throws Exception {

        UserTbl loginUser = findLoginUser(userNo);
        UserLocationDetailTbl userLocationDetail = findLoginUserLocationDetail(userNo);

        UserPictureTblId userPictureTblId = new UserPictureTblId(userNo, loginChannel.toString());
        Optional<UserPictureTbl> userPic = userPictureRepository.findById(userPictureTblId);

        if (userPic.isPresent()) {
            UserPictureTbl setUserPic = userPic.get();
            setUserPic.setPicUrl(userOAuthInfoDto.getUserPicture());
            setUserPic.setActiveYn('Y');
            setUserPic.setChgDt(new Date());
            setUserPic.setChgId("system");
            userPictureRepository.save(setUserPic);
        } else {
            inputUserPicTbl(userNo, loginChannel, userOAuthInfoDto.getUserPicture());
        }
    }

    public JwtValidationResult validateJwtAndCleanIfInvalid(String jwtName, HttpServletResponse res, HttpServletRequest req) {
        JwtValidationResult jwtValidationResult = jwtProviderService.validateAndParseJwt(jwtName, req);
        JwtValidationStatus jwtStatus = jwtValidationResult.getStatus();

        if (jwtStatus != JwtValidationStatus.SUCCESS) {
            /* 유효하지 않은 or 만료된 jwt 경우 삭제 시켜준다. */
            CookieUtil.clearCookie(res, jwtValidationResult.getJwtName());
        }

        return jwtValidationResult;
    }

    public String getSubjectFromJwt(JwtValidationResult jwtValidationResult) {

        if (jwtValidationResult == null || jwtValidationResult.getClaims() == null) {
            return null;
        }

        return jwtValidationResult.getClaims().getSubject();
    }

    public <T> T getClaimFromJwt(JwtValidationResult jwtValidationResult, String claimName, Class<T> clazz) {
        if (jwtValidationResult == null || jwtValidationResult.getClaims() == null) {
            return null;
        }

        try {
            return jwtValidationResult.getClaims().get(claimName, clazz);
        } catch (Exception e) {
            log.error("[Error][UserService->getClaimFromJwt] Failed to extract claim [{}] as type [{}]: {}", claimName, clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }


}
