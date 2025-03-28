package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.QUserTbl;
import com.cho_co_song_i.yummy.yummy.entity.QUserDetailTbl;
import com.cho_co_song_i.yummy.yummy.entity.QUserAuthTbl;

import com.cho_co_song_i.yummy.yummy.dto.UserProfileDto;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;


@Repository
public class UserRepositoryImpl implements UserCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;


    public UserRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public UserProfileDto GetUserInfo(String login_channel , String token_id){
        QUserTbl user = QUserTbl.userTbl;
        QUserDetailTbl detail = QUserDetailTbl.userDetailTbl;
        QUserAuthTbl auth = QUserAuthTbl.userAuthTbl;

        UserProfileDto userprofile = jpaQueryFactory.select(
                Projections.constructor(UserProfileDto.class,
                        user.user_no,
                        user.user_nm,
                        auth.login_channel,
                        auth.token_id,
                        detail.addrType,
                        detail.addr,
                        detail.lngX,
                        detail.latY,
                        user.reg_dt
                        ))
                .from(user)
                .join(user).on(user.user_no.eq(auth.authNo))   // User와 UserAuth 조인
                .join(detail).on(detail.userNo.eq(auth.userNo))
                .where(auth.login_channel.eq(login_channel)
                .and(auth.token_id.eq(token_id)))
                .fetchOne();

        return userprofile;
    }
}
