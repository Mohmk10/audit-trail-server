package com.mohmk10.audittrail.sdk.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceTest {

    @Test
    void shouldCreateResourceWithBuilder() {
        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .name("report.pdf")
                .build();

        assertThat(resource.getId()).isEqualTo("res-123");
        assertThat(resource.getType()).isEqualTo("DOCUMENT");
        assertThat(resource.getName()).isEqualTo("report.pdf");
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> Resource.builder()
                .type("DOCUMENT")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Resource id is required");
    }

    @Test
    void shouldThrowWhenTypeIsNull() {
        assertThatThrownBy(() -> Resource.builder()
                .id("res-123")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Resource type is required");
    }

    @Test
    void shouldCreateDocumentResourceWithFactoryMethod() {
        Resource resource = Resource.document("doc-456", "contract.pdf");

        assertThat(resource.getId()).isEqualTo("doc-456");
        assertThat(resource.getType()).isEqualTo("DOCUMENT");
        assertThat(resource.getName()).isEqualTo("contract.pdf");
    }

    @Test
    void shouldCreateUserResourceWithFactoryMethod() {
        Resource resource = Resource.user("user-789", "john.doe@example.com");

        assertThat(resource.getId()).isEqualTo("user-789");
        assertThat(resource.getType()).isEqualTo("USER");
        assertThat(resource.getName()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldCreateFileResourceWithFactoryMethod() {
        Resource resource = Resource.file("file-001", "data.csv");

        assertThat(resource.getId()).isEqualTo("file-001");
        assertThat(resource.getType()).isEqualTo("FILE");
        assertThat(resource.getName()).isEqualTo("data.csv");
    }

    @Test
    void shouldCreateConfigResourceWithFactoryMethod() {
        Resource resource = Resource.config("cfg-002", "app.properties");

        assertThat(resource.getId()).isEqualTo("cfg-002");
        assertThat(resource.getType()).isEqualTo("CONFIG");
        assertThat(resource.getName()).isEqualTo("app.properties");
    }

    @Test
    void shouldCreateCustomResourceWithOfMethod() {
        Resource resource = Resource.of("custom-123", "CUSTOM_TYPE", "Custom Resource");

        assertThat(resource.getId()).isEqualTo("custom-123");
        assertThat(resource.getType()).isEqualTo("CUSTOM_TYPE");
        assertThat(resource.getName()).isEqualTo("Custom Resource");
    }

    @Test
    void shouldCreateResourceWithBeforeStateUsingKeyValue() {
        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .before("status", "draft")
                .before("version", 1)
                .build();

        assertThat(resource.getBefore()).containsEntry("status", "draft");
        assertThat(resource.getBefore()).containsEntry("version", 1);
    }

    @Test
    void shouldCreateResourceWithBeforeStateUsingMap() {
        Map<String, Object> before = Map.of("status", "draft", "version", 1);

        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .before(before)
                .build();

        assertThat(resource.getBefore()).containsEntry("status", "draft");
        assertThat(resource.getBefore()).containsEntry("version", 1);
    }

    @Test
    void shouldCreateResourceWithAfterStateUsingKeyValue() {
        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .after("status", "published")
                .after("version", 2)
                .build();

        assertThat(resource.getAfter()).containsEntry("status", "published");
        assertThat(resource.getAfter()).containsEntry("version", 2);
    }

    @Test
    void shouldCreateResourceWithAfterStateUsingMap() {
        Map<String, Object> after = Map.of("status", "published", "version", 2);

        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .after(after)
                .build();

        assertThat(resource.getAfter()).containsEntry("status", "published");
        assertThat(resource.getAfter()).containsEntry("version", 2);
    }

    @Test
    void shouldCreateResourceWithBeforeAndAfterStates() {
        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .before("status", "draft")
                .after("status", "published")
                .build();

        assertThat(resource.getBefore()).containsEntry("status", "draft");
        assertThat(resource.getAfter()).containsEntry("status", "published");
    }

    @Test
    void shouldReturnNullForEmptyBefore() {
        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .build();

        assertThat(resource.getBefore()).isNull();
    }

    @Test
    void shouldReturnNullForEmptyAfter() {
        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .build();

        assertThat(resource.getAfter()).isNull();
    }

    @Test
    void shouldHaveNullNameWhenNotSet() {
        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .build();

        assertThat(resource.getName()).isNull();
    }

    @Test
    void shouldSupportEqualsBasedOnIdAndType() {
        Resource resource1 = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .name("file1.pdf")
                .build();

        Resource resource2 = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .name("file2.pdf")
                .build();

        assertThat(resource1).isEqualTo(resource2);
        assertThat(resource1.hashCode()).isEqualTo(resource2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentIds() {
        Resource resource1 = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .build();

        Resource resource2 = Resource.builder()
                .id("res-456")
                .type("DOCUMENT")
                .build();

        assertThat(resource1).isNotEqualTo(resource2);
    }

    @Test
    void shouldNotBeEqualWithDifferentTypes() {
        Resource resource1 = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .build();

        Resource resource2 = Resource.builder()
                .id("res-123")
                .type("FILE")
                .build();

        assertThat(resource1).isNotEqualTo(resource2);
    }

    @Test
    void shouldCreateResourceWithAllFields() {
        Map<String, Object> before = Map.of("status", "draft");
        Map<String, Object> after = Map.of("status", "published");

        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .name("report.pdf")
                .before(before)
                .after(after)
                .build();

        assertThat(resource.getId()).isEqualTo("res-123");
        assertThat(resource.getType()).isEqualTo("DOCUMENT");
        assertThat(resource.getName()).isEqualTo("report.pdf");
        assertThat(resource.getBefore()).containsEntry("status", "draft");
        assertThat(resource.getAfter()).containsEntry("status", "published");
    }

    @Test
    void shouldTrackComplexStateChanges() {
        Resource resource = Resource.builder()
                .id("doc-001")
                .type("DOCUMENT")
                .name("my-document.docx")
                .before("title", "Old Title")
                .before("content", "Old content")
                .after("title", "New Title")
                .after("content", "Updated content")
                .build();

        assertThat(resource.getBefore().get("title")).isEqualTo("Old Title");
        assertThat(resource.getAfter().get("title")).isEqualTo("New Title");
    }

    @Test
    void shouldMergeBeforeStates() {
        Map<String, Object> before1 = Map.of("key1", "value1");

        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .before(before1)
                .before("key2", "value2")
                .build();

        assertThat(resource.getBefore()).hasSize(2);
    }

    @Test
    void shouldMergeAfterStates() {
        Map<String, Object> after1 = Map.of("key1", "value1");

        Resource resource = Resource.builder()
                .id("res-123")
                .type("DOCUMENT")
                .after(after1)
                .after("key2", "value2")
                .build();

        assertThat(resource.getAfter()).hasSize(2);
    }
}
