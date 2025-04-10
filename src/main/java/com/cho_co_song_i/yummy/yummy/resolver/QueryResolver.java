package com.cho_co_song_i.yummy.yummy.resolver;

import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class QueryResolver {

    @Autowired
    private SearchService _searchService;

    @Autowired
    private Environment env;

    @SchemaMapping(typeName = "Query", field = "hello")
    public String getHello() {
        return "안녕하세요, GraphQL!";
    }

    @SchemaMapping(typeName = "Query", field = "helloWithName")
    public String getHelloWithName(@Argument("name") String name) {
        return "안녕하세요, " + (name != null ? name : "손님") + "!";
    }

    @SchemaMapping(typeName = "Query", field = "SearchStoreAllData")
    public CompletableFuture<List<SearchStoreDto>> GetSearchAllStores() {

        CompletableFuture<List<SearchStoreDto>> data = new CompletableFuture<>();
        String store_index = env.getProperty("spring.elasticsearch.index.store");

        if(store_index != null && !store_index.isEmpty()){
            data = _searchService.getSearchAllStores(store_index);
        }

        return data;
    }

//    @GetMapping("allData")
//    public CompletableFuture<List<SearchStoreDto>> getAllStores() {
//        return searchService.getSearchAllStores(storeIndex);
//    }




}
