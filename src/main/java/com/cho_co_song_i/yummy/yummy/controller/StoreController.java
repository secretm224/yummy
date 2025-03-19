package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.StoreDto;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
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

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
//        storeService.deleteStore(id);
//        return ResponseEntity.noContent().build();
//    }
}