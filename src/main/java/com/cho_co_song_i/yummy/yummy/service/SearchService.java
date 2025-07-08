package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.search.SearchStoreDto;
import com.cho_co_song_i.yummy.yummy.dto.search.AutoCompleteResDto;
import com.cho_co_song_i.yummy.yummy.dto.search.TotalSearchDto;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SearchService {
    /**
     * 특정 바운더리 내에 있는 상점 데이터를 검색해주는 함수
     * @param indexName
     * @param minLat
     * @param maxLat
     * @param minLon
     * @param maxLon
     * @param zoom
     * @param showOnlyZeroPay
     * @return
     */
    CompletableFuture<List<SearchStoreDto>> findSearchStoresBoundary(String indexName, double minLat, double maxLat, double minLon, double maxLon, int zoom, boolean showOnlyZeroPay);
    /**
     * 자동완성 키워드 알고리즘
     * @param indexName
     * @param searchText
     * @return
     * @throws Exception
     */
    CompletableFuture<List<AutoCompleteResDto>> findAutoSearchKeyword(String indexName, String searchText);
    /**
     * 가게 이름으로 단건 조회 (secretm test)
     * @param indexName Elasticsearch 인덱스명
     * @param storeName 조회할 가게 이름 (match 질의)
     * @return 이름과 유사한 문서 중 첫 번째를 Optional로 감싸서 반환
     * @throws Exception
     */
    Optional<SearchStoreDto> findStoreByName(String indexName, String storeName) throws Exception;
    /**
     * Elasticsearch 에서 모든 도큐먼트를 페이징 조회
     *
     * @param indexName 조회할 인덱스
     * @param page      1부터 시작하는 페이지 번호
     * @param size      페이지당 항목 수
     * @return List<SearchStoreDto>
     */
    List<SearchStoreDto> findStoresByPage(String indexName, int page, int size) throws Exception;
    /**
     * 통함검색 알고리즘
     * @param indexName
     * @param searchText
     * @param selectMajor
     * @param selectSub
     * @param zeroPossible
     * @return
     * @throws Exception
     */
    List<SearchStoreDto> findTotalSearchDatas(String indexName, String searchText, int selectMajor, int selectSub, boolean zeroPossible) throws Exception;

    /**
     * 통합검색 알고리즘 (new)
     * @param indexName
     * @param searchText
     * @param zeroPossible
     * @param startIdx
     * @param pageCnt
     * @return
     */
    CompletableFuture<List<TotalSearchDto>> findTotalsearch(String indexName, String searchText, boolean zeroPossible, int startIdx, int pageCnt);
}
