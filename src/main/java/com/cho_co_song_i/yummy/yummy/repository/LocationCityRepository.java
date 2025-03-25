package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.LocationCityTbl;
import com.cho_co_song_i.yummy.yummy.entity.LocationCountyTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationCityRepository extends JpaRepository<LocationCityTbl, Long> {
}