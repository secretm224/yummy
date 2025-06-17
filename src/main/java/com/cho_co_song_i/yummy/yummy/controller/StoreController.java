package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeMajorDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeSubDto;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;
    private final RedisAdapter redisAdapter;

//    @GetMapping
//    public ResponseEntity<List<StoreDto>> findAllStores() {
//        List<StoreDto> stores = storeService.findAllStores();
//        return ResponseEntity.ok(stores);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<StoreDto> findStoreById(@PathVariable Long id) {
//        return storeService.findStoreById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto dto) {
//        StoreDto createdStore = storeService.createStore(dto);
//        return ResponseEntity.ok(createdStore);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<StoreDto> modifyStore(@PathVariable Long id, @RequestBody StoreDto dto) {
//        StoreDto updatedStore = storeService.modifyStore(id, dto);
//        if (updatedStore != null) {
//            return ResponseEntity.ok(updatedStore);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

//    @PostMapping("/addStore")
//    public ResponseEntity<Boolean> inputStore(@RequestBody AddStoreDto addStoreDto) throws Exception {
//        Boolean addStore = storeService.isAddedStore(addStoreDto);
//        return ResponseEntity.ok(addStore);
//    }
//
//    @GetMapping("/getTypeMajor")
//    public ResponseEntity<List<StoreTypeMajorDto>> findStoreTypeMajor() throws Exception {
//        List<StoreTypeMajorDto> storeTypeMajors = storeService.findStoreTypeMajors();
//        return ResponseEntity.ok(storeTypeMajors);
//    }
//
//    @GetMapping("/getTypeSub")
//    public ResponseEntity<List<StoreTypeSubDto>> findStoreTypeSub(
//            @RequestParam(value = "majorType", required = false) Long majorType
//    ) throws Exception {
//        List<StoreTypeSubDto> storeTypeMajors = storeService.findStoreTypeSubs(majorType);
//        return ResponseEntity.ok(storeTypeMajors);
//    }
//
//    @GetMapping("/redisTest")
//    public ResponseEntity<Boolean> redisTest() {
//
//        System.out.println(redisAdapter.get("categories:main"));
//        return ResponseEntity.ok(true);
//    }

//    @GetMapping("/StoreDetailQuery")
//    public Optional<JsonNode> inputDetailQuery(@RequestParam(value = "storeName", required = true) String storeName,
//                                               @RequestParam(value = "lng", required = false) BigDecimal lng,
//                                               @RequestParam(value = "lat", required = false) BigDecimal lat
//    ) {
//        return storeService.inputDetailQuery(storeName, lng, lat);
//    }

//    @GetMapping("/updateStoreDetailInfo")
//    public ResponseEntity<StoreDto> modifySingleStoreDetail(@RequestParam(value = "id", required = false) long id,
//                                                         @RequestParam(value = "tel", required = false) String tel,
//                                                         @RequestParam(value = "url", required = false) String url) {
//        StoreDto updated = storeService.modifySingleStoreDetail(id, tel, url);
//        return ResponseEntity.ok(updated);
//    }
//    @GetMapping("/updateAllStoreInfobyKaKao")
//    public ResponseEntity<Optional<JsonNode>> modifyAllStoreInfobyKaKao(){
//        Optional<JsonNode> result = storeService.modifyAllStoreDetail();
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/updateStoreInfobyKaKao")
//    public ResponseEntity<Optional<JsonNode>> modifyEmptyStoreInfobyKaKao(){
//        Optional<JsonNode> result = storeService.modifyEmptyStoreDetail();
//        return ResponseEntity.ok(result);
//    }

    /**
     * 상점등록 메소드
     * @param storeName
     * @param page
     * @param size
     * @param pLat
     * @param pLng
     * @param zeroYn
     * @return
     */
    @GetMapping("/inputNewStore")
    public ResponseEntity<PublicStatus> inputNewStore(
            @RequestParam(value = "storeName", required = true) String storeName,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "pLat", required = false) BigDecimal pLat,
            @RequestParam(value = "pLng", required = false) BigDecimal pLng,
            @RequestParam(value = "zeroYn", required = true) Boolean zeroYn
            ) {
        return ResponseEntity.ok(storeService.inputNewStore(storeName, page, size, pLat, pLng,  zeroYn));
    }

    @GetMapping("/updateExistsStore")
    public ResponseEntity<PublicStatus> modifyExistsStoreDatas() {
        return ResponseEntity.ok(storeService.modifyExistsStores());
    }
}