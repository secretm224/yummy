package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.JwtValidationResult;
import com.cho_co_song_i.yummy.yummy.dto.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.UserLocationDetailTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.JwtValidationStatus;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.cho_co_song_i.yummy.yummy.entity.QUserLocationDetailTbl.userLocationDetailTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserTbl.userTbl;

@Service
@Slf4j
public class UserService {

    private final JPAQueryFactory queryFactory;
    private final JwtProviderService jwtProviderService;

    public UserService(JPAQueryFactory queryFactory, JwtProviderService jwtProviderService) {
        this.queryFactory = queryFactory;
        this.jwtProviderService = jwtProviderService;
    }

    /**
     * 유저의 기본 회원 정보. -> 뭔가 추가할게 있다면 여기랑 아래의 convertUserToBasicInfo 함수를 추가해주면 된다.
     * @param user
     * @return
     */
    public UserBasicInfoDto getUserBasicInfos(UserTbl user) {

        UserLocationDetailTbl userLocationDetail = queryFactory
                .selectFrom(userLocationDetailTbl)
                .where(userLocationDetailTbl.id.userNo.eq(user.getUserNo()))
                .fetchFirst();

        return convertUserToBasicInfo(user, userLocationDetail);
    }

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
     * 유저의 기본정보(Oauth)를 가져와주는 함수
     * @param UserNo
     * @param userOAuthInfoDto
     * @return
     */
    public UserBasicInfoDto getUserInfos(Long UserNo, UserOAuthInfoDto userOAuthInfoDto) {

        UserTbl loginUser = queryFactory
                .selectFrom(userTbl)
                .where(
                        userTbl.userNo.eq(UserNo)
                )
                .fetchFirst();

        UserLocationDetailTbl userLocationDetail = queryFactory
                .selectFrom(userLocationDetailTbl)
                .where(userLocationDetailTbl.id.userNo.eq(UserNo))
                .fetchFirst();

        return new UserBasicInfoDto(
                loginUser.getUserId(),
                loginUser.getUserNm(),
                loginUser.getUserBirth(),
                userOAuthInfoDto.getUserPicture(),
                Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLng).orElse(null),
                Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLat).orElse(null)
        );
    }

    /**
     * Jwt 의 토큰을 검증하고 그 내부의 내용을 반환해주는 함수.
     * - 만료된 토큰이거나, 위조된 토큰인 경우 삭제도 병행함.
     * @param jwtName
     * @param res
     * @param req
     * @return
     */
    public JwtValidationResult validateJwtAndCleanIfInvalid(String jwtName, HttpServletResponse res, HttpServletRequest req) {
        JwtValidationResult jwtValidationResult = jwtProviderService.validateAndParseJwt(jwtName, req);
        JwtValidationStatus jwtStatus = jwtValidationResult.getStatus();

        if (jwtStatus != JwtValidationStatus.SUCCESS) {
            /* 유효하지 않은 or 만료된 jwt 경우 삭제 시켜준다. */
            CookieUtil.clearCookie(res, jwtValidationResult.getJwtName());
        }

        return jwtValidationResult;
    }


    /**
     * JWT 의 subject 를 리턴해주는 함수
     * @param jwtValidationResult
     * @return
     */
    public String getSubjectFromJwt(JwtValidationResult jwtValidationResult) {

        if (jwtValidationResult == null || jwtValidationResult.getClaims() == null) {
            return null;
        }

        return jwtValidationResult.getClaims().getSubject();
    }

    /**
     * Jwt 의 claims 를 반환시켜주는 함수.
     * @param jwtValidationResult
     * @param claimName
     * @param clazz
     * @return
     * @param <T>
     */
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
