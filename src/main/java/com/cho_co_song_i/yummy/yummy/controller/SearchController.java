package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.search.*;
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
    
    @GetMapping("searchStore")
    public CompletableFuture<ResponseEntity<List<SearchStoreDto>>> findStoresBoundary(
            @RequestParam(value = "minLat", required = true) double minLat,
            @RequestParam(value = "maxLat", required = true) double maxLat,
            @RequestParam(value = "minLon", required = true) double minLon,
            @RequestParam(value = "maxLon", required = true) double maxLon,
            @RequestParam(value = "zoom", required = true) int zoom,
            @RequestParam(value = "showOnlyZeroPay", required = true) boolean showOnlyZeroPay
    ) {
        return searchService.findSearchStoresBoundary(minLat, maxLat, minLon, maxLon, zoom, showOnlyZeroPay)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("totalSearch")
    public CompletableFuture<ResponseEntity<TotalSearchDto>> findTotalSearchData(
            @RequestParam(value = "searchText", required = true) String searchText,
            @RequestParam(value = "zeroPossible", required = true) boolean zeroPossible,
            @RequestParam(value = "startIdx", required = true) int startIdx,
            @RequestParam(value = "pageCnt", required = true) int pageCnt
    ) {
        return searchService.findTotalsearch(searchText, zeroPossible, startIdx, pageCnt)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("autoKeyword")
    public CompletableFuture<ResponseEntity<List<AutoCompleteResDto>>> findAutoKeyword(
            @RequestParam(value = "searchText", required = false) String searchText
    ) {
        return searchService.findAutoSearchKeyword(searchText)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("searchSubway")
    public CompletableFuture<ResponseEntity<List<SubwayInfoDto>>> findSubwayInfo(
            @RequestParam(value = "minLat", required = true) double minLat,
            @RequestParam(value = "maxLat", required = true) double maxLat,
            @RequestParam(value = "minLon", required = true) double minLon,
            @RequestParam(value = "maxLon", required = true) double maxLon,
            @RequestParam(value = "zoom", required = true) int zoom
    ) {
        return searchService.findSubwayInfoSearch(minLat, maxLat, minLon, maxLon, zoom)
                .thenApply(ResponseEntity::ok);
    }
}