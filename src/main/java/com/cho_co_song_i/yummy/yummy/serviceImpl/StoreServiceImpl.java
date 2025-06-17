package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.dto.store.KakaoStoreDto;
import com.cho_co_song_i.yummy.yummy.entity.*;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.repository.*;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cho_co_song_i.yummy.yummy.entity.QStore.store;
import static com.cho_co_song_i.yummy.yummy.entity.QStoreLocationInfoTbl.storeLocationInfoTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QStoreLocationRoadInfoTbl.storeLocationRoadInfoTbl;
import static com.cho_co_song_i.yummy.yummy.entity.QStoreTypeMajor.storeTypeMajor;
import static com.cho_co_song_i.yummy.yummy.entity.QStoreTypeSub.storeTypeSub;
import static com.cho_co_song_i.yummy.yummy.entity.QStoreTypeLinkTbl.storeTypeLinkTbl;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    @PersistenceContext
    private EntityManager entityManager;

    private final StoreRepository storeRepository;
    private final StoreLocationInfoRepository storeLocationInfoRepository;
    private final StoreLocationRoadInfoRepository storeLocationRoadInfoRepository;
    private final ZeroPossibleMarketRepository zeroPossibleMarketRepository;
    private final CategoryRepository categoryRepository;
    private final StoreCategoryRepository storeCategoryRepository;

    private final JPAQueryFactory queryFactory;
    private final RedisAdapter redisAdapter;
    private final RestTemplate resttemplate;

    @Value("${kakao.search.api-url}")
    private String KAKAO_SEARCH_API_URL;
    @Value("${kakao.search.header}")
    private String KAKAO_SEARCH_HEADER;

    /* 오류 테스트 코드 */
    private void test() {
        throw new IllegalArgumentException("test");
    }

    /**
     * Entity -> DTO 변환
     * @param store
     * @return
     */
    private StoreDto convertToDto(Store store) {
        return new StoreDto(
                store.getSeq(),
                store.getName(),
                store.getType(),
                store.getUseYn(),
                store.getRegDt(),
                store.getRegId(),
                store.getChgDt(),
                store.getChgId(),
                store.getTel(),
                store.getUrl()
        );
    }

    /**
     * Entity -> DTO 변환 (StorTypeMajor)
     * @param storeTypeMajor
     * @return
     */
    private StoreTypeMajorDto convertTypeMajorToDto(StoreTypeMajor storeTypeMajor) {
        return new StoreTypeMajorDto(
                storeTypeMajor.getMajorType(),
                storeTypeMajor.getTypeName()
        );
    }

    /**
     *  Entity -> DTO 변환 (StorTypeSub)
     * @param storeTypeSub
     * @return
     */
    private StoreTypeSubDto convertTypeSubToDto(StoreTypeSub storeTypeSub) {
        return new StoreTypeSubDto(
                storeTypeSub.getSubType(),
                storeTypeSub.getMajorType(),
                storeTypeSub.getTypeName()
        );
    }


    /**
     * json 내부의 데이터를 파싱해주는 함수
     * @param json
     * @return
     */
    private Optional<DetailInfo> parseDetailInfo(JsonNode json) {
        if (json == null
                || !json.has("documents")
                || !json.get("documents").isArray()
                || json.get("documents").isEmpty()) {
            return Optional.empty();
        }

        JsonNode first = json.get("documents").get(0);

        String tel = Optional.ofNullable(first.get("phone"))
                .map(JsonNode::asText)
                .orElse(null);

        String url = Optional.ofNullable(first.get("place_url"))
                .map(JsonNode::asText)
                .orElse(null);

        return Optional.of(new DetailInfo(tel, url));
    }

//    /**
//     * tel, url 정보가 없는 Store, storeLocationInfoTbl 의 특정 데이터들만 쿼리해주는 함수
//     * 특정 컬럼만 가져와줘서 최대한 데이터 규모를 줄여준다.
//     * @return
//     */
//    private List<StoreLocationDto> findTelUrlEmptyData() {
//        return queryFactory
//                .select(Projections.constructor(StoreLocationDto.class,
//                        store.seq,
//                        store.name,
//                        storeLocationInfoTbl.lat,
//                        storeLocationInfoTbl.lng
//                ))
//                .from(storeLocationInfoTbl)
//                .innerJoin(storeLocationInfoTbl.store, store)
//                .where(
//                        store.tel.isEmpty()
//                                .or(store.url.isEmpty())
//                                .or(store.tel.isNull())
//                                .or(store.url.isNull())
//                )
//                .fetch();
//    }

    /**
     * 모든 Store, storeLocationInfoTbl 의 특정 데이터들만 쿼리해주는 함수
     * 특정 컬럼만 가져와줘서 최대한 데이터 규모를 줄여준다.
     * -> 향후에 데이터 사이즈가 커진다면 해당 메소드는 부하가 클 가능성이 있음.
     * @return
     */
//    private List<StoreLocationDto> findTelUrlAllData() {
//        return queryFactory
//                .select(Projections.constructor(StoreLocationDto.class,
//                        store.seq,
//                        store.name,
//                        storeLocationInfoTbl.lat,
//                        storeLocationInfoTbl.lng
//                ))
//                .from(storeLocationInfoTbl)
//                .innerJoin(storeLocationInfoTbl.store, store)
//                .fetch();
//    }

//    /**
//     * Store 테이블에 tel, url 정보를 업데이트 해주는 함수 (일괄 업데이트)
//     * @param updateMap
//     */
//    private void bulkUpdateTelAndUrl(Map<Long, DetailInfo> updateMap) {
//        List<Long> ids = new ArrayList<>(updateMap.keySet());
//
//        List<Store> stores = storeRepository.findAllById(ids); /* SELECT IN 쿼리 한 번 */
//
//        /* tel, url 컬럼 업데이트 */
//        for (Store store : stores) {
//            DetailInfo info = updateMap.get(store.getSeq());
//            store.updateContactInfo(info.tel(), info.url(), "Store>UpdateStoreDetail");
//        }
//    }

//    /**
//     * Store 테이블을 StoreLocationDto 존재하는 데이터를 기반으로 update 해주는 함수
//     * @param storeLocationDtos
//     * @return
//     */
//    private Optional<JsonNode> modifyStoreDetail(List<StoreLocationDto> storeLocationDtos) {
//        ObjectMapper mapper = new ObjectMapper();
//        int successCount = 0;
//
//        if (!storeLocationDtos.isEmpty()) {
//            Map<Long, DetailInfo> updateMap = new HashMap<>();
//
//            for (StoreLocationDto store : storeLocationDtos) {
//                inputDetailQuery(store.getName(), store.getLng(), store.getLat())
//                        .ifPresent(jsonNode -> {
//                            Optional<DetailInfo> detailOpt = parseDetailInfo(jsonNode);
//                            detailOpt.ifPresent(detail -> {
//                                updateMap.put(store.getSeq(), detail);
//                            });
//                        });
//            }
//
//            if (!updateMap.isEmpty()) {
//                bulkUpdateTelAndUrl(updateMap);
//                successCount += updateMap.size();
//            }
//        }
//
//        ObjectNode result = mapper.createObjectNode()
//                .put("success", successCount > 0)
//                .put("successCount", successCount);
//
//        return Optional.of(result);
//    }


    public List<StoreDto> findAllStores() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<StoreDto> findStoreById(Long id) {
        return storeRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<StoreLocationInfoTbl> findStoreLocationInfo(Long seq) {
        return storeLocationInfoRepository.findById(seq);
    }

    public StoreDto createStore(StoreDto dto) {
        Store store = new Store(dto);
        store = storeRepository.save(store);
        return convertToDto(store);
    }

    public StoreDto modifyStore(Long id, StoreDto dto) {
        Optional<Store> optionalStore = storeRepository.findById(id);

        Instant nowInstant = Instant.now();
        Date now = Date.from(nowInstant);

        if (optionalStore.isPresent()) {
            Store store = optionalStore.get();
            store.updateStore(dto, "modifyStore");
            store = storeRepository.save(store);
            return convertToDto(store);
        } else {
            return null; // 혹은 예외 처리
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean isAddedStore(AddStoreDto addStoreDto) throws Exception {

        if (addStoreDto == null) {
            throw new IllegalArgumentException("[Error][StoreService->addStore] AddStoreDto object is null.");
        }
        if (addStoreDto.getName() == null || addStoreDto.getName().isEmpty()) {
            throw new IllegalArgumentException("[Error][StoreService->addStore] The store name is missing.");
        }
        if (addStoreDto.getType() == null || addStoreDto.getType().isEmpty()) {
            throw new IllegalArgumentException("[Error][StoreService->addStore] The store type is missing.");
        }

        /* Store 객체*/
        Store newStore = new Store(addStoreDto);
        Long newStoreSeq = storeRepository.save(newStore).getSeq();
        //locationService.inputStoreLocationInfoTbl(addStoreDto, newStoreSeq, now);


        /* 비플페이 등록 업체라면 */
        if (addStoreDto.getIsBeefulPay()) {
            //locationService.inputZeroPossibleMarket(addStoreDto, newStoreSeq, now);
        }

        /* 음식점-타입 데이터 */
        //locationService.inputStoreTypeLinkTbl(addStoreDto, newStoreSeq, now);

        return true;
    }

//    public List<StoreTypeMajorDto> findStoreTypeMajors() throws Exception {
//
//        List<StoreTypeMajorDto> storeMajors = redisAdapter.getValue(categoryMain, new TypeReference<List<StoreTypeMajorDto>>() {});
//
//        if (storeMajors == null || storeMajors.isEmpty()) {
//
//            var query = queryFactory
//                    .select(storeTypeMajor)
//                    .from(storeTypeMajor);
//
//            List<StoreTypeMajor> storeMajorsDb = query.fetch();
//
//            return storeMajorsDb.stream()
//                    .map(this::convertTypeMajorToDto)
//                    .collect(Collectors.toList());
//
//        } else {
//            return storeMajors;
//        }
//    }

//    public List<StoreTypeSubDto> findStoreTypeSubs(Long majorType) throws Exception {
//
//        if (majorType == null || majorType <= 0) {
//            log.error("[Error][StoreService->getStoreTypeSubs] `majorType` must be at least 1 natural number.");
//            return Collections.emptyList();
//        }
//
//        String storeSubKey = String.format("%s:%s", categorySub, majorType);
//        List<StoreTypeSubDto> storeTypeSubs = redisAdapter.getValue(storeSubKey, new TypeReference<List<StoreTypeSubDto>>() {});
//
//        if (storeTypeSubs == null || storeTypeSubs.isEmpty()) {
//            /* Redis 에서 데이터를 못가져오거나 데이터가 존재하지 않을 경우 */
//            var query = queryFactory
//                    .select(storeTypeSub)
//                    .from(storeTypeSub)
//                    .join(storeTypeSub.storeTypeMajor, storeTypeMajor)
//                    .where(
//                            new BooleanBuilder()
//                                    .and(storeTypeSub.majorType.eq(majorType))
//                    );
//
//            List<StoreTypeSub> storeTypeSubsDb = query.fetch();
//
//            return storeTypeSubsDb.stream()
//                    .map(this::convertTypeSubToDto)
//                    .collect(Collectors.toList());
//        } else {
//            return storeTypeSubs;
//        }
//    }

//    public Optional<JsonNode> inputDetailQuery(String storeName , BigDecimal lng, BigDecimal lat)
//    {
//        if (storeName == null || storeName.isEmpty()) {
//            return Optional.empty();
//        }
//
//        String categoryGroupCode = "FD6";
//        String page = "1";
//        String size = "1";
//
//        UriComponentsBuilder builder = UriComponentsBuilder
//                .fromUriString(KAKAO_SEARCH_API_URL)
//                .queryParam("page", page)
//                .queryParam("size", size)
//                .queryParam("category_group_code", categoryGroupCode)
//                .queryParam("query", storeName);
//
//        if (lng != null && lat != null &&
//            lng.compareTo(BigDecimal.ZERO) > 0 && lat.compareTo(BigDecimal.ZERO) > 0) {
//            builder.queryParam("x", lng)
//                   .queryParam("y", lat);
//        }
//
//        URI apiuri = builder
//                .encode(StandardCharsets.UTF_8)
//                .build()
//                .toUri();
//
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization",KAKAO_SEARCH_HEADER);
//        HttpEntity<Void> request = new HttpEntity<>(headers);
//
//        ResponseEntity<JsonNode> resp = resttemplate.exchange(
//                apiuri, HttpMethod.GET, request, JsonNode.class
//        );
//
//        if (resp.getStatusCode().is2xxSuccessful() && resp.hasBody()) {
//            return Optional.of(resp.getBody());
//        } else {
//            return Optional.empty();
//        }
//    }

//    @Transactional(rollbackFor = Exception.class)
//    public Optional<JsonNode> modifyAllStoreDetail() {
//        List<StoreLocationDto> allStore = findTelUrlAllData();
//        return modifyStoreDetail(allStore);
//    }

//    @Transactional(rollbackFor = Exception.class)
//    public Optional<JsonNode> modifyEmptyStoreDetail() {
//        /* tel, url 이 없는 Store 정보만 쿼리한다.*/
//        List<StoreLocationDto> emptyStore = findTelUrlEmptyData();
//        return modifyStoreDetail(emptyStore);
//    }

//    @Transactional(rollbackFor = Exception.class)
//    public StoreDto modifySingleStoreDetail(long id, String tel, String url) {
//
//        if (id <= 0) {
//            throw new IllegalArgumentException("Invalid store ID");
//        }
//
//        Optional<StoreDto> optionalStore = findStoreById(id);
//        optionalStore.orElseThrow(() -> new NoSuchElementException("Store not found"));
//
//        StoreDto storeDto = optionalStore.get();
//        storeDto.setTel(tel);
//        storeDto.setUrl(url);
//        storeDto.setChgId("Store>UpdateStoreDetail");
//
//        return modifyStore(id,storeDto);
//    }

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
    private Optional<List<KakaoStoreDto>> getKakaoStoreDtoFromKakaoApi(
            String storeName, int page, int size,
            BigDecimal pLat, BigDecimal pLng) {

        if (storeName == null || storeName.isEmpty()) {
            return Optional.empty();
        }

        List<KakaoStoreDto> result = new ArrayList<>();

        /* category 우선순위: FD6(음식점) -> CE7(카페) */
        List<String> categories = List.of("FD6", "CE7");

        for (String category : categories) {
            URI uri = buildKakaoApiUri(storeName, page, size, pLat, pLng, category);
            JsonNode root = fetchKakaoApiResponse(uri);

            if (root != null) {
                List<KakaoStoreDto> parsed = parseKakaoDocuments(root.path("documents"));
                result.addAll(parsed);
            }

            if (!result.isEmpty()) break; /* 첫 번째 성공 시, 반복 중지 */
        }

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }



//    private Optional<List<KakaoStoreDto>> getKakaoStoreDtoFromKakaoApi(
//            String storeName, int page, int size,
//            BigDecimal pLat, BigDecimal pLng)
//    {
//
//        if (storeName == null || storeName.isEmpty()) {
//            return Optional.empty();
//        }
//
//        List<KakaoStoreDto> kakaoStoreDtos = new ArrayList<>();
//
//        List<String> categoryList = List.of("FD6", "CE7");
//
//        for (String category : categoryList) {
//
//            UriComponentsBuilder builder = UriComponentsBuilder
//                    .fromUriString(KAKAO_SEARCH_API_URL)
//                    .queryParam("category_group_code", category)
//                    .queryParam("query", storeName);
//
//            if (page != 0) builder.queryParam("page", page);
//            if (size != 0) builder.queryParam("size", size);
//
//            if (pLat != null && pLng != null) {
//                builder
//                        .queryParam("y", pLat)
//                        .queryParam("x", pLng);
//            }
//
//            URI apiuri = builder
//                    .encode(StandardCharsets.UTF_8)
//                    .build()
//                    .toUri();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Authorization",KAKAO_SEARCH_HEADER);
//            HttpEntity<Void> request = new HttpEntity<>(headers);
//
//            ResponseEntity<JsonNode> resp = resttemplate.exchange(
//                    apiuri, HttpMethod.GET, request, JsonNode.class);
//
//            JsonNode root = resp.getBody();
//
//            if (resp.getStatusCode().is2xxSuccessful() && resp.hasBody() && root != null) {
//                JsonNode documents = root.path("documents");
//
//                if (documents.isArray() && !documents.isEmpty()) {
//                    ArrayNode documentsArray = (ArrayNode)documents;
//
//                    for (JsonNode doc : documentsArray) {
//                        BigDecimal lat = new BigDecimal(doc.path("y").asText());
//                        BigDecimal lng = new BigDecimal(doc.path("x").asText());
//
//                        KakaoStoreDto dto = KakaoStoreDto.builder()
//                                .addressName(doc.path("address_name").asText(null))
//                                .categoryGroupName(doc.path("category_group_name").asText(null))
//                                .categoryGroupCode(doc.path("category_group_code").asText(null))
//                                .categoryName(doc.path("category_name").asText(null))
//                                .phone(doc.path("phone").asText(null))
//                                .placeUrl(doc.path("place_url").asText(null))
//                                .placeName(doc.path("place_name").asText(null))
//                                .roadAddressName(doc.path("road_address_name").asText(null))
//                                .lat(lat)
//                                .lng(lng)
//                                .build();
//
//                        kakaoStoreDtos.add(dto);
//                    }
//                }
//            }
//        }
//
//        return Optional.of(kakaoStoreDtos);
//    }

    private Optional<Store> createStoreFromKakaoStore(KakaoStoreDto kakaoStoreDto) {
        /* 이미 존재하는 음식점인지 체크 */
        Store existsStore = queryFactory
                .select(storeLocationInfoTbl.store)
                .from(storeLocationInfoTbl)
                .join(storeLocationInfoTbl.store, store)
                .where(
                        store.name.eq(kakaoStoreDto.getPlaceName()),
                        storeLocationInfoTbl.lat.eq(kakaoStoreDto.getLat()),
                        storeLocationInfoTbl.lng.eq(kakaoStoreDto.getLng())
                )
                .fetchFirst();

        if (existsStore == null) {
            Store createStore = new Store(kakaoStoreDto, "createStoreFromKakao");
            storeRepository.save(createStore);
            return Optional.of(createStore);
        } else {
            log.warn("[WARN][StoreServiceImpl->createStoreFromKakaoStore] It's a store that already exists.: {}", kakaoStoreDto);
            return Optional.empty();
        }
    }

    private void inputZeroPossibleTbl(Store store) {
        ZeroPossibleMarket zeroPossibleMarket = new ZeroPossibleMarket(store, "inputZeroPossibleTbl");
        zeroPossibleMarketRepository.save(zeroPossibleMarket);
    }

    private void inputStoreLocationInfo(KakaoStoreDto kakaoStoreDto, Store store) {
        StoreLocationInfoTbl storeLocationInfo = new StoreLocationInfoTbl(kakaoStoreDto, store, "inputNewStore");
        storeLocationInfoRepository.save(storeLocationInfo);
    }

    private void inputStoreLocationRoadInfo(KakaoStoreDto kakaoStoreDto, Store store) {
        StoreLocationRoadInfoTbl storeLocationRoadInfoTbl = new StoreLocationRoadInfoTbl(kakaoStoreDto, store, "inputStoreLocationRoad");
        storeLocationRoadInfoTbl.markAsNew();
        storeLocationRoadInfoRepository.save(storeLocationRoadInfoTbl);
    }

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

    private void inputStoreCategory(Store store, CategoryTbl categoryTbl) {
        StoreCategoryTbl storeCategoryTbl = new StoreCategoryTbl(store, categoryTbl, "inputStoreCategory");
        storeCategoryRepository.save(storeCategoryTbl);
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus inputNewStore(String storeName, Integer page, Integer size, BigDecimal pLat, BigDecimal pLng, Boolean zeroYn) {

        int inputPage = page == null ? 1 : page;
        int inputSize = size == null ? 1 : size;

        Optional<List<KakaoStoreDto>> kakaoStoreDto =
                getKakaoStoreDtoFromKakaoApi(storeName, inputPage, inputSize, pLat, pLng);

        if (kakaoStoreDto.isPresent()) {
            List<KakaoStoreDto> kakaoStoreDtos = kakaoStoreDto.get();

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


        return PublicStatus.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicStatus modifyExistsStores() {

        /* StoreLocationInfoTbl + Store join 한 결과 리스트 */
        List<StoreLocationInfoTbl> existsLocations = queryFactory
                .selectFrom(storeLocationInfoTbl)
                .join(storeLocationInfoTbl.store, store).fetchJoin()
                .fetch();

        for (StoreLocationInfoTbl location: existsLocations) {
            /* location 객체를 Kakao api 호출하여 */
            Optional<List<KakaoStoreDto>> kakaoStoreDtoOpts =
                getKakaoStoreDtoFromKakaoApi(location.getStore().getName(), 1, 1, location.getLat(), location.getLng());

            if (kakaoStoreDtoOpts.isPresent()) {
                List<KakaoStoreDto> kakaoStoreDtos = kakaoStoreDtoOpts.get();

                if (kakaoStoreDtos.size() == 1) {
                    KakaoStoreDto kakaoStoreDto = kakaoStoreDtos.get(0);

                    Store modifyStore = location.getStore();
                    modifyStore.updateStoreFromKakaoStoreDto(kakaoStoreDto, "modifyExistsStores");

                    location.modifyStoreLocationInfoFromKakaoStoreDto(kakaoStoreDto, "modifyExistsStores");

                    inputStoreLocationRoadInfo(kakaoStoreDto, modifyStore);

                    CategoryTbl categoryTbl = createOrFindCategoryTbl(kakaoStoreDto);

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
}