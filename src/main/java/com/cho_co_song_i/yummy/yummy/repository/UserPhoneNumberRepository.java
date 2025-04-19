package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserPhoneNumberTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserPhoneNumberTblId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPhoneNumberRepository extends JpaRepository<UserPhoneNumberTbl, UserPhoneNumberTblId> {
}
