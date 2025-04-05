package com.cho_co_song_i.yummy.yummy.repository;

//
//@Repository
//public class UserRepositoryImpl implements UserCustomRepository {
//
//    private final JPAQueryFactory jpaQueryFactory;
//
//    public UserRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
//        this.jpaQueryFactory = jpaQueryFactory;
//    }
//
//    public List<UserProfileDto> GetUserInfo(String login_channel , String token_id){
//        QUserTbl user = QUserTbl.userTbl;
//        QUserLocationDetailTbl detail = QUserLocationDetailTbl.userLocationDetailTbl;
//        QUserAuthTbl auth = QUserAuthTbl.userAuthTbl;
//
//        List<UserProfileDto> userProfileList= jpaQueryFactory.select(
//                Projections.constructor(UserProfileDto.class,
//                        user.userNo,
//                        user.userNm,
//                        auth.id.loginChannel,
//                        auth.token_id,
//                        detail.addrType,
//                        detail.addr,
//                        detail.lngX,
//                        detail.latY,
//                        user.reg_dt
//                        ))
//                .from(user)
//                .join(auth).on(user.user_no.eq(auth.userNo))   // User와 UserAuth 조인
//                .join(detail).on(detail.userNo.eq(auth.userNo))
//                .where(auth.login_channel.eq(login_channel)
//                .and(auth.token_id.eq(token_id)))
//                .fetch();
//
//        return userProfileList;
//    }
//}
