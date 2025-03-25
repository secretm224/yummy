package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.LocationCityDto;
import com.cho_co_song_i.yummy.yummy.dto.LocationCountyDto;
import com.cho_co_song_i.yummy.yummy.dto.LocationDistrictDto;
import com.cho_co_song_i.yummy.yummy.entity.LocationCityTbl;
import com.cho_co_song_i.yummy.yummy.entity.LocationCountyTbl;
import com.cho_co_song_i.yummy.yummy.entity.LocationDistrictTbl;
import com.cho_co_song_i.yummy.yummy.repository.LocationCountyRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.cho_co_song_i.yummy.yummy.entity.QLocationCountyTbl.locationCountyTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QLocationCityTbl.locationCityTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QLocationDistrictTbl.locationDistrictTbl;

@Service
@Slf4j
public class LocationService {
    private final LocationCountyRepository locationRepository;
    private final JPAQueryFactory queryFactory;

    public LocationService(LocationCountyRepository locationRepository, JPAQueryFactory queryFactory) {
        this.locationRepository = locationRepository;
        this.queryFactory = queryFactory;
    }

    /* Entity -> DTO 변환 (LocationCountyTbl) */
    private LocationCountyDto convertCountyToDto(LocationCountyTbl locationCountyTbl) {
        return new LocationCountyDto(
                locationCountyTbl.getLocationCountyCode(),
                locationCountyTbl.getLocationCounty()
        );
    }

    /* Entity -> DTO 변환 (LocationCityTbl) */
    private LocationCityDto convertCityToDto(LocationCityTbl locationCityTbl) {
        return new LocationCityDto(
                locationCityTbl.getId().getLocationCityCode(),
                locationCityTbl.getId().getLocationCountyCode(),
                locationCityTbl.getLocationCity()
        );
    }

    /* Entity -> DTO 변환 (LocationDistrictTbl) */
    private LocationDistrictDto convertDistrictToDto(LocationDistrictTbl locationDistrictTbl) {
        return new LocationDistrictDto(
                locationDistrictTbl.getId().getLocationDistrictCode(),
                locationDistrictTbl.getId().getLocationCityCode(),
                locationDistrictTbl.getId().getLocationCountyCode(),
                locationDistrictTbl.getLocationDistrict()
        );
    }

    /* LocationCountyTbl 객체 리스트를 가져와준다. */
    public List<LocationCountyDto> getAllLocationCounty() {
        List<LocationCountyTbl> locationCountyList = locationRepository.findAll();

        return locationCountyList.stream()
                .map(this::convertCountyToDto)
                .collect(Collectors.toList());
    }

    /**
     * location_county_tbl 의 시,도 코드를 파라미터로 넣어주면, 해당 시,도에 속해있는 군/구 정보를 가져와준다.
     * @param locationCountyCode
     * @return
     */
    public List<LocationCityDto> getLocationCities(Long locationCountyCode) {

        BooleanBuilder conditions = new BooleanBuilder();

        if (locationCountyCode != null && locationCountyCode >= 0) {
            conditions.and(locationCityTbl.id.locationCountyCode.eq(locationCountyCode));
        }

        var query = queryFactory
                .selectFrom(locationCityTbl)
                .join(locationCityTbl.locationCounty, locationCountyTbl);

        if (conditions.hasValue()) {
            query.where(conditions);
        }

        List<LocationCityTbl> locationCityTblList = query.fetch();

        return locationCityTblList.stream()
                .map(this::convertCityToDto)
                .collect(Collectors.toList());
    }

    /**
     * location_city_tbl 의 구/군 코드를 파라미터로 넣어주면, 해당 구/군에 속해있는 읍/면/동 정보를 가져와준다.
     * @param locationCityCode
     * @return
     */
    public List<LocationDistrictDto> getLocationDistrict(Long locationCityCode) {

        BooleanBuilder conditions = new BooleanBuilder();

        if (locationCityCode != null && locationCityCode >= 0) {
            conditions.and(locationDistrictTbl.id.locationCityCode.eq(locationCityCode));
        }

        var query = queryFactory
                .select(locationDistrictTbl)
                .from(locationDistrictTbl)
                .join(locationDistrictTbl.locationCity, locationCityTbl)
                .join(locationCityTbl.locationCounty, locationCountyTbl);


        if (conditions.hasValue()) {
            query.where(conditions);
        }

        List<LocationDistrictTbl> locationDistricts = query.fetch();

        return locationDistricts.stream()
                .map(this::convertDistrictToDto)
                .collect(Collectors.toList());
    }
}
