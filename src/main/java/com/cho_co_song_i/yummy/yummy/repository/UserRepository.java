package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserTbl, Long> {

    /**
     * 유저의 아이디를 통해서 유저 정보를 가져오는 함수
     * @param loginId
     * @return
     */
    @Query("SELECT u FROM UserTbl u WHERE u.userId = :loginId")
    UserTbl findUserByLoginId(@Param("loginId") String loginId);

//    /**
//     * 유저의 메인  Oauth 채널을 가져와주는 함수
//     * @param userNo
//     * @return
//     */
//    @Query("SELECT u.mainOauthChannel FROM UserTbl u WHERE u.userNo = :userNo")
//    Optional<String> findMainOauthChannelByUserNo(Long userNo);
}
