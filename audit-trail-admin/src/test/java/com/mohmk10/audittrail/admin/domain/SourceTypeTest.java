package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class SourceTypeTest {

    @Test
    void shouldHaveFiveSourceTypes() {
        assertThat(SourceType.values()).hasSize(5);
    }

    @Test
    void shouldContainWebAppType() {
        assertThat(SourceType.WEB_APP).isNotNull();
        assertThat(SourceType.WEB_APP.name()).isEqualTo("WEB_APP");
    }

    @Test
    void shouldContainMobileAppType() {
        assertThat(SourceType.MOBILE_APP).isNotNull();
        assertThat(SourceType.MOBILE_APP.name()).isEqualTo("MOBILE_APP");
    }

    @Test
    void shouldContainBackendServiceType() {
        assertThat(SourceType.BACKEND_SERVICE).isNotNull();
        assertThat(SourceType.BACKEND_SERVICE.name()).isEqualTo("BACKEND_SERVICE");
    }

    @Test
    void shouldContainDatabaseCdcType() {
        assertThat(SourceType.DATABASE_CDC).isNotNull();
        assertThat(SourceType.DATABASE_CDC.name()).isEqualTo("DATABASE_CDC");
    }

    @Test
    void shouldContainExternalApiType() {
        assertThat(SourceType.EXTERNAL_API).isNotNull();
        assertThat(SourceType.EXTERNAL_API.name()).isEqualTo("EXTERNAL_API");
    }

    @ParameterizedTest
    @EnumSource(SourceType.class)
    void shouldHaveValidName(SourceType type) {
        assertThat(type.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(SourceType.valueOf("WEB_APP")).isEqualTo(SourceType.WEB_APP);
        assertThat(SourceType.valueOf("MOBILE_APP")).isEqualTo(SourceType.MOBILE_APP);
        assertThat(SourceType.valueOf("BACKEND_SERVICE")).isEqualTo(SourceType.BACKEND_SERVICE);
        assertThat(SourceType.valueOf("DATABASE_CDC")).isEqualTo(SourceType.DATABASE_CDC);
        assertThat(SourceType.valueOf("EXTERNAL_API")).isEqualTo(SourceType.EXTERNAL_API);
    }
}
