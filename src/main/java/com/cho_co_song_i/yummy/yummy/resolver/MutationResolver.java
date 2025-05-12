package com.cho_co_song_i.yummy.yummy.resolver;

import com.cho_co_song_i.yummy.yummy.dto.AddStoreDto;
import com.cho_co_song_i.yummy.yummy.service.StoreService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MutationResolver {
    private final StoreService storeService;

    @MutationMapping
    public Boolean addStore(@Argument("addStoreDto") AddStoreDto addStoreDto) throws Exception {
        return storeService.isAddedStore(addStoreDto);
    }

    @MutationMapping
    public Optional<JsonNode> UpdateStoreDetail() {
        return storeService.modifyStoreDetail();
    }
}
