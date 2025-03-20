package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.configuration.ObjectMapperConfig;
import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {
    private final RestHighLevelClient restHighLevelClient;
    private final ObjectMapper objectMapper;

    public SearchService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
        this.objectMapper = ObjectMapperConfig.createObjectMapper();
    }

    public List<SearchStoreDto> searchStores(String indexName, String field, String value) {
        List<SearchStoreDto> stores = new ArrayList<>();

        /* 검색 요청 설정*/
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchQuery(field, value))
                .size(1000);

        searchRequest.source(sourceBuilder);

        try {

            SearchResponse resp = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : resp.getHits().getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SearchStoreDto storeDto = objectMapper.readValue(sourceAsString, SearchStoreDto.class);
                stores.add(storeDto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stores;
    }
}
