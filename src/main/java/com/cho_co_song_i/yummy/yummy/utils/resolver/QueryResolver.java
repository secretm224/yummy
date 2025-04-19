package com.cho_co_song_i.yummy.yummy.utils.resolver;

import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class QueryResolver {

    @Autowired
    private SearchService _searchService;

    @Autowired
    private Environment env;

    @QueryMapping
    public String hello() {
        return "안녕하세요, GraphQL!";
    }

    @QueryMapping
    public String helloWithName(@Argument("name") String name) {
        return "안녕하세요, " + (name != null ? name : "손님") + "!";
    }

    @QueryMapping(name = "SearchStoreAllData")
    public CompletableFuture<List<SearchStoreDto>> getSearchAllStores() {
        String store_index = env.getProperty("spring.elasticsearch.index.store");
        if (store_index != null && !store_index.isEmpty()) {
            return _searchService.getSearchAllStores(store_index);
        }
        return CompletableFuture.completedFuture(List.of());
    }

//    @GetMapping("allData")
//    public CompletableFuture<List<SearchStoreDto>> getAllStores() {
//        return searchService.getSearchAllStores(storeIndex);
//    }




}
