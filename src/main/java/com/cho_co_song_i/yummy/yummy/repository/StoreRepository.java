package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

//    @Query("SELECT s FROM Store s WHERE s.seq IN :ids")
//    @EntityGraph(attributePaths = {}) // 명시적으로 연관 객체 로딩 안 함
//    List<Store> findOnlyStoresByIds(@Param("ids") List<Long> ids);
}