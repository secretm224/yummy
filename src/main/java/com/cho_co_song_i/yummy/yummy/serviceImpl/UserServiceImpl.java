package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.component.JwtProvider;
import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.dto.userCache.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.UserLocationDetailTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserPictureTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserPictureTblId;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.repository.UserPictureRepository;
import com.cho_co_song_i.yummy.yummy.service.UserService;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final JPAQueryFactory queryFactory;
    private final JwtProvider jwtProvider;
    private final UserPictureRepository userPictureRepository;

    /**
     * 유저의 집/회사 장소관련 정보를 디비에서 반환해주는 함수
     * @param userNum
     * @return
     */
    private UserLocationDetailTbl findUserLocationDetailInfo(Long userNum) {
        return queryFactory
                .selectFrom(userLocationDetailTbl)
                .where(userLocationDetailTbl.id.userNo.eq(userNum))
                .fetchFirst();
    }

//    /**
//     * 유저의 프로필 사진을 반환해주는 함수
//     * @param userNo
//     * @param oauthChannelStatus
//     * @return
//     * @throws Exception
//     */
//    private String resolveUserPictureUrl(Long userNo, OauthChannelStatus oauthChannelStatus) throws Exception {
//        UserPictureTbl userPic;
//
//        if (oauthChannelStatus == OauthChannelStatus.standard) {
//            userPic = findUserPictureRecentInfo(userNo);
//        } else {
//            userPic = findUserPictureInfo(userNo, oauthChannelStatus);
//        }
//
//        return userPic != null ? userPic.getPicUrl() : null;
//    }

    public UserBasicInfoDto getUserBasicInfos(UserTbl user) {
        UserLocationDetailTbl userLocationDetail = findUserLocationDetailInfo(user.getUserNo());
        BigDecimal lng = Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLng).orElse(null);
        BigDecimal lat = Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLat).orElse(null);

        return new UserBasicInfoDto(
                user.getUserId(),
                user.getUserNm(),
                user.getUserBirth(),
                null,
                lng,
                lat
        );
    }

    public JwtValidationResult validateJwtAndCleanIfInvalid(String jwtName, HttpServletResponse res, HttpServletRequest req) {
        JwtValidationResult jwtValidationResult = jwtProvider.validateAndParseJwt(jwtName, req);
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

    public void inputUserPictureTbl(UserTbl userTbl, OauthChannelStatus oauthChannelStatus, String picUrl) {

        UserPictureTblId userPictureTblId = new UserPictureTblId(userTbl.getUserNo(), oauthChannelStatus.toString());
        UserPictureTbl userPicture = new UserPictureTbl();
        userPicture.setUser(userTbl);
        userPicture.setId(userPictureTblId);
        userPicture.setPicUrl(picUrl);
        userPicture.setActiveYn('Y');
        userPicture.setRegDt(new Date());
        userPicture.setRegId("system");

        userPictureRepository.save(userPicture);
    }

}
