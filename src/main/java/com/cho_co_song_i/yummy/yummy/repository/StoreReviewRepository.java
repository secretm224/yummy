package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.StoreReviewId;
import com.cho_co_song_i.yummy.yummy.entity.StoreReviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreReviewRepository extends JpaRepository<StoreReviews, StoreReviewId> {
    long countByStoreSeq(Long seq);
}
