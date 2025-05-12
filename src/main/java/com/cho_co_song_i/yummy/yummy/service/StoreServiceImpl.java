package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.adapter.redis.RedisAdapter;
import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.Store;
import com.cho_co_song_i.yummy.yummy.entity.StoreLocationInfoTbl;
import com.cho_co_song_i.yummy.yummy.entity.StoreTypeMajor;
import com.cho_co_song_i.yummy.yummy.entity.StoreTypeSub;
import com.cho_co_song_i.yummy.yummy.repository.StoreLocationInfoRepository;
import com.cho_co_song_i.yummy.yummy.repository.StoreRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.querydsl.core.BooleanBuilder;
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
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.cho_co_song_i.yummy.yummy.entity.QStoreTypeMajor.storeTypeMajor;
import static com.cho_co_song_i.yummy.yummy.entity.QStoreTypeSub.storeTypeSub;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    @PersistenceContext
    private EntityManager entityManager;

    private final JPAQueryFactory queryFactory;
    private final LocationService locationService;
    private final StoreRepository storeRepository;
    private final RedisAdapter redisAdapter;
    private final RestTemplate resttemplate;
    private final StoreLocationInfoRepository storeLocationInfoRepository;


    /* Redis Cache 관련 필드 */
    @Value("${spring.redis.category_main}")
    private String categoryMain;
    @Value("${spring.redis.category_sub}")
    private String categorySub;

    private static final String KAKAO_SEARCH_API_URL =
            "https://dapi.kakao.com/v2/local/search/keyword.json";

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
     * DTO -> Entity 변환
     * @param dto
     * @return
     */
    private Store convertToEntity(StoreDto dto) {
        Store store = new Store();
        store.setName(dto.getName());
        store.setType(dto.getType());
        store.setUseYn(dto.getUseYn());
        store.setRegDt(dto.getRegDt());
        store.setRegId(dto.getRegId());
        store.setChgDt(dto.getChgDt());
        store.setChgId(dto.getChgId());
        store.setTel(dto.getTel());
        store.setUrl(dto.getUrl());
        return store;
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
        Store store = convertToEntity(dto);
        store = storeRepository.save(store);
        return convertToDto(store);
    }

    public StoreDto modifyStore(Long id, StoreDto dto) {
        Optional<Store> optionalStore = storeRepository.findById(id);

        Instant nowInstant = Instant.now();
        Date now = Date.from(nowInstant);

        if (optionalStore.isPresent()) {
            Store store = optionalStore.get();
            store.setName(dto.getName());
            store.setType(dto.getType());
            store.setUseYn(dto.getUseYn());
            store.setRegDt(dto.getRegDt());
            store.setRegId(dto.getRegId());
            store.setChgDt(now);
            store.setChgId(dto.getChgId());
            store.setTel(dto.getTel());
            store.setUrl(dto.getUrl());

            store = storeRepository.save(store);
            return convertToDto(store);
        } else {
            return null; // 혹은 예외 처리
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean isAddedStore(AddStoreDto addStoreDto) throws Exception {

        /* 현재 시각 */
        Instant nowInstant = Instant.now();
        Date now = Date.from(nowInstant);

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
        Store newStore = new Store();
        newStore.setName(addStoreDto.getName());
        newStore.setType(addStoreDto.getType());
        newStore.setUseYn('Y');
        newStore.setRegDt(now);
        newStore.setRegId("system");
        newStore.setTel(addStoreDto.getTel());
        newStore.setUrl(addStoreDto.getUrl());
        newStore.markAsNew();

        Long newStoreSeq = storeRepository.save(newStore).getSeq();
        locationService.inputStoreLocationInfoTbl(addStoreDto, newStoreSeq, now);


        /* 비플페이 등록 업체라면 */
        if (addStoreDto.getIsBeefulPay()) {
            locationService.inputZeroPossibleMarket(addStoreDto, newStoreSeq, now);
        }

        /* 음식점-타입 데이터 */
        locationService.inputStoreTypeLinkTbl(addStoreDto, newStoreSeq, now);

        return true;
    }

    public List<StoreTypeMajorDto> findStoreTypeMajors() throws Exception {

        List<StoreTypeMajorDto> storeMajors = redisAdapter.getValue(categoryMain, new TypeReference<List<StoreTypeMajorDto>>() {});

        if (storeMajors == null || storeMajors.isEmpty()) {

            var query = queryFactory
                    .select(storeTypeMajor)
                    .from(storeTypeMajor);

            List<StoreTypeMajor> storeMajorsDb = query.fetch();

            return storeMajorsDb.stream()
                    .map(this::convertTypeMajorToDto)
                    .collect(Collectors.toList());

        } else {
            return storeMajors;
        }
    }

    public List<StoreTypeSubDto> findStoreTypeSubs(Long majorType) throws Exception {

        if (majorType == null || majorType <= 0) {
            log.error("[Error][StoreService->getStoreTypeSubs] `majorType` must be at least 1 natural number.");
            return Collections.emptyList();
        }

        String storeSubKey = String.format("%s:%s", categorySub, majorType);
        List<StoreTypeSubDto> storeTypeSubs = redisAdapter.getValue(storeSubKey, new TypeReference<List<StoreTypeSubDto>>() {});

        if (storeTypeSubs == null || storeTypeSubs.isEmpty()) {
            /* Redis 에서 데이터를 못가져오거나 데이터가 존재하지 않을 경우 */
            var query = queryFactory
                    .select(storeTypeSub)
                    .from(storeTypeSub)
                    .join(storeTypeSub.storeTypeMajor, storeTypeMajor)
                    .where(
                            new BooleanBuilder()
                                    .and(storeTypeSub.majorType.eq(majorType))
                    );

            List<StoreTypeSub> storeTypeSubsDb = query.fetch();

            return storeTypeSubsDb.stream()
                    .map(this::convertTypeSubToDto)
                    .collect(Collectors.toList());
        } else {
            return storeTypeSubs;
        }
    }

    // 비즈니스 앱 등록 이슈 해결 후 처리 예정
    public Optional<JsonNode> inputDetailQuery(String storeName , BigDecimal lngX, BigDecimal latY)
    {
        if (storeName == null || storeName.isEmpty()) {
            return Optional.empty();
        }

        String apiKey = "2fcfa96247ae04a4ad26cd853f1e5551";
        String categoryGroupCode = "FD6";
        String page = "1";
        String size = "1";

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(KAKAO_SEARCH_API_URL)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("category_group_code", categoryGroupCode)
                .queryParam("query", storeName);

        if (lngX != null && latY != null &&
            lngX.compareTo(BigDecimal.ZERO) > 0 && latY.compareTo(BigDecimal.ZERO) > 0) {
            builder.queryParam("x", lngX)
                   .queryParam("y", latY);
        }

        URI apiuri = builder
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();


        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","KakaoAK 2fcfa96247ae04a4ad26cd853f1e5551");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> resp = resttemplate.exchange(
                apiuri, HttpMethod.GET, request, JsonNode.class
        );

        if (resp.getStatusCode().is2xxSuccessful() && resp.hasBody()) {
            return Optional.of(resp.getBody());
        } else {
            return Optional.empty();
        }
    }

    public Optional<JsonNode> modifyStoreDetail() {
        List<StoreDto> l_store = this.findAllStores();
        AtomicInteger successCount = new AtomicInteger(0);
        ObjectMapper mapper = new ObjectMapper();

        if(!l_store.isEmpty()) {
            for (StoreDto store : l_store) {
                Long storeSeq = store.getSeq();
                StoreLocationInfoDto store_location = new StoreLocationInfoDto();
                if(storeSeq > 0){
                    this.findStoreLocationInfo(storeSeq).ifPresent(loc -> {
                        store_location.setSeq(store.getSeq());
                        store_location.setLng(loc.getLng());
                        store_location.setLat(loc.getLat());
                    });
                }

                this.inputDetailQuery(store.getName(),store_location.getLng(),
                                                      store_location.getLat()).
                                                      ifPresent(jsonNode -> {
                                                          //카테고리 추가 예정
                                                          if (jsonNode.has("documents") && jsonNode.get("documents").isArray() && jsonNode.get("documents").size() > 0) {
                                                              JsonNode firstDoc = jsonNode.get("documents").get(0);

                                                              String tel = Optional.ofNullable(firstDoc.get("phone"))
                                                                      .map(JsonNode::asText)
                                                                      .orElse(null);

                                                              String url = Optional.ofNullable(firstDoc.get("place_url"))
                                                                      .map(JsonNode::asText)
                                                                      .orElse(null);

                                                              store.setTel(tel);
                                                              store.setUrl(url);
                                                              store.setChgId("Store>UpdateStoreDetail");

                                                              StoreDto update_dto = this.modifyStore(store.getSeq(),store);
                                                              if(update_dto != null){
                                                                  successCount.incrementAndGet();
                                                              }
                                                          }
                                                          //updateStore
                                                      });

            }

        }

        ObjectNode result = mapper.createObjectNode()
                                  .put("success", successCount.get() > 0)
                                  .put("successCount", successCount.get());

        return Optional.of(result);
    }

}