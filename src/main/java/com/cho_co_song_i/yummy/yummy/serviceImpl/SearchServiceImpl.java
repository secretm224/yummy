package com.cho_co_song_i.yummy.yummy.serviceImpl;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.LatLonGeoLocation;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.cho_co_song_i.yummy.yummy.dto.search.*;
import com.cho_co_song_i.yummy.yummy.service.SearchService;
import com.cho_co_song_i.yummy.yummy.utils.AnalyzerUtil;
import com.cho_co_song_i.yummy.yummy.utils.HangulQwertyConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    @Value("${spring.elasticsearch.index.store}")
    private String storeIndex;
    @Value("${spring.elasticsearch.index.auto-complete}")
    private String autoKeywordIndex;
    @Value("${spring.elasticsearch.index.subway}")
    private String subwayIndex;

    private final ElasticsearchClient searchClient;
    private final ElasticsearchAsyncClient asyncSearchClient;

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

    /**
     * elasticsearch async 유틸 공통함수 추출
     * @param futureResponse
     * @return
     * @param <T>
     */
    private static <T> CompletableFuture<List<T>> extractSources(CompletableFuture<SearchResponse<T>> futureResponse) {
        return futureResponse.thenApply(response -> response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList()
        );
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
     * @param searchText
     * @param topCnt
     * @return
     */
    private CompletableFuture<List<AutoCompleteDto>> findTopAutoSearchKeyword(String searchText, int topCnt) {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(autoKeywordIndex)
                .query(q -> q
                        .multiMatch(m -> m
                                .query(searchText)
                                .fields("name.autocomplete^3", "name.basic", "name_chosung^2")
                                .type(TextQueryType.BestFields)
                        )
                )
                .size(topCnt)
        );

        return extractSources(
                asyncSearchClient.search(searchRequest, AutoCompleteDto.class)
        );
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

    public CompletableFuture<List<SearchStoreDto>> findSearchStoresBoundary(double minLat, double maxLat, double minLon, double maxLon, int zoom, boolean showOnlyZeroPay) {

        /* 필터 리스트를 동적으로 구성 */
        List<Query> filters = new ArrayList<>();

        /* 1. geo_bounding_box 필터 추가 */
        filters.add(Query.of(f -> f
                .geoBoundingBox(gb -> gb
                        .field("location")
                        .boundingBox(bb -> bb
                                .tlbr(tlbr -> tlbr
                                        .topLeft(GeoLocation.of(gl -> gl
                                                .latlon(LatLonGeoLocation.of(ll -> ll
                                                        .lat(maxLat)
                                                        .lon(minLon)
                                                ))
                                        ))
                                        .bottomRight(GeoLocation.of(gl -> gl
                                                .latlon(LatLonGeoLocation.of(ll -> ll
                                                        .lat(minLat)
                                                        .lon(maxLon)
                                                ))
                                        ))
                                )
                        )
                )
        ));

        /* 2. 조건에 따라 term 필터 추가 */
        if (showOnlyZeroPay) {
            filters.add(Query.of(f -> f
                    .term(t -> t
                            .field("zero_possible")
                            .value(true)
                    )
            ));
        }

        /* 3. 최종 SearchRequest 구성 */
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(storeIndex)
                .size(1000)
                .query(q -> q
                        .bool(b -> b
                                .filter(filters)
                        )
                )
        );

        return extractSources(
                asyncSearchClient.search(searchRequest, SearchStoreDto.class)
        );
    }

    public Optional<SearchStoreDto> findStoreByName(String storeName) throws Exception {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(storeIndex)
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
            int page,
            int size
    ) throws Exception {
        int from = (page - 1) * size;

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(storeIndex)
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

    public CompletableFuture<List<AutoCompleteResDto>> findAutoSearchKeyword(String searchText) {

        return findTopAutoSearchKeyword(searchText, 15)
                .thenCompose(searchResp -> {
                    List<AutoCompleteResDto> resultResp = getSimilarAutoCompleteResults(searchText, searchResp);

                    if (resultResp.isEmpty() && AnalyzerUtil.isAllEnglish(searchText)) {
                        String convertKeyword = HangulQwertyConverter.convertQwertyToHangul(searchText);
                        return findTopAutoSearchKeyword(convertKeyword, 15)
                                .thenApply(fallbackResp ->
                                        getSimilarAutoCompleteResults(convertKeyword, fallbackResp)
                                );
                    }

                    return CompletableFuture.completedFuture(resultResp);
                });
    }


    public CompletableFuture<TotalSearchDto> findTotalsearch(String searchText, boolean zeroPossible, int startIdx, int pageCnt) {

        /* 검색금지단어 -> 향후 추가 예정 */

        /* 1. 상점 관련 검색결과*/
        CompletableFuture<List<StoreSearchDto>> storeFutures = findTotalStoreSearch(searchText, zeroPossible, startIdx, pageCnt);

        /* 2. 지하철역 관련 검색 결과 */
        CompletableFuture<List<SubwayInfoDto>> subwayFutures = findTotalSubwaySearch(searchText);

        return CompletableFuture.allOf(storeFutures, subwayFutures)
                .thenApply(voided -> {
                    List<StoreSearchDto> storeList = storeFutures.join();
                    List<SubwayInfoDto> subwayList = subwayFutures.join();

                    return TotalSearchDto
                            .builder()
                            .storeSearchDtoList(storeList)
                            .subwayInfoDtoList(subwayList)
                            .build();
                });

    }

    private CompletableFuture<List<StoreSearchDto>> findTotalStoreSearch(String searchText, boolean zeroPossible, int startIdx, int pageCnt) {

        /* 필터 리스트를 동적으로 구성 */
        List<Query> filters = new ArrayList<>();

        /* 1. term filter 추가 (zero_possible: true) */
        filters.add(Query.of(f -> f
                .term(t -> t
                        .field("zero_possible")
                        .value(true)
                )
        ));

        /* 2. multi_match query (must) */
        Query mustQuery = Query.of(q -> q
                .multiMatch(mm -> mm
                        .query(searchText)
                        .fields("name^3", "road_address", "address", "category_name")
                        .operator(Operator.And)
                        .type(TextQueryType.CrossFields)
                )
        );

        /* 3. 최종 SearchRequest 생성 */
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(storeIndex)
                .from(startIdx)
                .size(pageCnt)
                .source(src -> src
                        .filter(f -> f
                                .includes("address", "lat", "lng", "name", "road_address", "seq", "tel", "url", "zeroPossible")
                        )
                )
                .query(q -> q
                        .bool(b -> b
                                .must(mustQuery)
                                .filter(filters)
                        )
                )
        );

        return extractSources(
                asyncSearchClient.search(searchRequest, StoreSearchDto.class)
        );
    }

    private CompletableFuture<List<SubwayInfoDto>> findTotalSubwaySearch(String searchText) {

        /* Must Query */
        Query mustQuery = Query.of(q -> q
                .multiMatch(mm -> mm
                        .query(searchText)
                        .fields("station_name^50", "station_eng_name^10", "subway_line^2", "station_load_addr")
                        .operator(Operator.And)
                        .type(TextQueryType.BestFields)
                )
        );

        /* Search Query */
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(subwayIndex)
                .size(5)
                .source(src -> src
                        .filter(f -> f
                                .includes("seq", "station_name", "station_load_addr", "station_eng_name", "subway_line")
                        )
                )
                .query(q -> q
                        .bool(b -> b
                                .must(mustQuery)
                        )
                )
        );

        return extractSources(
                asyncSearchClient.search(searchRequest, SubwayInfoDto.class)
        );

    }


    public CompletableFuture<List<SubwayInfoDto>> findSubwayInfoSearch(double minLat, double maxLat, double minLon, double maxLon, int zoom) {

        /* 필터 리스트를 동적으로 구성 */
        List<Query> filters = new ArrayList<>();

        /* 1. geo_bounding_box 필터 추가 */
        filters.add(Query.of(f -> f
                .geoBoundingBox(gb -> gb
                        .field("location")
                        .boundingBox(bb -> bb
                                .tlbr(tlbr -> tlbr
                                        .topLeft(GeoLocation.of(gl -> gl
                                                .latlon(LatLonGeoLocation.of(ll -> ll
                                                        .lat(maxLat)
                                                        .lon(minLon)
                                                ))
                                        ))
                                        .bottomRight(GeoLocation.of(gl -> gl
                                                .latlon(LatLonGeoLocation.of(ll -> ll
                                                        .lat(minLat)
                                                        .lon(maxLon)
                                                ))
                                        ))
                                )
                        )
                )
        ));

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(subwayIndex)
                .size(1000)
                .query(q -> q
                        .bool(b -> b
                                .filter(filters)
                        )
                )
        );

        return extractSources(
                asyncSearchClient.search(searchRequest, SubwayInfoDto.class)
        );
    }
}