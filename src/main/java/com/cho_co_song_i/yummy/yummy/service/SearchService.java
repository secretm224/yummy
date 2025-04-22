package com.cho_co_song_i.yummy.yummy.service;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {
    private final ElasticsearchAsyncClient searchAsyncClient;

    public SearchService(ElasticsearchAsyncClient searchAsyncClient) {
        this.searchAsyncClient = searchAsyncClient;
    }
   // master merge test
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

    @SuppressWarnings("unchecked")
    private List<FieldValue> convertToFieldValues(Object values) {
        if (values instanceof List<?>) {  /* 리스트 타입 처리 */
            return ((List<?>) values).stream()
                    .map(this::convertSingleValue)
                    .collect(Collectors.toList());
        } else {  /* 단일 값 처리 */
            return List.of(convertSingleValue(values));
        }
    }

    private FieldValue convertSingleValue(Object value) {
        if (value instanceof String) {
            return FieldValue.of((String) value);
        } else if (value instanceof Integer) {
            return FieldValue.of((Integer) value);
        } else if (value instanceof Long) {
            return FieldValue.of((Long) value);
        } else if (value instanceof Double) {
            return FieldValue.of((Double) value);
        } else if (value instanceof Boolean) {
            return FieldValue.of((Boolean) value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }

    /**
     * 지도 데이터 -> Elasticsearch 에서 모든 음식점 데이터를 가져와준다.
     * @return 모든 지도 데이터 객체 리스트 -> 향후에 로직 수정 필요
     */
    public CompletableFuture<List<SearchStoreDto>> getSearchAllStores(String indexName) {

        try {

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .size(10000)
                    .query(q -> q
                            .matchAll(m -> m)));

            return searchAsyncClient.search(searchRequest, SearchStoreDto.class)
                    .thenApply(resp -> resp.hits().hits().stream()
                            .map(Hit::source)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()))
                    .exceptionally(ex -> {
                        log.error("[Error][SearchService -> getSearchAllStores] {}", ex.getMessage());
                        return List.of();
                    });

        } catch (Exception e) {
            log.error("[Error][SearchService -> getSearchAllStores] {}", e.getMessage());
            return CompletableFuture.completedFuture(List.of());
        }
    }

    /** secretm test
     * 가게 이름으로 단건 조회
     *
     * @param indexName Elasticsearch 인덱스명
     * @param storeName 조회할 가게 이름 (match 질의)
     * @return 이름과 유사한 문서 중 첫 번째를 Optional로 감싸서 반환
     */
    public CompletableFuture<Optional<SearchStoreDto>> getStoreByName(
            String indexName,
            String storeName
    ) {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .size(1)
                .query(q -> q
                        .match(m -> m
                                .field("name")
                                .query(FieldValue.of(storeName))// 을 쓰면 variant 가 자동 지정됩니다
                                //.query(FieldValue.of(fv -> fv.stringValue(storeName)))
                        )
                )
        );

        return searchAsyncClient.search(searchRequest, SearchStoreDto.class)
                .thenApply(resp ->
                        resp.hits().hits().stream()
                                .map(hit -> hit.source())
                                .filter(Objects::nonNull)
                                .findFirst()
                )
                .exceptionally(ex -> {
                    log.error("[Error][SearchService -> getStoreByName] {}", ex.getMessage());
                    return Optional.empty();
                });
    }

    /**
     * Elasticsearch 에서 모든 도큐먼트를 페이징 조회
     *
     * @param indexName 조회할 인덱스
     * @param page      1부터 시작하는 페이지 번호
     * @param size      페이지당 항목 수
     * @return CompletableFuture<List<SearchStoreDto>>
     */
    public CompletableFuture<List<SearchStoreDto>> getStoresByPage(
            String indexName,
            int page,
            int size
    ) {
        int from = (page - 1) * size;

        SearchRequest req = SearchRequest.of(s -> s
                .index(indexName)
                .from(from)       // 건너뛸 도큐먼트 수
                .size(size)
                .sort(so -> so
                        .field(f -> f
                                .field("seq")
                                .order(SortOrder.Asc)
                        )
                )// 한 번에 가져올 도큐먼트 수
                .query(q -> q
                        .matchAll(ma -> ma)    // 전체 조회, 필요에 따라 필터나 match 쿼리로 대체 가능
                )
        );

        return searchAsyncClient.search(req, SearchStoreDto.class)
                .thenApply(resp ->
                        resp.hits().hits().stream()
                                .map(Hit::source)
                                .filter(Objects::nonNull)
                                .toList()
                );
    }

    /**
     * 통함검색 알고리즘
     * @return
     */
    public CompletableFuture<List<SearchStoreDto>> getTotalSearchDatas(String indexName, String searchText, int selectMajor, int selectSub, boolean zeroPossible) {

        try {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            if (zeroPossible) {
                boolQuery.filter(f -> f.term(t -> t.field("zero_possible").value(true)));
            }

            if (selectMajor != 0) {
                List<Integer> selectMajorList = List.of(selectMajor);
                boolQuery.must(m -> m.terms(t -> t.field("major_type").terms(t1 -> t1.value(convertToFieldValues(selectMajorList)))));
            }

            if (selectSub != 0) {
                List<Integer> selectSubList = List.of(selectMajor);
                boolQuery.must(m -> m.terms(t -> t.field("major_type").terms(t1 -> t1.value(convertToFieldValues(selectSubList)))));
            }

            if (!searchText.isEmpty()) {
                boolQuery.should(m -> m.match(mq -> mq.field("name").query(searchText).boost(2.0f)));
                boolQuery.should(m -> m.match(mq -> mq.field("address").query(searchText).boost(1.5f)));
                boolQuery.minimumShouldMatch("1");
            }

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(q -> q.bool(boolQuery.build()))
                    .size(10000)
            );

            return searchAsyncClient.search(searchRequest, SearchStoreDto.class)
                    .thenApply(resp -> resp.hits().hits().stream()
                            .map(Hit::source)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()))
                    .exceptionally(ex -> {
                        log.error("[Error][SearchService -> getTotalSearchDatas] {}", ex.getMessage());
                        return List.of();
                    });

        } catch(Exception e) {
            log.error("[Error][SearchService -> getTotalSearchDatas] {}", e.getMessage());
            return CompletableFuture.completedFuture(List.of());
        }
    }
}