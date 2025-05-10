package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.SearchStoreDto;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SearchService {
    CompletableFuture<List<SearchStoreDto>> searchDocuments(String index, String field, String query);
    CompletableFuture<List<SearchStoreDto>> getSearchAllStores(String indexName);

    /** secretm test
     * 가게 이름으로 단건 조회
     *
     * @param indexName Elasticsearch 인덱스명
     * @param storeName 조회할 가게 이름 (match 질의)
     * @return 이름과 유사한 문서 중 첫 번째를 Optional로 감싸서 반환
     */
    CompletableFuture<Optional<SearchStoreDto>> getStoreByName(String indexName, String storeName);

    /**
     * Elasticsearch 에서 모든 도큐먼트를 페이징 조회
     *
     * @param indexName 조회할 인덱스
     * @param page      1부터 시작하는 페이지 번호
     * @param size      페이지당 항목 수
     * @return CompletableFuture<List<SearchStoreDto>>
     */
    CompletableFuture<List<SearchStoreDto>> getStoresByPage (String indexName, int page, int size);

    /**
     * 통함검색 알고리즘
     * @param indexName
     * @param searchText
     * @param selectMajor
     * @param selectSub
     * @param zeroPossible
     * @return
     */
    CompletableFuture<List<SearchStoreDto>> getTotalSearchDatas(String indexName, String searchText, int selectMajor, int selectSub, boolean zeroPossible);

}
