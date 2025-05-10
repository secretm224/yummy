package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @Value("${spring.elasticsearch.index.store}")
    private String storeIndex;

    @GetMapping("/test")
    public CompletableFuture<List<SearchStoreDto>> testSearch(
            @RequestParam("index") String index,
            @RequestParam("field") String field,
            @RequestParam("query") String query
    ) {
        return searchService.searchDocuments(index, field, query);
    }

    @GetMapping("allData")
    public CompletableFuture<List<SearchStoreDto>> getAllStores() {
        return searchService.getSearchAllStores(storeIndex);
    }

    @GetMapping("totalSearch")
    public CompletableFuture<List<SearchStoreDto>> getTotalSearch(
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "selectMajor", required = false, defaultValue = "0") int selectMajor,
            @RequestParam(value = "selectSub", required = false, defaultValue = "0") int selectSub,
            @RequestParam(value = "zeroPossible", required = false, defaultValue = "false") boolean zeroPossible
    ) {
        return searchService.getTotalSearchDatas(storeIndex, searchText, selectMajor, selectSub, zeroPossible);
    }
}