package com.cho_co_song_i.yummy.yummy.serviceImpl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final ElasticsearchClient searchClient;


    /* ERROR 테스트용 */
    private void test() throws Exception {
        int a = 1 / 0;
    }

    /* ERROR 테스트용2 */
    private void test2() {
        throw new RuntimeException("HOHO");
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

    public List<SearchStoreDto> findSearchAllStores(String indexName) throws Exception {

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .size(10000)
                .query(q -> q
                        .matchAll(m -> m)));


        SearchResponse<SearchStoreDto> resp = searchClient.search(searchRequest, SearchStoreDto.class);

        return resp.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Optional<SearchStoreDto> findStoreByName(String indexName, String storeName) throws Exception {
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

        SearchResponse<SearchStoreDto> resp = searchClient.search(searchRequest, SearchStoreDto.class);

        return resp.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .findFirst();
    }

    public List<SearchStoreDto> findStoresByPage (
            String indexName,
            int page,
            int size
    ) throws Exception {
        int from = (page - 1) * size;

        SearchRequest searchRequest = SearchRequest.of(s -> s
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

        SearchResponse<SearchStoreDto> resp = searchClient.search(searchRequest, SearchStoreDto.class);

        return resp.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<SearchStoreDto> findTotalSearchDatas(String indexName, String searchText, int selectMajor, int selectSub, boolean zeroPossible) throws Exception {

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

        SearchResponse<SearchStoreDto> resp = searchClient.search(searchRequest, SearchStoreDto.class);

        return resp.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}