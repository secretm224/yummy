package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserTempPwTbl;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserTempPwHistoryRepository extends JpaRepository<UserTempPwTbl, Long>{
}
