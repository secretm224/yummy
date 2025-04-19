package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserEmailTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserEmailTblId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEmailRepository extends JpaRepository<UserEmailTbl, UserEmailTblId> {
}
