package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeMajorDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeSubDto;
import com.cho_co_song_i.yummy.yummy.entity.StoreLocationInfoTbl;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StoreService {

    /**
     * 새로운 상점을 등록해주는 기능.
     * @param storeName
     * @param page
     * @param size
     * @param pLat
     * @param pLng
     * @param zeroYn
     * @return
     */
    PublicStatus inputNewStore(String storeName, Integer page, Integer size, BigDecimal pLat, BigDecimal pLng, Boolean zeroYn);
    /**
     * 새로운 상점 여러개를 등록해주는 기능.
     * @param storeName
     * @param page
     * @param category
     * @param zeroYn
     * @return
     */
    PublicStatus inputNewStores(String storeName, Integer page, String category, Boolean zeroYn);
    /**
     * 기존 store 데이터를 모두 Kakao API 기반으로 update 시켜주는 메소드
     * @return
     */
    PublicStatus modifyExistsStores();

    /**
     * 특정 상점의 별점 평균 점수를 가져와주는 메소드
     * @param storeSeq
     * @return
     * @throws Exception
     */
    BigDecimal findRateScore(long storeSeq) throws Exception;

    /**
     * 특정 상점의 리뷰 개수를 가져와주는 메소드
     * @param storeSeq
     * @return
     */
    long findReviewCnt(long storeSeq);

}
