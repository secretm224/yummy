package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.StoreCategoryTbl;
import com.cho_co_song_i.yummy.yummy.entity.StoreCategoryTblId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategoryTbl, StoreCategoryTblId> {
}
