package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.LocationCityDto;
import com.cho_co_song_i.yummy.yummy.dto.LocationCountyDto;
import com.cho_co_song_i.yummy.yummy.dto.LocationDistrictDto;

import java.util.Date;
import java.util.List;

public interface LocationService {

    /**
     * LocationCountyTbl 객체 리스트를 가져와준다.
     * -> 먼저 Redis 를 바라봐주고, 데이터가 존재하지 않거나, Redis 에 문제가 생긴 경우 db에서 데이터를 가져와준다.
     * @return
     */
    List<LocationCountyDto> getAllLocationCounty();

    /**
     * location_county_tbl 의 시,도 코드를 파라미터로 넣어주면, 해당 시,도에 속해있는 군/구 정보를 가져와준다.
     * @param locationCountyCode
     * @return
     */
    List<LocationCityDto> getLocationCities(Long locationCountyCode);

    /**
     * location_city_tbl 의 구/군 코드를 파라미터로 넣어주면, 해당 구/군에 속해있는 읍/면/동 정보를 가져와준다.
     * @param locationCityCode
     * @return
     */
    List<LocationDistrictDto> getLocationDistrict(Long locationCityCode);

    /**
     *
     * @param addStoreDto
     * @param storeSeq
     * @param now
     */
    void addStoreLocationInfoTbl(AddStoreDto addStoreDto, Long storeSeq, Date now);

    /**
     *
     * @param addStoreDto
     * @param storeSeq
     * @param now
     */
    void addZeroPossibleMarket(AddStoreDto addStoreDto, Long storeSeq, Date now);

    /**
     *
     * @param addStoreDto
     * @param storeSeq
     * @param now
     */
    void addStoreTypeLinkTbl(AddStoreDto addStoreDto, Long storeSeq, Date now);



}
