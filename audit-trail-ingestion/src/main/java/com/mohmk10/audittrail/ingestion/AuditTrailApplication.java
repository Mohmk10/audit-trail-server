package com.mohmk10.audittrail.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.mohmk10.audittrail.core",
        "com.mohmk10.audittrail.storage",
        "com.mohmk10.audittrail.search",
        "com.mohmk10.audittrail.ingestion"
})
@EntityScan(basePackages = "com.mohmk10.audittrail.storage.adapter.out.persistence.entity")
@EnableJpaRepositories(basePackages = "com.mohmk10.audittrail.storage.adapter.out.persistence.repository")
public class AuditTrailApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditTrailApplication.class, args);
    }
}
