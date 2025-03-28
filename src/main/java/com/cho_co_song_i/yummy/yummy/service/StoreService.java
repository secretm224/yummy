package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreDto;
import com.cho_co_song_i.yummy.yummy.entity.Store;
import com.cho_co_song_i.yummy.yummy.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StoreService {

    @PersistenceContext
    private EntityManager entityManager;

    private final LocationService locationService;

    private final StoreRepository storeRepository;
    public StoreService(StoreRepository storeRepository,  LocationService locationService) {
        this.storeRepository = storeRepository;
        this.locationService = locationService;
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
            log.error("[Error][StoreController->addStore] {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }
}