package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeMajorDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeSubDto;
import com.cho_co_song_i.yummy.yummy.service.LocationService;
import com.cho_co_song_i.yummy.yummy.service.RedisService;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@Slf4j
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;
    private final LocationService locationService;
    private final RedisService redisService;

    public StoreController(StoreService storeService, LocationService locationService, RedisService redisService) {
        this.storeService = storeService;
        this.locationService = locationService;
        this.redisService = redisService;
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
    public ResponseEntity<Boolean> addStore(@RequestBody AddStoreDto addStoreDto) {
        Boolean addStore = storeService.addStore(addStoreDto);
        return ResponseEntity.ok(addStore);
    }

    @GetMapping("/getTypeMajor")
    public ResponseEntity<List<StoreTypeMajorDto>> getStoreTypeMajor() {
        List<StoreTypeMajorDto> storeTypeMajors = storeService.getStoreTypeMajors();
        return ResponseEntity.ok(storeTypeMajors);
    }

    @GetMapping("/getTypeSub")
    public ResponseEntity<List<StoreTypeSubDto>> getStoreTypeSub(
            @RequestParam(value = "majorType", required = false) Long majorType
    ) {
        List<StoreTypeSubDto> storeTypeMajors = storeService.getStoreTypeSubs(majorType);
        return ResponseEntity.ok(storeTypeMajors);
    }

    @GetMapping("/redisTest")
    public ResponseEntity<Boolean> redisTest() {

        System.out.println(redisService.get("categories:main"));
        return ResponseEntity.ok(true);
    }

}