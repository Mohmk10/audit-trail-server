package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SourceTest {

    @Test
    void shouldBuildSourceWithAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Map<String, String> config = Map.of("env", "prod");

        Source source = Source.builder()
                .id(id)
                .tenantId("tenant-001")
                .name("Web Application")
                .description("Main web app source")
                .type(SourceType.WEB_APP)
                .status(SourceStatus.ACTIVE)
                .config(config)
                .createdAt(now)
                .lastEventAt(now)
                .eventCount(1000L)
                .build();

        assertThat(source.getId()).isEqualTo(id);
        assertThat(source.getTenantId()).isEqualTo("tenant-001");
        assertThat(source.getName()).isEqualTo("Web Application");
        assertThat(source.getDescription()).isEqualTo("Main web app source");
        assertThat(source.getType()).isEqualTo(SourceType.WEB_APP);
        assertThat(source.getStatus()).isEqualTo(SourceStatus.ACTIVE);
        assertThat(source.getConfig()).isEqualTo(config);
        assertThat(source.getCreatedAt()).isEqualTo(now);
        assertThat(source.getLastEventAt()).isEqualTo(now);
        assertThat(source.getEventCount()).isEqualTo(1000L);
    }

    @Test
    void shouldCreateEmptySource() {
        Source source = new Source();

        assertThat(source.getId()).isNull();
        assertThat(source.getName()).isNull();
        assertThat(source.getType()).isNull();
    }

    @Test
    void shouldSetAndGetId() {
        Source source = new Source();
        UUID id = UUID.randomUUID();

        source.setId(id);

        assertThat(source.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetTenantId() {
        Source source = new Source();

        source.setTenantId("tenant-002");

        assertThat(source.getTenantId()).isEqualTo("tenant-002");
    }

    @Test
    void shouldSetAndGetName() {
        Source source = new Source();

        source.setName("Mobile App");

        assertThat(source.getName()).isEqualTo("Mobile App");
    }

    @Test
    void shouldSetAndGetDescription() {
        Source source = new Source();

        source.setDescription("iOS mobile application");

        assertThat(source.getDescription()).isEqualTo("iOS mobile application");
    }

    @Test
    void shouldSetAndGetType() {
        Source source = new Source();

        source.setType(SourceType.MOBILE_APP);

        assertThat(source.getType()).isEqualTo(SourceType.MOBILE_APP);
    }

    @Test
    void shouldSetAndGetStatus() {
        Source source = new Source();

        source.setStatus(SourceStatus.INACTIVE);

        assertThat(source.getStatus()).isEqualTo(SourceStatus.INACTIVE);
    }

    @Test
    void shouldSetAndGetConfig() {
        Source source = new Source();
        Map<String, String> config = Map.of("key", "value");

        source.setConfig(config);

        assertThat(source.getConfig()).isEqualTo(config);
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        Source source = new Source();
        Instant now = Instant.now();

        source.setCreatedAt(now);

        assertThat(source.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetLastEventAt() {
        Source source = new Source();
        Instant now = Instant.now();

        source.setLastEventAt(now);

        assertThat(source.getLastEventAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetEventCount() {
        Source source = new Source();

        source.setEventCount(5000L);

        assertThat(source.getEventCount()).isEqualTo(5000L);
    }

    @Test
    void shouldCreateSourceFromFixtures() {
        Source source = AdminTestFixtures.createSource();

        assertThat(source).isNotNull();
        assertThat(source.getId()).isNotNull();
        assertThat(source.getTenantId()).isNotNull();
        assertThat(source.getName()).isNotNull();
        assertThat(source.getType()).isNotNull();
        assertThat(source.getStatus()).isEqualTo(SourceStatus.ACTIVE);
    }

    @Test
    void shouldCreateSourceWithDifferentTypes() {
        for (SourceType type : SourceType.values()) {
            Source source = AdminTestFixtures.createSourceWithType(type);

            assertThat(source.getType()).isEqualTo(type);
        }
    }

    @Test
    void shouldCreateSourceWithDifferentStatuses() {
        for (SourceStatus status : SourceStatus.values()) {
            Source source = AdminTestFixtures.createSourceWithStatus(status);

            assertThat(source.getStatus()).isEqualTo(status);
        }
    }
}
