package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserTempPwTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserTempPwHistoryRepository extends JpaRepository<UserTempPwTbl, Long>{

    @Query(value = "SELECT EXISTS (SELECT 1 FROM user_temp_pw_tbl WHERE user_no = :userNo)", nativeQuery = true)
    Long existsByUserNo(@Param("userNo") Long userNo);

}
