package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.UserBasicInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import com.cho_co_song_i.yummy.yummy.entity.UserLocationDetailTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.cho_co_song_i.yummy.yummy.entity.QUserLocationDetailTbl.userLocationDetailTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QUserTbl.userTbl;

@Service
@Slf4j
public class UserService {

    private final JPAQueryFactory queryFactory;

    public UserService(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 유저의 기본 회원 정보. -> 뭔가 추가할게 있다면 여기랑 아래의 convertUserToBasicInfo 함수를 추가해주면 된다.
     * @param user
     * @return
     */
    public UserBasicInfoDto getUserBasicInfos(UserTbl user) throws Exception {

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
                Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLngX).orElse(null),
                Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLatY).orElse(null)
        );
    }

    /**
     * 유저의 기본정보(Oauth)를 가져와주는 함수
     * @param UserNo
     * @param userOAuthInfoDto
     * @return
     */
    public UserBasicInfoDto getUserInfos(Long UserNo, UserOAuthInfoDto userOAuthInfoDto) throws Exception {

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
                Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLngX).orElse(null),
                Optional.ofNullable(userLocationDetail).map(UserLocationDetailTbl::getLatY).orElse(null)
        );
    }




}
