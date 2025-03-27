package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.StoreLocationInfoTbl;
import com.cho_co_song_i.yummy.yummy.entity.StoreTypeLinkTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreTypeLinkRepository extends JpaRepository<StoreTypeLinkTbl, Long> {
}