package com.cho_co_song_i.yummy.yummy.serviceImpl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.cho_co_song_i.yummy.yummy.dto.search.AutoCompleteDto;
import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.search.AutoCompleteResDto;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import com.cho_co_song_i.yummy.yummy.utils.AnalyzerUtil;
import com.cho_co_song_i.yummy.yummy.utils.HangulQwertyConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient searchClient;

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

    /**
     *
     * @param value
     * @return
     */
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
     *
     * @param selectMajor
     * @param selectSub
     * @param zeroPossible
     * @return
     */
    private BoolQuery.Builder getBuilder(int selectMajor, int selectSub, boolean zeroPossible) {
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
            boolQuery.must(m -> m.terms(t -> t.field("sub_type").terms(t1 -> t1.value(convertToFieldValues(selectSubList)))));
        }
        return boolQuery;
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

        BoolQuery.Builder boolQuery = getBuilder(selectMajor, selectSub, zeroPossible);

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

    /**
     *
     * @param array
     * @param target
     * @return
     */
    private static int indexOf(char[] array, char target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    /**
     * score 순으로 특정 문자열을 검색해주는 함수
     * @param indexName
     * @param searchText
     * @param topCnt
     * @return
     * @throws Exception
     */
    private List<AutoCompleteDto> findTopAutoSearchKeyword(String indexName, String searchText, int topCnt) throws Exception {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(q -> q
                        .multiMatch(m -> m
                                .query(searchText)
                                .fields("name.autocomplete^3", "name.basic", "name_chosung^2")
                                .type(TextQueryType.BestFields)
                        )
                )
                .size(topCnt)
        );

        SearchResponse<AutoCompleteDto> response = searchClient.search(searchRequest, AutoCompleteDto.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 입력 문자열과 후보 리스트를 비교해서, 유사도가 높은 항목만 필터링하고 점수까지 포함한 새 객체 리스트를 만들어주는 함수
     * @param searchText
     * @param searchResp
     * @return
     */
    private List<AutoCompleteResDto> getSimilarAutoCompleteResults(String searchText, List<AutoCompleteDto> searchResp) {

        List<AutoCompleteResDto> resultResp = new ArrayList<>();

        for (AutoCompleteDto dto: searchResp) {

            String dtoName = dto.getName();
            String dtoChosung = dto.getNameChosung();
            int keywordWeight = dto.getKeywordWeight();

            double similarityWord = AnalyzerUtil.similarityByLevenstein(searchText, dtoName);
            double similarityChosung = AnalyzerUtil.similarityByLevenstein(searchText, dtoChosung);

            //System.out.println(dto.getName());
            //System.out.println("similarityWord: " + similarityWord);
            //System.out.println("similarityChosung: " + similarityChosung);

            if (similarityWord > 0.6 || similarityChosung == 1.0) {
                double bigScore = Math.max(similarityWord, similarityChosung);
                float rounded = Math.round((float) bigScore * 100) / 100f;

                AutoCompleteResDto newDto = AutoCompleteResDto.builder()
                        .name(dtoName)
                        .score(rounded)
                        .keywordWeight(keywordWeight)
                        .build();

                resultResp.add(newDto);
            }
        }

        return resultResp;
    }

    public List<AutoCompleteResDto> findAutoSearchKeyword(String indexName, String searchText) throws Exception {



        List<AutoCompleteDto> searchResp = findTopAutoSearchKeyword(indexName, searchText, 15);
        List<AutoCompleteResDto> resultResp = getSimilarAutoCompleteResults(searchText, searchResp);

        if (resultResp.isEmpty() && AnalyzerUtil.isAllEnglish(searchText)) {
            String convertKeyword = HangulQwertyConverter.convertQwertyToHangul(searchText);
            searchResp = findTopAutoSearchKeyword(indexName, convertKeyword, 15);
            resultResp = getSimilarAutoCompleteResults(convertKeyword, searchResp);
        }

        return resultResp;
    }
}