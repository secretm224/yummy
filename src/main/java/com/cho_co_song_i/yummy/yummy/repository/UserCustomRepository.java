package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.dto.UserProfileDto;

public interface UserCustomRepository {
    UserProfileDto GetUserInfo(String loginChannel, String tokenId);

}
