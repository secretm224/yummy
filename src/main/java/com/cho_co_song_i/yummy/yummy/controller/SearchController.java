package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.search.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.search.AutoCompleteResDto;
import com.cho_co_song_i.yummy.yummy.dto.search.TotalSearchDto;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Value("${spring.elasticsearch.index.auto-complete}")
    private String autoKeywordIndex;

    @GetMapping("searchStore")
    public CompletableFuture<ResponseEntity<List<SearchStoreDto>>> findStoresBoundary(
            @RequestParam(value = "minLat", required = true) double minLat,
            @RequestParam(value = "maxLat", required = true) double maxLat,
            @RequestParam(value = "minLon", required = true) double minLon,
            @RequestParam(value = "maxLon", required = true) double maxLon,
            @RequestParam(value = "zoom", required = true) int zoom,
            @RequestParam(value = "showOnlyZeroPay", required = true) boolean showOnlyZeroPay
    ) {
        return searchService.findSearchStoresBoundary(storeIndex, minLat, maxLat, minLon, maxLon, zoom, showOnlyZeroPay)
                .thenApply(ResponseEntity::ok);
    }

    @Deprecated
    @GetMapping("totalSearchOld")
    public ResponseEntity<List<SearchStoreDto>> findTotalSearch(
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "selectMajor", required = false, defaultValue = "0") int selectMajor,
            @RequestParam(value = "selectSub", required = false, defaultValue = "0") int selectSub,
            @RequestParam(value = "zeroPossible", required = false, defaultValue = "false") boolean zeroPossible
    ) throws Exception {
        return ResponseEntity.ok(searchService.findTotalSearchDatas(storeIndex, searchText, selectMajor, selectSub, zeroPossible));
    }

    @GetMapping("totalSearch")
    public CompletableFuture<ResponseEntity<List<TotalSearchDto>>> findTotalSearchData(
            @RequestParam(value = "searchText", required = true) String searchText,
            @RequestParam(value = "zeroPossible", required = true) boolean zeroPossible,
            @RequestParam(value = "startIdx", required = true) int startIdx,
            @RequestParam(value = "pageCnt", required = true) int pageCnt
    ) {
        return searchService.findTotalsearch(storeIndex, searchText, zeroPossible, startIdx, pageCnt)
                .thenApply(ResponseEntity::ok);
    }


    @GetMapping("autoKeyword")
    public CompletableFuture<ResponseEntity<List<AutoCompleteResDto>>> findAutoKeyword(
            @RequestParam(value = "searchText", required = false) String searchText
    ) {
        return searchService.findAutoSearchKeyword(autoKeywordIndex, searchText)
                .thenApply(ResponseEntity::ok);
    }
}