package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.dto.store.KakaoStoreDto;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.repository.*;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.cho_co_song_i.yummy.yummy.entity.QStore.store;
import static com.cho_co_song_i.yummy.yummy.entity.QStoreLocationInfoTbl.storeLocationInfoTbl;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final StoreLocationInfoRepository storeLocationInfoRepository;
    private final StoreLocationRoadInfoRepository storeLocationRoadInfoRepository;
    private final ZeroPossibleMarketRepository zeroPossibleMarketRepository;
    private final CategoryRepository categoryRepository;
    private final StoreCategoryRepository storeCategoryRepository;

    private final StoreReviewRepository storeReviewRepository;

    private final JPAQueryFactory queryFactory;
    private final RestTemplate resttemplate;

    private final RedisAdapter redisAdapter;

    @Value("${kakao.search.api-url}")
    private String KAKAO_SEARCH_API_URL;
    @Value("${kakao.search.header}")
    private String KAKAO_SEARCH_HEADER;

    @Value("${spring.redis.store_rate_score}")
    private String STORE_RATE_SCORE_KEY;

    /* 오류 테스트 코드 */
    private void test() {
        throw new IllegalArgumentException("test");
    }

    /**
     * Kakao API 를 사용하기 위해서 정보들을 빌드해주는 함수.
     * @param storeName
     * @param page
     * @param size
     * @param pLat
     * @param pLng
     * @param category
     * @return
     */
    private URI buildKakaoApiUri(String storeName, int page, int size,
                                 BigDecimal pLat, BigDecimal pLng, String category) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(KAKAO_SEARCH_API_URL)
                .queryParam("category_group_code", category)
                .queryParam("query", storeName);

        if (page > 0) builder.queryParam("page", page);
        if (size > 0) builder.queryParam("size", size);
        if (pLat != null && pLng != null) {
            builder.queryParam("y", pLat).queryParam("x", pLng);
        }

        return builder.encode(StandardCharsets.UTF_8).build().toUri();
    }

    /**
     * Kakao api 를 통해서 음식점 데이터를 fetch 해주는 함수
     * @param uri
     * @return
     */
    private JsonNode fetchKakaoApiResponse(URI uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", KAKAO_SEARCH_HEADER);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = resttemplate.exchange(
                uri, HttpMethod.GET, request, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }

        return null;
    }

    /**
     * Kakao Api 에서 호출된 json 객체를 KakaoStoreDto 리스트로 파싱해주는 메소드.
     * @param documents
     * @return
     */
    private List<KakaoStoreDto> parseKakaoDocuments(JsonNode documents) {
        List<KakaoStoreDto> list = new ArrayList<>();

        if (documents != null && documents.isArray()) {
            for (JsonNode doc : documents) {
                KakaoStoreDto dto = KakaoStoreDto.builder()
                        .addressName(doc.path("address_name").asText(null))
                        .categoryGroupName(doc.path("category_group_name").asText(null))
                        .categoryGroupCode(doc.path("category_group_code").asText(null))
                        .categoryName(doc.path("category_name").asText(null))
                        .phone(doc.path("phone").asText(null))
                        .placeUrl(doc.path("place_url").asText(null))
                        .placeName(doc.path("place_name").asText(null))
                        .roadAddressName(doc.path("road_address_name").asText(null))
                        .lat(new BigDecimal(doc.path("y").asText("0")))
                        .lng(new BigDecimal(doc.path("x").asText("0")))
                        .build();

                list.add(dto);
            }
        }

        return list;
    }

    /**
     * Kakao api 에서 상점정보들을 가져와주는 기능
     * @param storeName
     * @param page
     * @param size
     * @param pLat
     * @param pLng
     * @return
     */
    private Optional<List<KakaoStoreDto>> getKakaoStoreDtoFromKakaoApiFromCategories(String storeName, int page, int size,
            BigDecimal pLat, BigDecimal pLng) {

        if (storeName == null || storeName.isEmpty()) {
            return Optional.empty();
        }

        List<KakaoStoreDto> result = new ArrayList<>();

        /* category 우선순위: FD6(음식점) -> CE7(카페) */
        List<String> categories = List.of("FD6", "CE7");

        for (String category : categories) {
            result = getKakaoStoreDtoFromKakaoApiByCategory(storeName, page, size, pLat, pLng, category);
            if (!result.isEmpty()) break; /* 첫 번째 성공 시, 반복 중지 */
        }

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    /**
     * storeLocationInfoTbl + store 테이블을 조인하여 조건에 맞는 첫번째 결과를 반환해주는 메소드.
     * @param kakaoStoreDto
     * @return
     */
    private Store findFirstDataJoinedStoreLocationInfoAndStore(KakaoStoreDto kakaoStoreDto) {
        return queryFactory
                .select(storeLocationInfoTbl.store)
                .from(storeLocationInfoTbl)
                .join(storeLocationInfoTbl.store, store)
                .where(
                        store.name.eq(kakaoStoreDto.getPlaceName()),
                        storeLocationInfoTbl.lat.eq(kakaoStoreDto.getLat()),
                        storeLocationInfoTbl.lng.eq(kakaoStoreDto.getLng())
                )
                .fetchFirst();
    }

    /**
     * Kakao API 에서 받아온 데이터를 가공하여 Store 데이터로 디비에 저장해주는 기능
     * @param kakaoStoreDto
     * @return
     */
    private Optional<Store> createStoreFromKakaoStore(KakaoStoreDto kakaoStoreDto) {
        /* 이미 존재하는 음식점인지 체크 */
        Store existsStore = findFirstDataJoinedStoreLocationInfoAndStore(kakaoStoreDto);

        if (existsStore == null) {
            Store createStore = new Store(kakaoStoreDto, "createStoreFromKakao");
            storeRepository.save(createStore);
            return Optional.of(createStore);
        } else {
            log.warn("[WARN][StoreServiceImpl->createStoreFromKakaoStore] It's a store that already exists.: {}", kakaoStoreDto);
            return Optional.empty();
        }
    }

    /**
     * ZeroPossibleMarket 저장 기능
     * @param store
     */
    private void inputZeroPossibleTbl(Store store) {
        ZeroPossibleMarket zeroPossibleMarket = new ZeroPossibleMarket(store, "inputZeroPossibleTbl");
        zeroPossibleMarket.markAsNew();
        zeroPossibleMarketRepository.save(zeroPossibleMarket);
    }

    /**
     * StoreLocationInfoTbl 저장 기능
     * @param kakaoStoreDto
     * @param store
     */
    private void inputStoreLocationInfo(KakaoStoreDto kakaoStoreDto, Store store) {
        StoreLocationInfoTbl storeLocationInfo = new StoreLocationInfoTbl(kakaoStoreDto, store, "inputNewStore");
        storeLocationInfo.markAsNew();
        storeLocationInfoRepository.save(storeLocationInfo);
    }

    /**
     * StoreLocationRoadInfoTbl 저장 기능
     * @param kakaoStoreDto
     * @param store
     */
    private void inputStoreLocationRoadInfo(KakaoStoreDto kakaoStoreDto, Store store) {
        StoreLocationRoadInfoTbl storeLocationRoadInfoTbl = new StoreLocationRoadInfoTbl(kakaoStoreDto, store, "inputStoreLocationRoad");
        storeLocationRoadInfoTbl.markAsNew();
        storeLocationRoadInfoRepository.save(storeLocationRoadInfoTbl);
    }

    /**
     * CategoryTbl 저장해주는 함수 -> 기존에 존재하는 카테고리라면 저장하지 않는다.
     * @param kakaoStoreDto
     * @return
     */
    private CategoryTbl createOrFindCategoryTbl(KakaoStoreDto kakaoStoreDto) {
        Optional<CategoryTbl> categoryTblOpt =
                categoryRepository.findByCategoryGroupCodeAndCategoryGroupNameAndCategoryName(
                        kakaoStoreDto.getCategoryGroupCode(),
                        kakaoStoreDto.getCategoryGroupName(),
                        kakaoStoreDto.getCategoryName()
                );

        if (categoryTblOpt.isPresent()) {
            return categoryTblOpt.get();
        } else {
            CategoryTbl categoryTbl = new CategoryTbl(kakaoStoreDto, "createOrFindCategoryTbl");
            categoryRepository.save(categoryTbl);
            return categoryTbl;
        }
    }

    /**
     * StoreCategoryTbl 를 저장해주는 기능
     * @param store
     * @param categoryTbl
     */
    private void inputStoreCategory(Store store, CategoryTbl categoryTbl) {
        StoreCategoryTbl storeCategoryTbl = new StoreCategoryTbl(store, categoryTbl, "inputStoreCategory");
        storeCategoryRepository.save(storeCategoryTbl);
    }

    /**
     * storeLocationInfoTbl + store 를 fetch join 한 결과를 반환해주는 메소드
     * @return
     */
    private List<StoreLocationInfoTbl> fetchJoinedStoreLocations() {
        return queryFactory
                .selectFrom(storeLocationInfoTbl)
                .join(storeLocationInfoTbl.store, store).fetchJoin()
                .fetch();
    }

    /**
     * KakaoStoreDto 데이터를 기준으로 디비에 상점 관련 데이터들을 저장해주는 메소드
     * @param kakaoStoreDtos
     * @param zeroYn
     */
    private void inputNewStoreDataToDb(List<KakaoStoreDto> kakaoStoreDtos, Boolean zeroYn) {
        for (KakaoStoreDto dto : kakaoStoreDtos) {
            Optional<Store> storeOpt = createStoreFromKakaoStore(dto);

            if (storeOpt.isPresent()) {
                Store inputStore = storeOpt.get();

                if (zeroYn) inputZeroPossibleTbl(inputStore);

                inputStoreLocationInfo(dto, inputStore);
                inputStoreLocationRoadInfo(dto, inputStore);
                CategoryTbl categoryTbl = createOrFindCategoryTbl(dto);
                inputStoreCategory(inputStore, categoryTbl);
            }
        }
    }

    /**
     * 카테고리별로 상점 정보를 Kakao API 를통해서 가져와주는 메소드.
     * @param storeName
     * @param page
     * @param size
     * @param pLat
     * @param pLng
     * @param category
     * @return
     */
    private List<KakaoStoreDto> getKakaoStoreDtoFromKakaoApiByCategory(String storeName, int page, int size,
                                                                       BigDecimal pLat, BigDecimal pLng, String category) {
        List<KakaoStoreDto> result = new ArrayList<>();

        URI uri = buildKakaoApiUri(storeName, page, size, pLat, pLng, category);
        JsonNode root = fetchKakaoApiResponse(uri);

        if (root != null) {
            List<KakaoStoreDto> parsed = parseKakaoDocuments(root.path("documents"));
            result.addAll(parsed);
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus inputNewStore(String storeName, Integer page, Integer size, BigDecimal pLat, BigDecimal pLng, Boolean zeroYn) {

        int inputPage = page == null ? 1 : page;
        int inputSize = size == null ? 1 : size;

        Optional<List<KakaoStoreDto>> kakaoStoreDto =
                getKakaoStoreDtoFromKakaoApiFromCategories(storeName, inputPage, inputSize, pLat, pLng);

        if (kakaoStoreDto.isPresent()) {
            List<KakaoStoreDto> kakaoStoreDtos = kakaoStoreDto.get();

            inputNewStoreDataToDb(kakaoStoreDtos, zeroYn);
        }

        return PublicStatus.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus inputNewStores(String storeName, Integer page, String category, Boolean zeroYn) {

        int sizePerPage = 15;
        int quotient = (page + sizePerPage - 1) / sizePerPage;
        int remainder = page % sizePerPage;

        List<KakaoStoreDto> totalkakaoStoreDto = new ArrayList<>();

        for (int i = 1; i <= quotient; i++) {
            int currentPageSize = (i == quotient && remainder != 0) ? remainder : sizePerPage;

            List<KakaoStoreDto> kakaoStoreDtos =
                    getKakaoStoreDtoFromKakaoApiByCategory(storeName, i, currentPageSize, null, null, category);

            if (!kakaoStoreDtos.isEmpty()) {
                totalkakaoStoreDto.addAll(kakaoStoreDtos);
            }
        }

        inputNewStoreDataToDb(totalkakaoStoreDto, zeroYn);

        return PublicStatus.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus modifyExistsStores() {

        /* StoreLocationInfoTbl + Store join 한 결과 리스트 */
        List<StoreLocationInfoTbl> existsLocations = fetchJoinedStoreLocations();

        for (StoreLocationInfoTbl location: existsLocations) {
            Optional<List<KakaoStoreDto>> kakaoStoreDtoOpts =
                    getKakaoStoreDtoFromKakaoApiFromCategories(location.getStore().getName(), 1, 1, location.getLat(), location.getLng());

            if (kakaoStoreDtoOpts.isPresent()) {
                List<KakaoStoreDto> kakaoStoreDtos = kakaoStoreDtoOpts.get();

                if (kakaoStoreDtos.size() == 1) {
                    KakaoStoreDto kakaoStoreDto = kakaoStoreDtos.get(0);

                    /* store 테이블을 update. */
                    Store modifyStore = location.getStore();
                    modifyStore.updateStoreFromKakaoStoreDto(kakaoStoreDto, "modifyExistsStores");

                    /* store_location_info_tbl 테이블을 update */
                    location.modifyStoreLocationInfoFromKakaoStoreDto(kakaoStoreDto, "modifyExistsStores");

                    /*
                    * store_location_road_info_tbl 에 새로운 데이터를 insert
                    * 해당 부분에서 기존의 store_location_road_info_tbl 데이터들을 모두 truncate 해야 한다.
                    * */
                    inputStoreLocationRoadInfo(kakaoStoreDto, modifyStore);

                    /* CategoryTbl에 새로운 카테고리 insert */
                    CategoryTbl categoryTbl = createOrFindCategoryTbl(kakaoStoreDto);

                    /* 카테고리 - 상품 테이블에 data insert */
                    inputStoreCategory(modifyStore, categoryTbl);

                } else {
                    log.warn("[WARN][StoreServiceImpl->modifyExistsStores] There are more than one filtered object. (location.seq: {})", location.getSeq());
                }
            } else {
                log.warn("[WARN][StoreServiceImpl->modifyExistsStores] No matching value exists for Kakao API. (location.seq: {})", location.getSeq());
            }
        }

        return PublicStatus.SUCCESS;
    }

    public BigDecimal findRateScore(long storeSeq) throws Exception {

        String redisKey = String.format("%s:%d",STORE_RATE_SCORE_KEY,storeSeq);

        /* Redis 에서 특정 상점의 평균 별점 점수를 가져와준다. */
        Double avgScore = Optional.ofNullable(
                redisAdapter.getValue(redisKey, new TypeReference<Double>() {})
        ).orElse(0.0);

        return BigDecimal.valueOf(avgScore).setScale(1, RoundingMode.HALF_UP);
    }

    public long findReviewCnt(long storeSeq) {
        return storeReviewRepository.countByStoreSeq(storeSeq);
    }



}