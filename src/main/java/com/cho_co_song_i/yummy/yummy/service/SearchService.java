package com.cho_co_song_i.yummy.yummy.service;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final ElasticsearchAsyncClient searchAsyncClient;

    public SearchService(ElasticsearchAsyncClient searchAsyncClient) {
        this.searchAsyncClient = searchAsyncClient;
    }

    public CompletableFuture<List<SearchStoreDto>> searchDocuments(String index, String field, String query) {

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(index)
                .query(q -> q.match(m -> m
                        .field(field)
                        .query(query)
                ))
                .build();

        return searchAsyncClient.search(searchRequest, SearchStoreDto.class)
                .thenApply(resp -> resp.hits().hits().stream()
                        .map(Hit::source)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }
}