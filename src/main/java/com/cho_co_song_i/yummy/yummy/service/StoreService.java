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
     *
     * @param storeName
     * @param lngX
     * @param latY
     * @return
     */
    Optional<JsonNode> inputDetailQuery(String storeName , BigDecimal lngX, BigDecimal latY);

    /**
     *
     * @return
     */
    Optional<JsonNode> modifyAllStoreDetail();

    /**
     *
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
    StoreDto modifyStoreDetail(long id, String tel, String url);
}
