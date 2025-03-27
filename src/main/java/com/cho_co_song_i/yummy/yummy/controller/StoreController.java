package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.LocationCountyDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreDto;
import com.cho_co_song_i.yummy.yummy.service.LocationService;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;
    private final LocationService locationService;

    public StoreController(StoreService storeService, LocationService locationService) {
        this.storeService = storeService;
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<StoreDto>> getAllStores() {
        List<StoreDto> stores = storeService.getAllStores();
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable Long id) {
        return storeService.getStoreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto dto) {
        StoreDto createdStore = storeService.createStore(dto);
        return ResponseEntity.ok(createdStore);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDto> updateStore(@PathVariable Long id, @RequestBody StoreDto dto) {
        StoreDto updatedStore = storeService.updateStore(id, dto);
        if (updatedStore != null) {
            return ResponseEntity.ok(updatedStore);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/addStore")
    @Transactional
    public ResponseEntity<String> addStore(@RequestBody AddStoreDto addStoreDto) {

        /* 현재 시각 */
        Instant nowInstant = Instant.now();
        Date now = Date.from(nowInstant);

        try {

            Long storeSeq = storeService.addStore(addStoreDto, now);
            locationService.addStoreLocation(addStoreDto, storeSeq, now);

            /* 비플페이 등록 업체라면 */
            if (addStoreDto.getIsBeefulPay()) {
                locationService.addZeroPossibleMarket(addStoreDto, storeSeq, now);
            }

            /* 음식점-타입 데이터 */
            locationService.addStoreTypeLink(addStoreDto, storeSeq, now);

            return ResponseEntity.ok("Success");
        } catch(Exception e) {
            log.error("[Error][StoreController->addStore] {}", e.getMessage());
            return ResponseEntity.status(500).body("[Error][StoreController->addStore] " + e.getMessage());
        }
    }

//    public ResponseEntity<List<LocationCountyDto>> getLocationCounty() {
//        storeService.g
//    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
//        storeService.deleteStore(id);
//        return ResponseEntity.noContent().build();
//    }
}