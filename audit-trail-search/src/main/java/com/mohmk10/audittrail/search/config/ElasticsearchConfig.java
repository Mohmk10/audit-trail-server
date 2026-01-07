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

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.username:}")
    private String username;

    @Value("${elasticsearch.password:}")
    private String password;

    @Value("${elasticsearch.use-ssl:false}")
    private boolean useSsl;

    @Override
    public ClientConfiguration clientConfiguration() {
        String hostAndPort = host + ":" + port;

        boolean hasCredentials = username != null && !username.isEmpty()
                && password != null && !password.isEmpty();

        if (useSsl && hasCredentials) {
            return ClientConfiguration.builder()
                    .connectedTo(hostAndPort)
                    .usingSsl()
                    .withBasicAuth(username, password)
                    .withConnectTimeout(Duration.ofSeconds(10))
                    .withSocketTimeout(Duration.ofSeconds(30))
                    .build();
        } else if (useSsl) {
            return ClientConfiguration.builder()
                    .connectedTo(hostAndPort)
                    .usingSsl()
                    .withConnectTimeout(Duration.ofSeconds(10))
                    .withSocketTimeout(Duration.ofSeconds(30))
                    .build();
        } else if (hasCredentials) {
            return ClientConfiguration.builder()
                    .connectedTo(hostAndPort)
                    .withBasicAuth(username, password)
                    .withConnectTimeout(Duration.ofSeconds(10))
                    .withSocketTimeout(Duration.ofSeconds(30))
                    .build();
        } else {
            return ClientConfiguration.builder()
                    .connectedTo(hostAndPort)
                    .withConnectTimeout(Duration.ofSeconds(10))
                    .withSocketTimeout(Duration.ofSeconds(30))
                    .build();
        }
    }
}
