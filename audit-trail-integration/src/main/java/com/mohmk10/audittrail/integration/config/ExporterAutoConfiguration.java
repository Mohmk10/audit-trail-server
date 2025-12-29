package com.mohmk10.audittrail.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohmk10.audittrail.integration.exporter.CompositeEventExporter;
import com.mohmk10.audittrail.integration.exporter.EventExporter;
import com.mohmk10.audittrail.integration.exporter.elk.ElkExporter;
import com.mohmk10.audittrail.integration.exporter.elk.ElkProperties;
import com.mohmk10.audittrail.integration.exporter.s3.S3Exporter;
import com.mohmk10.audittrail.integration.exporter.s3.S3Properties;
import com.mohmk10.audittrail.integration.exporter.splunk.SplunkExporter;
import com.mohmk10.audittrail.integration.exporter.splunk.SplunkProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@ConditionalOnProperty(prefix = "audit-trail.exporter", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({SplunkProperties.class, ElkProperties.class, S3Properties.class})
public class ExporterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "audit-trail.exporter.splunk", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(SplunkExporter.class)
    public SplunkExporter splunkExporter(SplunkProperties properties, ObjectMapper objectMapper) {
        return new SplunkExporter(properties, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "audit-trail.exporter.elk", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(ElkExporter.class)
    public ElkExporter elkExporter(ElkProperties properties, ObjectMapper objectMapper) {
        return new ElkExporter(properties, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "audit-trail.exporter.s3", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(S3AsyncClient.class)
    public S3AsyncClient s3AsyncClient(S3Properties properties) {
        return S3AsyncClient.builder()
            .region(Region.of(properties.getRegion()))
            .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "audit-trail.exporter.s3", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(S3Exporter.class)
    public S3Exporter s3Exporter(
            S3AsyncClient s3Client,
            S3Properties properties,
            ObjectMapper objectMapper) {
        return new S3Exporter(s3Client, properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeEventExporter compositeEventExporter(List<EventExporter> exporters) {
        // Filter out composite to avoid circular reference
        List<EventExporter> filtered = new ArrayList<>();
        for (EventExporter exporter : exporters) {
            if (!(exporter instanceof CompositeEventExporter)) {
                filtered.add(exporter);
            }
        }
        return new CompositeEventExporter(filtered);
    }
}
