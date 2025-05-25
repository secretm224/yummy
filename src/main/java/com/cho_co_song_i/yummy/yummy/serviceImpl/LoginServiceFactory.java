package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LoginServiceFactory {
    private final Map<OauthChannelStatus, LoginService> serviceMap;

    @Autowired
    public LoginServiceFactory(List<LoginService> services) {
        this.serviceMap = services.stream()
                .collect(Collectors.toMap(LoginService::getOauthChannel, Function.identity()));
    }

    public LoginService getService(String channel) {
        return serviceMap.get(OauthChannelStatus.valueOf(channel));
    }

    public LoginService getService(OauthChannelStatus channel) {
        return serviceMap.get(channel);
    }
}
