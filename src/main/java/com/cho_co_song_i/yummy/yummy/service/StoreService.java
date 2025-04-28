package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.Store;
import com.cho_co_song_i.yummy.yummy.entity.StoreTypeMajor;
import com.cho_co_song_i.yummy.yummy.entity.StoreTypeSub;
import com.cho_co_song_i.yummy.yummy.repository.StoreRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.cho_co_song_i.yummy.yummy.entity.QStoreTypeMajor.storeTypeMajor;
import static com.cho_co_song_i.yummy.yummy.entity.QStoreTypeSub.storeTypeSub;

@Service
@Slf4j
public class StoreService {

    @PersistenceContext
    private EntityManager entityManager;

    private final JPAQueryFactory queryFactory;
    private final LocationService locationService;
    private final StoreRepository storeRepository;
    private final RedisService redisService;
    //commit test
    //commit test2
    /* Redis Cache 관련 필드 */
    @Value("${spring.redis.category_main}")
    private String categoryMain;
    @Value("${spring.redis.category_sub}")
    private String categorySub;

    public StoreService(StoreRepository storeRepository,  LocationService locationService,
                        JPAQueryFactory queryFactory, RedisService redisService) {
        this.storeRepository = storeRepository;
        this.locationService = locationService;
        this.queryFactory = queryFactory;
        this.redisService = redisService;
    }

    // Entity -> DTO 변환
    private StoreDto convertToDto(Store store) {
        return new StoreDto(
                store.getSeq(),
                store.getName(),
                store.getType(),
                store.getUseYn(),
                store.getRegDt(),
                store.getRegId(),
                store.getChgDt(),
                store.getChgId()
        );
    }

    // DTO -> Entity 변환
    private Store convertToEntity(StoreDto dto) {
        Store store = new Store();
        store.setName(dto.getName());
        store.setType(dto.getType());
        store.setUseYn(dto.getUseYn());
        store.setRegDt(dto.getRegDt());
        store.setRegId(dto.getRegId());
        store.setChgDt(dto.getChgDt());
        store.setChgId(dto.getChgId());
        return store;
    }

    public List<StoreDto> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<StoreDto> getStoreById(Long id) {
        return storeRepository.findById(id)
                .map(this::convertToDto);
    }

    public StoreDto createStore(StoreDto dto) {
        Store store = convertToEntity(dto);
        store = storeRepository.save(store);
        return convertToDto(store);
    }

    public StoreDto updateStore(Long id, StoreDto dto) {
        Optional<Store> optionalStore = storeRepository.findById(id);
        if (optionalStore.isPresent()) {
            Store store = optionalStore.get();
            store.setName(dto.getName());
            store.setType(dto.getType());
            store.setUseYn(dto.getUseYn());
            store.setRegDt(dto.getRegDt());
            store.setRegId(dto.getRegId());
            store.setChgDt(dto.getChgDt());
            store.setChgId(dto.getChgId());
            store = storeRepository.save(store);
            return convertToDto(store);
        } else {
            return null; // 혹은 예외 처리
        }
    }

    /* Entity -> DTO 변환 (StorTypeMajor) */
    private StoreTypeMajorDto convertTypeMajorToDto(StoreTypeMajor storeTypeMajor) {
        return new StoreTypeMajorDto(
                storeTypeMajor.getMajorType(),
                storeTypeMajor.getTypeName()
        );
    }

    /* Entity -> DTO 변환 (StorTypeSub) */
    private StoreTypeSubDto convertTypeSubToDto(StoreTypeSub storeTypeSub) {
        return new StoreTypeSubDto(
                storeTypeSub.getSubType(),
                storeTypeSub.getMajorType(),
                storeTypeSub.getTypeName()
        );
    }

    /**
     * Store 객체를 디비에 저장해주는 함수
     * @param addStoreDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean addStore(AddStoreDto addStoreDto) {

        /* 현재 시각 */
        Instant nowInstant = Instant.now();
        Date now = Date.from(nowInstant);

        try{

            if (addStoreDto == null) {
                throw new IllegalArgumentException("[Error][StoreService->addStore] AddStoreDto object is null.");
            }
            if (addStoreDto.getName() == null || addStoreDto.getName().isEmpty()) {
                throw new IllegalArgumentException("[Error][StoreService->addStore] The store name is missing.");
            }
            if (addStoreDto.getType() == null || addStoreDto.getType().isEmpty()) {
                throw new IllegalArgumentException("[Error][StoreService->addStore] The store type is missing.");
            }

            /* Store 객체*/
            Store newStore = new Store();
            newStore.setName(addStoreDto.getName());
            newStore.setType(addStoreDto.getType());
            newStore.setUseYn('Y');
            newStore.setRegDt(now);
            newStore.setRegId("system");
            newStore.setTel(addStoreDto.getTel());
            newStore.setUrl(addStoreDto.getUrl());
            newStore.markAsNew();

            Long newStoreSeq = storeRepository.save(newStore).getSeq();
            locationService.addStoreLocationInfoTbl(addStoreDto, newStoreSeq, now);


            /* 비플페이 등록 업체라면 */
            if (addStoreDto.getIsBeefulPay()) {
                locationService.addZeroPossibleMarket(addStoreDto, newStoreSeq, now);
            }

            /* 음식점-타입 데이터 */
            locationService.addStoreTypeLinkTbl(addStoreDto, newStoreSeq, now);

            return true;
        } catch(Exception e) {
            log.error("[Error][StoreService->addStore] {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    /**
     * 음식점 대분류 데이터를 가져와주는 함수
     * @return
     */
    public List<StoreTypeMajorDto> getStoreTypeMajors() {

        List<StoreTypeMajorDto> storeMajors = new ArrayList<>();

        try {
            storeMajors = redisService.getValue(categoryMain, new TypeReference<List<StoreTypeMajorDto>>() {});
        } catch(Exception e) {
            log.error("[Error][StoreService->getStoreTypeMajors] {}", e.getMessage(), e);
        }

        if (storeMajors == null || storeMajors.isEmpty()) {

            var query = queryFactory
                    .select(storeTypeMajor)
                    .from(storeTypeMajor);

            try {
                List<StoreTypeMajor> storeMajorsDb = query.fetch();

                return storeMajorsDb.stream()
                        .map(this::convertTypeMajorToDto)
                        .collect(Collectors.toList());

            } catch(Exception e) {
                log.error("[Error][StoreService->getStoreTypeMajors] {}", e.getMessage(), e);
                return Collections.emptyList();
            }

        } else {
            return storeMajors;
        }
    }

    public List<StoreTypeSubDto> getStoreTypeSubs(Long majorType) {

        if (majorType == null || majorType <= 0) {
            log.error("[Error][StoreService->getStoreTypeSubs] `majorType` must be at least 1 natural number.");
            return Collections.emptyList();
        }

        List<StoreTypeSubDto> storeTypeSubs = new ArrayList<>();

        try {
            String storeSubKey = String.format("%s:%s", categorySub, majorType);
            storeTypeSubs = redisService.getValue(storeSubKey, new TypeReference<List<StoreTypeSubDto>>() {});
        } catch(Exception e) {
            log.error("[Error][StoreService->getStoreTypeSubs] {}", e.getMessage(), e);
        }

        if (storeTypeSubs == null || storeTypeSubs.isEmpty()) {
            /* Redis 에서 데이터를 못가져오거나 데이터가 존재하지 않을 경우 */
            var query = queryFactory
                    .select(storeTypeSub)
                    .from(storeTypeSub)
                    .join(storeTypeSub.storeTypeMajor, storeTypeMajor)
                    .where(
                            new BooleanBuilder()
                                    .and(storeTypeSub.majorType.eq(majorType))
                    );

            try {
                List<StoreTypeSub> storeTypeSubsDb = query.fetch();

                return storeTypeSubsDb.stream()
                        .map(this::convertTypeSubToDto)
                        .collect(Collectors.toList());

            } catch(Exception e) {
                log.error("[Error][StoreService->getStoreTypeSubs] {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        } else {
            return storeTypeSubs;
        }
    }

}