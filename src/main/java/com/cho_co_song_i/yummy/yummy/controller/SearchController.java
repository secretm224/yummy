package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/testo")
    public void test() {
        System.out.println("??");
    }

    @GetMapping("/test")
    public CompletableFuture<List<SearchStoreDto>> testSearch(
            @RequestParam("index") String index,
            @RequestParam("field") String field,
            @RequestParam("query") String query
    ) {
        System.out.println("호출됨!");
        System.out.println(index);
        System.out.println(field);
        System.out.println(query);

        return searchService.searchDocuments(index, field, query);
    }
}