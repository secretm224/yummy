package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserLocationDetailTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserLocationDetailTblId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserLocationDetailRepository extends JpaRepository<UserLocationDetailTbl, UserLocationDetailTblId> {

    @Query("SELECT u FROM UserLocationDetailTbl u WHERE u.id.userNo = :userNo")
    Optional<UserLocationDetailTbl> findFirstByUserNo(@Param("userNo") Long userNo);
}
