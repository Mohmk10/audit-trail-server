package com.mohmk10.audittrail.storage;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.mohmk10.audittrail.storage.adapter.out.persistence.entity")
@EnableJpaRepositories(basePackages = "com.mohmk10.audittrail.storage.adapter.out.persistence.repository")
public class TestApplication {
}
