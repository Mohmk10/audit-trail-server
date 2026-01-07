package com.mohmk10.audittrail.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.mohmk10.audittrail.search.adapter.out.elasticsearch.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        String host = elasticsearchUri
                .replace("http://", "")
                .replace("https://", "")
                .replaceAll("/.*$", "");

        boolean useSsl = elasticsearchUri.startsWith("https://");
        boolean hasCredentials = username != null && !username.isEmpty()
                && password != null && !password.isEmpty();

        if (useSsl && hasCredentials) {
            return ClientConfiguration.builder()
                    .connectedTo(host)
                    .usingSsl()
                    .withBasicAuth(username, password)
                    .withConnectTimeout(Duration.ofSeconds(10))
                    .withSocketTimeout(Duration.ofSeconds(30))
                    .build();
        } else if (useSsl) {
            return ClientConfiguration.builder()
                    .connectedTo(host)
                    .usingSsl()
                    .withConnectTimeout(Duration.ofSeconds(10))
                    .withSocketTimeout(Duration.ofSeconds(30))
                    .build();
        } else if (hasCredentials) {
            return ClientConfiguration.builder()
                    .connectedTo(host)
                    .withBasicAuth(username, password)
                    .withConnectTimeout(Duration.ofSeconds(10))
                    .withSocketTimeout(Duration.ofSeconds(30))
                    .build();
        } else {
            return ClientConfiguration.builder()
                    .connectedTo(host)
                    .withConnectTimeout(Duration.ofSeconds(10))
                    .withSocketTimeout(Duration.ofSeconds(30))
                    .build();
        }
    }
}
