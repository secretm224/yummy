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
    List<StoreDto> getAllStores();

    /**
     *
     * @param id
     * @return
     */
    Optional<StoreDto> getStoreById(Long id);

    /**
     *
     * @param seq
     * @return
     */
    Optional<StoreLocationInfoTbl> getStoreLocationInfo(Long seq);

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
    StoreDto updateStore(Long id, StoreDto dto);

    /**
     * Store 객체를 디비에 저장해주는 함수
     * @param addStoreDto
     * @return
     */
    Boolean addStore(AddStoreDto addStoreDto);

    /**
     * 음식점 대분류 데이터를 가져와주는 함수
     * @return
     */
    List<StoreTypeMajorDto> getStoreTypeMajors();

    /**
     *
     * @param majorType
     * @return
     */
    List<StoreTypeSubDto> getStoreTypeSubs(Long majorType);

    /**
     *
     * @param storeName
     * @param lngX
     * @param latY
     * @return
     */
    Optional<JsonNode> StoreDetailQuery(String storeName , BigDecimal lngX, BigDecimal latY);

    /**
     *
     * @return
     */
    Optional<JsonNode> UpdateStoreDetail();


}
