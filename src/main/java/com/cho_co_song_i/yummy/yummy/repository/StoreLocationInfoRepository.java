package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.StoreLocationInfoTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreLocationInfoRepository extends JpaRepository<StoreLocationInfoTbl, Long> {
}