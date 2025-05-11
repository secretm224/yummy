package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@RequestMapping("/search")
@RestController
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    private final SearchService searchService;
    @Value("${spring.elasticsearch.index.store}")
    private String storeIndex;

    @GetMapping("allData")
    public ResponseEntity<List<SearchStoreDto>> getAllStores() {
        try {
            return ResponseEntity.ok(searchService.getSearchAllStores(storeIndex));
        } catch(Exception e) {
            log.error("[Error][SearchController->getAllStores] {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("totalSearch")
    public ResponseEntity<List<SearchStoreDto>> getTotalSearch(
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "selectMajor", required = false, defaultValue = "0") int selectMajor,
            @RequestParam(value = "selectSub", required = false, defaultValue = "0") int selectSub,
            @RequestParam(value = "zeroPossible", required = false, defaultValue = "false") boolean zeroPossible
    ) {
        try {
            return ResponseEntity.ok(searchService.getTotalSearchDatas(storeIndex, searchText, selectMajor, selectSub, zeroPossible));
        } catch(Exception e) {
            log.error("[Error][SearchController->getTotalSearch] {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
}