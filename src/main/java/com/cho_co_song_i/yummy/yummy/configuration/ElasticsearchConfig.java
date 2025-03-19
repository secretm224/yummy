package com.cho_co_song_i.yummy.yummy.configuration;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ElasticsearchConfig {
    @Value("${spring.elasticsearch.uris}")
    private String esUris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    private RestHighLevelClient client;  // 싱글톤 인스턴스를 유지하기 위한 변수

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        /* 문자열로 된 ES 노드 주소를 HttpHost 배열로 변환 */
        List<HttpHost> hosts = Arrays.stream(esUris.split(","))
                .map(url -> {
                    String[] parts = url.split(":");
                    String host = parts[1].replace("//", ""); // "http://" 제거
                    int port = Integer.parseInt(parts[2]);
                    return new HttpHost(host, port, "http");
                })
                .toList();

        /* 인증 정보 설정 */
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        /* RestHighLevelClient 생성 (싱글톤) */
        client = new RestHighLevelClient(
                RestClient.builder(hosts.toArray(new HttpHost[0]))
                        .setHttpClientConfigCallback(httpClientBuilder ->
                                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
        );

        return client;
    }

    /* ✅ 애플리케이션 종료 시 안전하게 클라이언트 닫기 */
    @PreDestroy
    public void closeClient() {
        try {
            if (client != null) {
                client.close();
                System.out.println("✅ Elasticsearch 클라이언트가 정상적으로 종료되었습니다.");
            }
        } catch (IOException e) {
            System.err.println("❌ Elasticsearch 클라이언트 종료 중 오류 발생: " + e.getMessage());
        }
    }

}
