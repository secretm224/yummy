package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserOauthKakaoTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserOauthKakaoRepository extends JpaRepository<UserOauthKakaoTbl, String> {
//    @Query("SELECT u FROM UserOauthKakaoTbl u WHERE u.tokenId = :tokenId AND u.oauthBannedYn = 'N'")
//    Optional<UserOauthKakaoTbl> findByTokenIdAndNotBanned(@Param("tokenId") String tokenId);
    Optional<UserOauthKakaoTbl> findFirstByTokenIdAndOauthBannedYn(String tokenId, char oauthBannedYn);
    Optional<UserOauthKakaoTbl> findFirstByUserNo(Long userNo);
}
