package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.entity.Store;
import com.cho_co_song_i.yummy.yummy.entity.StoreLocationInfoTbl;
import com.cho_co_song_i.yummy.yummy.entity.StoreTypeMajor;
import com.cho_co_song_i.yummy.yummy.entity.StoreTypeSub;
import com.cho_co_song_i.yummy.yummy.repository.StoreRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.cho_co_song_i.yummy.yummy.entity.QStoreTypeMajor.storeTypeMajor;
import static com.cho_co_song_i.yummy.yummy.entity.QStoreTypeSub.storeTypeSub;
import com.cho_co_song_i.yummy.yummy.repository.StoreLocationInfoRepository;

@Service
@Slf4j
public class StoreService {

    @PersistenceContext
    private EntityManager entityManager;

    private final JPAQueryFactory queryFactory;
    private final LocationService locationService;
    private final StoreRepository storeRepository;
    private final RedisService redisService;
    private final RestTemplate resttemplate;
    private final StoreLocationInfoRepository storeLocationInfoRepository;


    /* Redis Cache 관련 필드 */
    @Value("${spring.redis.category_main}")
    private String categoryMain;
    @Value("${spring.redis.category_sub}")
    private String categorySub;

    private static final String KAKAO_SEARCH_API_URL =
            "https://dapi.kakao.com/v2/local/search/keyword.json";


    public StoreService(StoreRepository storeRepository, LocationService locationService,
                        JPAQueryFactory queryFactory, RedisService redisService, RestTemplate resttemplate,
                        StoreLocationInfoRepository storeLocationInfoRepository) {
        this.storeRepository = storeRepository;
        this.locationService = locationService;
        this.queryFactory = queryFactory;
        this.redisService = redisService;
        this.resttemplate = resttemplate;
        this.storeLocationInfoRepository = storeLocationInfoRepository;
    }

    // Entity -> DTO 변환
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

    // DTO -> Entity 변환
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

    public List<StoreDto> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<StoreDto> getStoreById(Long id) {
        return storeRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<StoreLocationInfoTbl> getStoreLocationInfo(Long seq){
        return storeLocationInfoRepository.findById(seq);
    }

    public StoreDto createStore(StoreDto dto) {
        Store store = convertToEntity(dto);
        store = storeRepository.save(store);
        return convertToDto(store);
    }

    public StoreDto updateStore(Long id, StoreDto dto) {
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

    /* Entity -> DTO 변환 (StorTypeMajor) */
    private StoreTypeMajorDto convertTypeMajorToDto(StoreTypeMajor storeTypeMajor) {
        return new StoreTypeMajorDto(
                storeTypeMajor.getMajorType(),
                storeTypeMajor.getTypeName()
        );
    }

    /* Entity -> DTO 변환 (StorTypeSub) */
    private StoreTypeSubDto convertTypeSubToDto(StoreTypeSub storeTypeSub) {
        return new StoreTypeSubDto(
                storeTypeSub.getSubType(),
                storeTypeSub.getMajorType(),
                storeTypeSub.getTypeName()
        );
    }

    /**
     * Store 객체를 디비에 저장해주는 함수
     * @param addStoreDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean addStore(AddStoreDto addStoreDto) {

        /* 현재 시각 */
        Instant nowInstant = Instant.now();
        Date now = Date.from(nowInstant);

        try{

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
            locationService.addStoreLocationInfoTbl(addStoreDto, newStoreSeq, now);


            /* 비플페이 등록 업체라면 */
            if (addStoreDto.getIsBeefulPay()) {
                locationService.addZeroPossibleMarket(addStoreDto, newStoreSeq, now);
            }

            /* 음식점-타입 데이터 */
            locationService.addStoreTypeLinkTbl(addStoreDto, newStoreSeq, now);

            return true;
        } catch(Exception e) {
            log.error("[Error][StoreService->addStore] {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    /**
     * 음식점 대분류 데이터를 가져와주는 함수
     * @return
     */
    public List<StoreTypeMajorDto> getStoreTypeMajors() {

        List<StoreTypeMajorDto> storeMajors = new ArrayList<>();

        try {
            storeMajors = redisService.getValue(categoryMain, new TypeReference<List<StoreTypeMajorDto>>() {});
        } catch(Exception e) {
            log.error("[Error][StoreService->getStoreTypeMajors] {}", e.getMessage(), e);
        }

        if (storeMajors == null || storeMajors.isEmpty()) {

            var query = queryFactory
                    .select(storeTypeMajor)
                    .from(storeTypeMajor);

            try {
                List<StoreTypeMajor> storeMajorsDb = query.fetch();

                return storeMajorsDb.stream()
                        .map(this::convertTypeMajorToDto)
                        .collect(Collectors.toList());

            } catch(Exception e) {
                log.error("[Error][StoreService->getStoreTypeMajors] {}", e.getMessage(), e);
                return Collections.emptyList();
            }

        } else {
            return storeMajors;
        }
    }

    public List<StoreTypeSubDto> getStoreTypeSubs(Long majorType) {

        if (majorType == null || majorType <= 0) {
            log.error("[Error][StoreService->getStoreTypeSubs] `majorType` must be at least 1 natural number.");
            return Collections.emptyList();
        }

        List<StoreTypeSubDto> storeTypeSubs = new ArrayList<>();

        try {
            String storeSubKey = String.format("%s:%s", categorySub, majorType);
            storeTypeSubs = redisService.getValue(storeSubKey, new TypeReference<List<StoreTypeSubDto>>() {});
        } catch(Exception e) {
            log.error("[Error][StoreService->getStoreTypeSubs] {}", e.getMessage(), e);
        }

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

            try {
                List<StoreTypeSub> storeTypeSubsDb = query.fetch();

                return storeTypeSubsDb.stream()
                        .map(this::convertTypeSubToDto)
                        .collect(Collectors.toList());

            } catch(Exception e) {
                log.error("[Error][StoreService->getStoreTypeSubs] {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        } else {
            return storeTypeSubs;
        }
    }

    // 비즈니스 앱 등록 이슈 해결 후 처리 예정
    public Optional<JsonNode> StoreDetailQuery(String storeName ,String lngX, String latY)
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

        if (StringUtils.hasText(lngX) && StringUtils.hasText(latY)) {
            builder.queryParam("x", lngX)
                   .queryParam("y", latY);
        }

        URI apiuri = builder
//                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();


        try{
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization","KakaoAK 2fcfa96247ae04a4ad26cd853f1e5551");
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> resp = resttemplate.exchange(
                    apiuri, HttpMethod.GET, request, JsonNode.class
            );

            if (resp.getStatusCode().is2xxSuccessful() && resp.hasBody()) {
                return Optional.of(resp.getBody());
            }else{
                return Optional.empty();
            }
        }catch(Exception e){
            return Optional.empty();
        }
    }

    public Optional<JsonNode> UpdateStoreDetail(){
        List<StoreDto> l_store = this.getAllStores();
        AtomicInteger successCount = new AtomicInteger(0);
        ObjectMapper mapper = new ObjectMapper();

        if(!l_store.isEmpty()) {
            for (StoreDto store : l_store) {
                Long storeSeq = store.getSeq();
                StoreLocationInfoDto store_location = new StoreLocationInfoDto();
                if(storeSeq > 0){
                    this.getStoreLocationInfo(storeSeq).ifPresent(loc -> {
                        store_location.setLng(loc.getLng());
                        store_location.setLat(loc.getLat());
                    });
                }

                this.StoreDetailQuery(store.getName(),store_location.getLng().toString(),
                                                      store_location.getLat().toString()).
                                                      ifPresent(jsonNode -> {
                                                          //카테고리 추가 예정
                                                          String tel = jsonNode.get("tel").asText();
                                                          String url = jsonNode.get("url").asText();
                                                          //updateStore
                                                          store.setTel(tel);
                                                          store.setUrl(url);

                                                          StoreDto update_dto = this.updateStore(store.getSeq(),store);
                                                          if(update_dto != null){
                                                              successCount.incrementAndGet();
                                                          }
                                                      });

            }

        }
        ObjectNode result = mapper.createObjectNode()
                                  .put("success", successCount.get() > 0)
                                  .put("successCount", successCount.get());

        return Optional.of(result);
    }

}