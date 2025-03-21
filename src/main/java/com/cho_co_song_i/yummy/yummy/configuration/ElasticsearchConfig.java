package com.cho_co_song_i.yummy.yummy.configuration;


import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Stream;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String[] uris;

    @Value("${spring.elasticsearch.username}")
    private String userName;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    public ElasticsearchAsyncClient elasticAsyncClient() {
        BasicCredentialsProvider credentialProvider = new BasicCredentialsProvider();
        credentialProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));

        /*
            ====== Elasticsearch Server URL 설정 ======
            1) uris 는 String[] 타입의 배열이다.
            uris = ["http://221.149.34.65:2025", "http://221.149.34.65:2026", "http://221.149.34.65:2027"]
            이 배열을 스트림으로 변환한 후, HttpHost.create(uri) 로 각각의 문자열 URL 을 HttpHost 객체로 반환한 후 배열로 바꾸는 과정

            2) .setHttpClientConfigCallback() 는 비동기 클라이언트 설정을 구성하는 부분
            -> RestClientBuilder.HttpClientConfigCallback 인터페이스를 구현하는 익명 클래스를 정의.
            -> 인증정보, 다른 커스텀 설정을 구성 가능
            -> 타임아웃 설정, 헤더 추가, SSL 인증서 설정 등 다양한 설정 구성가능
            ** -> credentialProvider 를 매개변수로 넣고 있다.
        */
        RestClientBuilder builder = RestClient.builder(
                Stream.of(uris).map(HttpHost::create).toArray(HttpHost[]::new)
        ).setHttpClientConfigCallback(
                httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialProvider)
        );

        RestClient restClient = builder.build();

        /*
            커스터마이징한 오브젝트 맵퍼 생성 -> Search 결과를 DTO에 맵핑하기 위함.
        */
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JacksonJsonpMapper jsonMapper = new JacksonJsonpMapper(mapper);
        RestClientTransport transport = new RestClientTransport(restClient, jsonMapper);

        return new ElasticsearchAsyncClient(transport);
    }
}