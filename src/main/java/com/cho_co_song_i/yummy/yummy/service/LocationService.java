package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.repository.LocationCountyRepository;
import com.cho_co_song_i.yummy.yummy.repository.StoreLocationInfoRepository;
import com.cho_co_song_i.yummy.yummy.repository.StoreTypeLinkRepository;
import com.cho_co_song_i.yummy.yummy.repository.ZeroPossibleMarketRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.cho_co_song_i.yummy.yummy.entity.QLocationCountyTbl.locationCountyTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QLocationCityTbl.locationCityTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QLocationDistrictTbl.locationDistrictTbl;

@Service
@Slf4j
public class LocationService {

    @PersistenceContext
    private EntityManager entityManager;

    private final JPAQueryFactory queryFactory;

    private final LocationCountyRepository locationRepository;
    private final ZeroPossibleMarketRepository zeroPossibleMarketRepository;
    private final StoreLocationInfoRepository storeLocationInfoRepository;
    private final StoreTypeLinkRepository storeTypeLinkRepository;
    private final RedisService redisService;


    /* Redis Cache 관련 필드 */
    @Value("${spring.redis.location_county}")
    private String locationCounty;
    @Value("${spring.redis.location_city}")
    private String locationCity;
    @Value("${spring.redis.location_district}")
    private String locationDistrict;

    public LocationService(LocationCountyRepository locationRepository, JPAQueryFactory queryFactory,
                           ZeroPossibleMarketRepository zeroPossibleMarketRepository, StoreLocationInfoRepository storeLocationInfoRepository,
                           StoreTypeLinkRepository storeTypeLinkRepository, RedisService redisService) {
        this.locationRepository = locationRepository;
        this.queryFactory = queryFactory;
        this.zeroPossibleMarketRepository = zeroPossibleMarketRepository;
        this.storeLocationInfoRepository = storeLocationInfoRepository;
        this.storeTypeLinkRepository = storeTypeLinkRepository;
        this.redisService = redisService;
    }

    /* ** Fetch Join / Join 비교 ** -> JPA 를 사용하면서 굉장히 중요한 부분  */
    public void getTestDtos(Long locationCountyCode) {

        BooleanBuilder conditions = new BooleanBuilder();

        if (locationCountyCode != null && locationCountyCode >= 0) {
            conditions.and(locationCityTbl.id.locationCountyCode.eq(locationCountyCode));
        }

        var query = queryFactory
                .selectFrom(locationCityTbl)
                .join(locationCityTbl.locationCounty, locationCountyTbl)
                .fetchJoin(); /* 해당 부분을 없애고 실행하면 쿼리가 N+1 번 실행됨 */

        try {
            List<LocationCityTbl> locationCityTblList = query.fetch();

            /* Fetch join 을 수행하면 문제가 발생하지 않음 */
            /* 🔥 N+1 문제를 발생시키는 코드 (for-each 로 연관 객체 접근) */
            for (LocationCityTbl city : locationCityTblList) {
                String countyName = city.getLocationCounty().getLocationCounty();  // 🔥 Fetch join 하지 않을 시, N번 추가 쿼리 발생
                System.out.println("LocationCity: " + city.getLocationCity() + ", County: " + countyName);
            }

        } catch(Exception e) {
            log.error("[Error][LocationService->getLocationCities] {}", e.getMessage(), e);
        }

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
                Objects.requireNonNull(locationCityTbl.getId()).getLocationCityCode(),
                locationCityTbl.getId().getLocationCountyCode(),
                locationCityTbl.getLocationCity()
        );
    }

    /* Entity -> DTO 변환 (LocationDistrictTbl) */
    private LocationDistrictDto convertDistrictToDto(LocationDistrictTbl locationDistrictTbl) {
        return new LocationDistrictDto(
                Objects.requireNonNull(locationDistrictTbl.getId()).getLocationDistrictCode(),
                locationDistrictTbl.getId().getLocationCityCode(),
                locationDistrictTbl.getId().getLocationCountyCode(),
                locationDistrictTbl.getLocationDistrict()
        );
    }

    /*
        LocationCountyTbl 객체 리스트를 가져와준다.
        -> 먼저 Redis 를 바라봐주고, 데이터가 존재하지 않거나, Redis 에 문제가 생긴 경우 db에서 데이터를 가져와준다.
    */
    public List<LocationCountyDto> getAllLocationCounty() {
        try {

            List<LocationCountyDto> locationCountyList =
                    redisService.getValue(locationCounty, new TypeReference<List<LocationCountyDto>>() {});

            if (locationCountyList.isEmpty()) {
                /* Redis 에서 데이터를 받아오지 못한 경우 */
                List<LocationCountyTbl> locationCountyListDb = locationRepository.findAll();

                return locationCountyListDb.stream()
                    .map(this::convertCountyToDto)
                    .collect(Collectors.toList());

            } else {
                return locationCountyList;
            }

        } catch(Exception e) {
            log.error("[Error][LocationService->getAllLocationCounty] {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * location_county_tbl 의 시,도 코드를 파라미터로 넣어주면, 해당 시,도에 속해있는 군/구 정보를 가져와준다.
     * @param locationCountyCode
     * @return
     */
    public List<LocationCityDto> getLocationCities(Long locationCountyCode) {

        List<LocationCityDto> locationCityList = new ArrayList<>();

        try {
            String locationCityKey = String.format("%s:%s", locationCity, locationCountyCode);
            locationCityList = redisService.getValue(locationCityKey, new TypeReference<List<LocationCityDto>>() {});
        } catch(Exception e) {
            log.error("[Error][LocationService->getLocationCities] {}", e.getMessage(), e);
        }

        if (locationCityList == null || locationCityList.isEmpty()) {
            /* Redis 에서 데이터를 못가져오거나 데이터가 존재하지 않을 경우 */
            BooleanBuilder conditions = new BooleanBuilder();

            if (locationCountyCode != null && locationCountyCode > 0) {
                conditions.and(locationCityTbl.id.locationCountyCode.eq(locationCountyCode));
            }

            var query = queryFactory
                    .selectFrom(locationCityTbl)
                    .join(locationCityTbl.locationCounty, locationCountyTbl);

            if (conditions.hasValue()) {
                query.where(conditions);
            }

            try {
                List<LocationCityTbl> locationCityTblList = query.fetch();

                return locationCityTblList.stream()
                        .map(this::convertCityToDto)
                        .collect(Collectors.toList());

            } catch(Exception e) {
                log.error("[Error][LocationService->getLocationCities] {}", e.getMessage(), e);
                return Collections.emptyList();
            }

        } else {
            return locationCityList;
        }
    }

    /**
     * location_city_tbl 의 구/군 코드를 파라미터로 넣어주면, 해당 구/군에 속해있는 읍/면/동 정보를 가져와준다.
     * @param locationCityCode
     * @return
     */
    public List<LocationDistrictDto> getLocationDistrict(Long locationCityCode) {

        List<LocationDistrictDto> locationDistrictList = new ArrayList<>();

        try {
            String locationDistrictKey = String.format("%s:%s", locationDistrict, locationCityCode);
            locationDistrictList = redisService.getValue(locationDistrictKey, new TypeReference<List<LocationDistrictDto>>() {});
        } catch(Exception e) {
            log.error("[Error][LocationService->getLocationDistrict] {}", e.getMessage(), e);
        }


        if (locationDistrictList == null || locationDistrictList.isEmpty()) {

            BooleanBuilder conditions = new BooleanBuilder();

            if (locationCityCode != null && locationCityCode > 0) {
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

            try {
                List<LocationDistrictTbl> locationDistricts = query.fetch();

                return locationDistricts.stream()
                        .map(this::convertDistrictToDto)
                        .collect(Collectors.toList());

            } catch(Exception e) {
                log.error("[Error][LocationService->getLocationDistrict] {}", e.getMessage(), e);
                return Collections.emptyList();
            }

        } else {
            return locationDistrictList;
        }
    }

    public void addStoreLocationInfoTbl(AddStoreDto addStoreDto, Long storeSeq, Date now) {

        if (storeSeq <= 0) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreLocation] 'storeSeq' data must be at least 1 natural number.");
        }
        if (addStoreDto.getLocationCounty() == null || addStoreDto.getLocationCounty().isEmpty()) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreLocation] The location county name is missing.");
        }
        if (addStoreDto.getLocationCity() == null || addStoreDto.getLocationCity().isEmpty()) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreLocation] The location city name is missing.");
        }
        if (addStoreDto.getLocationDistrict() == null || addStoreDto.getLocationDistrict().isEmpty()) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreLocation] The location district name is missing.");
        }
        if (addStoreDto.getAddress() == null || addStoreDto.getAddress().isEmpty()) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreLocation] The address name is missing.");
        }
        if (addStoreDto.getLat() == null) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreLocation] The lat value is missing.");
        }
        if (addStoreDto.getLng() == null) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreLocation] The lng value is missing.");
        }

        StoreLocationInfoTbl storeLocationInfoTbl = new StoreLocationInfoTbl();
        storeLocationInfoTbl.setSeq(storeSeq);
        storeLocationInfoTbl.setLat(addStoreDto.getLat());
        storeLocationInfoTbl.setLng(addStoreDto.getLng());
        storeLocationInfoTbl.setLocationCounty(addStoreDto.getLocationCounty());
        storeLocationInfoTbl.setLocationCity(addStoreDto.getLocationCity());
        storeLocationInfoTbl.setLocationDistrict(addStoreDto.getLocationDistrict());
        storeLocationInfoTbl.setAddress(addStoreDto.getAddress());
        storeLocationInfoTbl.setRegId("system");
        storeLocationInfoTbl.setRegDt(now);
        storeLocationInfoTbl.markAsNew();

        storeLocationInfoRepository.save(storeLocationInfoTbl);
    }



    public void addZeroPossibleMarket(AddStoreDto addStoreDto, Long storeSeq, Date now) {

        if (storeSeq <= 0) {
            throw new IllegalArgumentException("[Error][LocationService->addZeroPossibleMarket] 'storeSeq' data must be at least 1 natural number.");
        }
        if (addStoreDto.getName() == null || addStoreDto.getName().isEmpty()) {
            throw new IllegalArgumentException("[Error][LocationService->addZeroPossibleMarket] The store name is missing.");
        }

        /* ZeroPossibleMarket 객체 */
        ZeroPossibleMarket zeroPossibleMarket = new ZeroPossibleMarket();
        zeroPossibleMarket.setSeq(storeSeq);
        zeroPossibleMarket.setUseYn('Y');
        zeroPossibleMarket.setName(addStoreDto.getName());
        zeroPossibleMarket.setRegDt(now);
        zeroPossibleMarket.setRegId("system");
        zeroPossibleMarket.markAsNew();

        zeroPossibleMarketRepository.save(zeroPossibleMarket);
    }

    public void addStoreTypeLinkTbl(AddStoreDto addStoreDto, Long storeSeq, Date now) {

        // 일부로 에러를 발생시켜줌
        if (storeSeq <= 0) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreTypeLink] 'storeSeq' data must be at least 1 natural number.");
        }
        if (addStoreDto == null) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreTypeLink] AddStoreDto object is null.");
        }
        if (addStoreDto.getSubType() <= 0) {
            throw new IllegalArgumentException("[Error][LocationService->addStoreTypeLink] 'subType' data must be at least 1 natural number.");
        }

        /* StoreTypeLink 객체 */
        StoreTypeLinkTbl storeTypeLinkTbl = new StoreTypeLinkTbl();
        StoreTypeLinkTblId storeTypeLinkTblId = new StoreTypeLinkTblId(addStoreDto.getSubType(), storeSeq);

        storeTypeLinkTbl.setId(storeTypeLinkTblId);
        storeTypeLinkTbl.setRegDt(now);
        storeTypeLinkTbl.setRegId("system");
        storeTypeLinkTbl.markAsNew();

        storeTypeLinkRepository.save(storeTypeLinkTbl);
    }

}
