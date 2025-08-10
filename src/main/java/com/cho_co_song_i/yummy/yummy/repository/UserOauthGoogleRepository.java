package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserOauthGoogleTbl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOauthGoogleRepository extends JpaRepository<UserOauthGoogleTbl, String> {
    Optional<UserOauthGoogleTbl> findFirstByTokenIdAndOauthBannedYn(String tokenId, char oauthBannedYn);
    Optional<UserOauthGoogleTbl> findFirstByUserNo(Long userNo);
}
