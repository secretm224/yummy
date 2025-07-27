package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.search.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SearchService {
    /**
     * 특정 바운더리 내에 있는 상점 데이터를 검색해주는 함수
     * @param minLat
     * @param maxLat
     * @param minLon
     * @param maxLon
     * @param zoom
     * @param showOnlyZeroPay
     * @return
     */
    CompletableFuture<List<SearchStoreDto>> findSearchStoresBoundary(double minLat, double maxLat, double minLon, double maxLon, int zoom, boolean showOnlyZeroPay);
    /**
     * 자동완성 키워드 알고리즘
     * @param searchText
     * @return
     * @throws Exception
     */
    CompletableFuture<List<AutoCompleteResDto>> findAutoSearchKeyword(String searchText);
    /**
     * 가게 이름으로 단건 조회 (secretm test)
     * @param storeName 조회할 가게 이름 (match 질의)
     * @return 이름과 유사한 문서 중 첫 번째를 Optional로 감싸서 반환
     * @throws Exception
     */
    Optional<SearchStoreDto> findStoreByName(String storeName) throws Exception;
    /**
     * Elasticsearch 에서 모든 도큐먼트를 페이징 조회
     *
     * @param page      1부터 시작하는 페이지 번호
     * @param size      페이지당 항목 수
     * @return List<SearchStoreDto>
     */
    List<SearchStoreDto> findStoresByPage(int page, int size) throws Exception;
    /**
     * 통합검색 알고리즘 (new)
     * @param searchText
     * @param zeroPossible
     * @param startIdx
     * @param pageCnt
     * @return
     */
    CompletableFuture<TotalSearchDto> findTotalsearch(String searchText, boolean zeroPossible, int startIdx, int pageCnt);
    /**
     * 맵에 지하철을 표시하기 위한 검색 메소드
     * @param minLat
     * @param maxLat
     * @param minLon
     * @param maxLon
     * @param zoom -> 향후에 필요해질 수 있음
     * @return
     */
    CompletableFuture<List<SubwayInfoDto>> findSubwayInfoSearch(double minLat, double maxLat, double minLon, double maxLon, int zoom);

}
