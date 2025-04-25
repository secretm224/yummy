package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserTokenIdTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserTokenIdTblId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenIdRepository extends JpaRepository<UserTokenIdTbl, UserTokenIdTblId> {
}
