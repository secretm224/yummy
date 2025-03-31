package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.dto.UserProfileDto;

import java.util.List;

public interface UserCustomRepository {
    List<UserProfileDto> GetUserInfo(String loginChannel, String tokenId);

}
