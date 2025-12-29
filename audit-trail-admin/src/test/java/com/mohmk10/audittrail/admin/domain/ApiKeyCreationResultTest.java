package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyCreationResultTest {

    @Test
    void shouldCreateResultWithApiKeyAndPlainTextKey() {
        ApiKey apiKey = AdminTestFixtures.createApiKey();
        String plainTextKey = "atk_test_1234567890abcdef";

        ApiKeyCreationResult result = new ApiKeyCreationResult(apiKey, plainTextKey);

        assertThat(result.apiKey()).isEqualTo(apiKey);
        assertThat(result.plainTextKey()).isEqualTo(plainTextKey);
    }

    @Test
    void shouldReturnApiKey() {
        ApiKey apiKey = AdminTestFixtures.createApiKey();
        ApiKeyCreationResult result = new ApiKeyCreationResult(apiKey, "key");

        assertThat(result.apiKey()).isNotNull();
        assertThat(result.apiKey().getId()).isEqualTo(apiKey.getId());
    }

    @Test
    void shouldReturnPlainTextKey() {
        ApiKey apiKey = AdminTestFixtures.createApiKey();
        String plainTextKey = "atk_test_plain_text_key";

        ApiKeyCreationResult result = new ApiKeyCreationResult(apiKey, plainTextKey);

        assertThat(result.plainTextKey()).isEqualTo(plainTextKey);
    }

    @Test
    void shouldSupportNullValues() {
        ApiKeyCreationResult result = new ApiKeyCreationResult(null, null);

        assertThat(result.apiKey()).isNull();
        assertThat(result.plainTextKey()).isNull();
    }

    @Test
    void shouldCreateFromFixtures() {
        ApiKeyCreationResult result = AdminTestFixtures.createApiKeyCreationResult();

        assertThat(result).isNotNull();
        assertThat(result.apiKey()).isNotNull();
        assertThat(result.plainTextKey()).isNotBlank();
    }

    @Test
    void shouldHaveCorrectEquality() {
        ApiKey apiKey = AdminTestFixtures.createApiKey();
        String plainTextKey = "key123";

        ApiKeyCreationResult result1 = new ApiKeyCreationResult(apiKey, plainTextKey);
        ApiKeyCreationResult result2 = new ApiKeyCreationResult(apiKey, plainTextKey);

        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void shouldHaveCorrectHashCode() {
        ApiKey apiKey = AdminTestFixtures.createApiKey();
        String plainTextKey = "key123";

        ApiKeyCreationResult result1 = new ApiKeyCreationResult(apiKey, plainTextKey);
        ApiKeyCreationResult result2 = new ApiKeyCreationResult(apiKey, plainTextKey);

        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    void shouldHaveToString() {
        ApiKey apiKey = AdminTestFixtures.createApiKey();
        ApiKeyCreationResult result = new ApiKeyCreationResult(apiKey, "key");

        assertThat(result.toString()).contains("ApiKeyCreationResult");
    }
}
