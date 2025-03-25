package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.LocationCountyTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationCountyRepository extends JpaRepository<LocationCountyTbl, Long> {
    // 필요한 커스텀 메소드 추가 가능
}