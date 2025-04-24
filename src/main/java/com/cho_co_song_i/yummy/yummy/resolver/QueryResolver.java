package com.cho_co_song_i.yummy.yummy.resolver;

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



    /*
        header 추가
            {
                "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiLtirjrn7ztlIQiLCJpYXQiOjE3NDQ2MTEzNTcsImF1dGgiOiJVU0VSIn0.b8NvXWTAYTCswEefXzucvOI_3k-tAVbRyA_Pnye__dQ"
            }
     */

    /*
     query {
            SearchStoreAllData {
                address
                lat
                lng
                locationCity
                locationCounty
                locationDistrict
                majorType
                name
                recommendNames
                seq
                subType
                timestamp
                type
                zeroPossible
            }
        }
     */
    @QueryMapping(name = "SearchStoreAllData")
    public CompletableFuture<List<SearchStoreDto>> getSearchAllStores() {
        String store_index = env.getProperty("spring.elasticsearch.index.store");
        if (store_index != null && !store_index.isEmpty()) {
            return _searchService.getSearchAllStores(store_index);
        }
        return CompletableFuture.completedFuture(List.of());
    }

    /*
        단일 상점 조회
        query{
          SearchStoreName(SearchStoreName: "맘스터치"){
            address
            lat
            lng
            locationCity
            locationCounty
            locationDistrict
            majorType
            name
            recommendNames
            seq
            subType
            timestamp
            type
            zeroPossible
          }
        }
        여러개의 지점 조회
        query {
          store1:SearchStoreName(SearchStoreName: "별양집") {
             address
            lat
            lng
            locationCity
            locationCounty
            locationDistrict
            majorType
            name
            recommendNames
            seq
            subType
            type
            zeroPossible
          }
          store2:SearchStoreName(SearchStoreName: "모쿠") {
            address
            lat
            lng
            locationCity
            locationCounty
            locationDistrict
            majorType
            name
            recommendNames
            seq
            subType
            type
            zeroPossible
          }
          store3:SearchStoreName(SearchStoreName: "소코아") {
           address
            lat
            lng
            locationCity
            locationCounty
            locationDistrict
            majorType
            name
            recommendNames
            seq
            subType
            timestamp
            type
            zeroPossible
          }
        }

     */
    @QueryMapping(name = "SearchStoreName")
    public CompletableFuture<SearchStoreDto> GetSearchStoreName(@Argument("SearchStoreName")String SearchStoreName){
        String store_index = env.getProperty("spring.elasticsearch.index.store");
        return _searchService.getStoreByName(store_index,SearchStoreName)
                                 .thenApply(optional -> optional.orElse(null));
    }

    /*
        query {
            SearchStoreList(page: 2, size: 5) {
                seq
                address
                lat
                lng
                locationCity
                locationCounty
                locationDistrict
                majorType
                name
                recommendNames
                subType
                timestamp
                type
                zeroPossible
            }
        }
     */
    @QueryMapping(name = "SearchStoreList")
    public CompletableFuture<List<SearchStoreDto>> searchStoreList(
            @Argument("page") int page,
            @Argument("size") int size
    ) {
        String index = env.getProperty("spring.elasticsearch.index.store");
        return _searchService.getStoresByPage(index, page, size);
    }

}
