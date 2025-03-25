package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.LocationCityDto;
import com.cho_co_song_i.yummy.yummy.dto.LocationCountyDto;
import com.cho_co_song_i.yummy.yummy.entity.LocationCityTbl;
import com.cho_co_song_i.yummy.yummy.entity.LocationCountyTbl;
import com.cho_co_song_i.yummy.yummy.repository.LocationCountyRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.cho_co_song_i.yummy.yummy.entity.QLocationCountyTbl.locationCountyTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QLocationCityTbl.locationCityTbl;

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

    private LocationCityDto convertCityToDto(LocationCityTbl locationCityTbl) {
        return new LocationCityDto(
                locationCityTbl.getId().getLocationCityCode(),
                locationCityTbl.getId().getLocationCountyCode(),
                locationCityTbl.getLocationCity()
        );
    }

    /* LocationCountyTbl 객체 리스트를 가져와준다. */
    public List<LocationCountyDto> getAllLocationCounty() {
        List<LocationCountyTbl> locationCountyList = locationRepository.findAll();

        return locationCountyList.stream()
                .map(this::convertCountyToDto)
                .collect(Collectors.toList());
    }

    public List<LocationCityDto> getLocationCities(Long locationCountyCode) {

        BooleanBuilder conditions = new BooleanBuilder();

        if (locationCountyCode != null && locationCountyCode >= 0) {
            conditions.and(locationCityTbl.id.locationCountyCode.eq(locationCountyCode));
        }

        var query = queryFactory
                .selectFrom(locationCityTbl)
                .join(locationCityTbl.locationCounty, locationCountyTbl).fetchJoin();

        if (conditions.hasValue()) {
            query.where(conditions);
        }

        List<LocationCityTbl> locationCityTblList = query.fetch();

        return locationCityTblList.stream()
                .map(this::convertCityToDto)
                .collect(Collectors.toList());
    }

}
