package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserLocationDetailTbl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailRepository extends JpaRepository<UserLocationDetailTbl, Long> {
}
