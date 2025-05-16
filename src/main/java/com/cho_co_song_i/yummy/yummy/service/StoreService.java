package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeMajorDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeSubDto;
import com.cho_co_song_i.yummy.yummy.entity.StoreLocationInfoTbl;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StoreService {
    /**
     *
     * @return
     */
    List<StoreDto> findAllStores();

    /**
     *
     * @param id
     * @return
     */
    Optional<StoreDto> findStoreById(Long id);

    /**
     *
     * @param seq
     * @return
     */
    Optional<StoreLocationInfoTbl> findStoreLocationInfo(Long seq);

    /**
     *
     * @param dto
     * @return
     */
    StoreDto createStore(StoreDto dto);

    /**
     *
     * @param id
     * @param dto
     * @return
     */
    StoreDto modifyStore(Long id, StoreDto dto);

    /**
     * Store 객체를 디비에 저장해주는 함수
     * @param addStoreDto
     * @return
     * @throws Exception
     */
    Boolean isAddedStore(AddStoreDto addStoreDto) throws Exception;

    /**
     * 음식점 대분류 데이터를 가져와주는 함수
     * @return
     * @throws Exception
     */
    List<StoreTypeMajorDto> findStoreTypeMajors() throws Exception;

    /**
     * 음식점 소분류 데이터를 가져와주는 함수
     * @param majorType
     * @return
     * @throws Exception
     */
    List<StoreTypeSubDto> findStoreTypeSubs(Long majorType) throws Exception;

    /**
     * Kakao Search 를 통해서 음식점의 상세정보를 가져와주는 함수
     * @param storeName
     * @param lngX
     * @param latY
     * @return
     */
    Optional<JsonNode> inputDetailQuery(String storeName , BigDecimal lngX, BigDecimal latY);

    /**
     * Store 테이블 모든 데이터 대상으로 Kakao Search 를 연동해서
     * 음식점의 tel, url 정보를 입력해주는 함수
     * @return
     */
    Optional<JsonNode> modifyAllStoreDetail();

    /**
     * Store 테이블에서 tel, url 컬럼 내용이 존재하지 않는 row 대상으로
     * Kakao Search 를 연동해서 음식점의 tel, url 정보를 입력해주는 함수
     * @return
     */
    Optional<JsonNode> modifyEmptyStoreDetail();

    /**
     * 특정 id와 tel, url 을 입력하면 특정 id 의 tel, url 을 원하는 데이터로 입력해준다.
     * @param id
     * @param tel
     * @param url
     * @return
     */
    StoreDto modifySingleStoreDetail(long id, String tel, String url);
}
