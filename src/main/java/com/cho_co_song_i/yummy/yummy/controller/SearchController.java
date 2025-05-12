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
    public ResponseEntity<List<SearchStoreDto>> findAllStores() throws Exception {
        return ResponseEntity.ok(searchService.findSearchAllStores(storeIndex));
    }

    @GetMapping("totalSearch")
    public ResponseEntity<List<SearchStoreDto>> findTotalSearch(
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "selectMajor", required = false, defaultValue = "0") int selectMajor,
            @RequestParam(value = "selectSub", required = false, defaultValue = "0") int selectSub,
            @RequestParam(value = "zeroPossible", required = false, defaultValue = "false") boolean zeroPossible
    ) throws Exception {
        return ResponseEntity.ok(searchService.findTotalSearchDatas(storeIndex, searchText, selectMajor, selectSub, zeroPossible));
    }
}