package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeMajorDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeSubDto;
import com.cho_co_song_i.yummy.yummy.service.LocationService;
import com.cho_co_song_i.yummy.yummy.service.RedisService;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @GetMapping("/storyquery")
    public JsonNode UpdateStoreDetail(@RequestParam(value = "storeQuery", required = false) String storeQuery){
        return storeService.UpdateStoreDetail(storeQuery);
    }

    @GetMapping("/UpdateDetailInfo")
    public ResponseEntity<StoreDto> UpdateStoreDetail(@RequestParam(value = "id", required = false) long id,
                                                      @RequestParam(value = "tel", required = false) String tel,
                                                      @RequestParam(value = "url", required = false) String url){
        if(id <=0){
            return ResponseEntity.badRequest().build();
        }

        Optional<StoreDto> optionalStore = storeService.getStoreById(id);
        if (optionalStore.isEmpty()) {
            return ResponseEntity.notFound().build(); // id로 Store를 못찾으면 404 리턴
        }

        StoreDto storeDto = optionalStore.get();
        storeDto.setTel(tel);
        storeDto.setUrl(url);
        storeDto.setChgId("Store>UpdateStoreDetail");

       StoreDto update_store = storeService.updateStore(id,storeDto);
        return ResponseEntity.ok(update_store);
    }

}