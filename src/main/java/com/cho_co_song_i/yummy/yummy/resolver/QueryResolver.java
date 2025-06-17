package com.cho_co_song_i.yummy.yummy.resolver;

import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeMajorDto;
import com.cho_co_song_i.yummy.yummy.dto.StoreTypeSubDto;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class QueryResolver {

    private final SearchService searchService;
    private final StoreService storeService;

    @Value("${spring.elasticsearch.index.store}")
    private String storeIndex;

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
    public List<SearchStoreDto> findSearchAllStores() throws Exception {
        if (!StringUtils.hasText(storeIndex)) {
            return Collections.emptyList();
        }
        return searchService.findSearchAllStores(storeIndex);
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
    public SearchStoreDto GetSearchStoreName(@Argument("SearchStoreName") String storeName) throws Exception {
        if (!StringUtils.hasText(storeName)) {
            log.warn("검색할 매장 이름이 비어 있습니다.");
            return null;
        }
        return searchService.findStoreByName(storeIndex, storeName).orElse(null);
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
    public List<SearchStoreDto> searchStoreList(
            @Argument("page") int page,
            @Argument("size") int size
    ) throws Exception {
        if (!StringUtils.hasText(storeIndex)) {
            return Collections.emptyList();
        }

        return searchService.findStoresByPage(storeIndex, page, size);
    }

//    query{
//        StoreMajorsTypeList{
//            majorType
//                    typeName
//
//        }
//    }
    @QueryMapping(name="StoreMajorsTypeList")
    public List<StoreTypeMajorDto> findStoreMajorsType() throws Exception {

        return storeService.findStoreTypeMajors();
    }
//    query{
//        StoreSubMajorsTypeList(major_code:1){
//            subType
//                    majorType
//            typeName
//        }
//
//    }
//    @QueryMapping(name="StoreSubMajorsTypeList")
//    public List<StoreTypeSubDto>GetStoreSubMajorsType(@Argument("major_code") int major_code) throws Exception {
//        return storeService.findStoreTypeSubs((long)major_code);
//    }

}

