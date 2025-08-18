package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    /**
     * 상점등록 api
     * @param storeName
     * @param page
     * @param size
     * @param pLat
     * @param pLng
     * @param zeroYn
     * @return
     */
    @GetMapping("/inputNewStore")
    public ResponseEntity<PublicStatus> inputNewStore(
            @RequestParam(value = "storeName", required = true) String storeName,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "pLat", required = false) BigDecimal pLat,
            @RequestParam(value = "pLng", required = false) BigDecimal pLng,
            @RequestParam(value = "zeroYn", required = true) Boolean zeroYn
            ) {
        return ResponseEntity.ok(storeService.inputNewStore(storeName, page, size, pLat, pLng,  zeroYn));
    }

    /**
     * 키워드 기준 여러개의 삼점을 등록하는 api
     * @param storeName
     * @param size
     * @param zeroYn
     * @return
     */
    @GetMapping("/inputNewStores")
    public ResponseEntity<PublicStatus> inputNewStores(
            @RequestParam(value = "storeName", required = true) String storeName,
            @RequestParam(value = "size", required = true) Integer size,
            @RequestParam(value = "category", required = true) String category,
            @RequestParam(value = "zeroYn", required = true) Boolean zeroYn) {

        return ResponseEntity.ok(storeService.inputNewStores(storeName, size, category, zeroYn));
    }

    /**
     * 기존의 Store 데이터를 Kakao API 기준 데이터로 모두 update 시켜주는 api
     * @return
     */
    @GetMapping("/updateExistsStore")
    public ResponseEntity<PublicStatus> modifyExistsStoreDatas() {
        return ResponseEntity.ok(storeService.modifyExistsStores());
    }

    /**
     * 특정 상점의 평균별점을 가져오는 api -> 상세에서 쓸 것
     * @param storeSeq
     * @return
     * @throws Exception
     */
    @GetMapping("/getRatingScore")
    public ResponseEntity<BigDecimal> getRateScoreStore(
            @RequestParam(value = "storeSeq", required = true) Integer storeSeq
    ) throws Exception {
        return ResponseEntity.ok(storeService.findRateScore(storeSeq));
    }

    /**
     * 특정 상점의 리뷰 개수를 가져와주는 api -> 상세에서 쓸 것
     * @param storeSeq
     * @return
     */
    @GetMapping("/getReviewCnt")
    public ResponseEntity<Long> getReviewCnt(
            @RequestParam(value = "storeSeq", required = true) Integer storeSeq
    ) {
        return ResponseEntity.ok(storeService.findReviewCnt(storeSeq));
    }

}