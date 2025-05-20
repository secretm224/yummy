package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.UserPictureTbl;
import com.cho_co_song_i.yummy.yummy.entity.UserPictureTblId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPictureRepository extends JpaRepository<UserPictureTbl, UserPictureTblId> {
}
