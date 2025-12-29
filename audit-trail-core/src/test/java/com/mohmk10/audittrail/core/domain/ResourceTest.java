package com.mohmk10.audittrail.core.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceTest {

    @Test
    void shouldCreateResourceWithAllFields() {
        Map<String, Object> before = Map.of("status", "draft");
        Map<String, Object> after = Map.of("status", "published");

        Resource resource = new Resource(
                "doc-123",
                Resource.ResourceType.DOCUMENT,
                "Annual Report 2024",
                before,
                after
        );

        assertThat(resource.id()).isEqualTo("doc-123");
        assertThat(resource.type()).isEqualTo(Resource.ResourceType.DOCUMENT);
        assertThat(resource.name()).isEqualTo("Annual Report 2024");
        assertThat(resource.before()).isEqualTo(before);
        assertThat(resource.after()).isEqualTo(after);
    }

    @Test
    void shouldHandleBeforeState() {
        Map<String, Object> before = Map.of(
                "email", "old@example.com",
                "name", "Old Name",
                "active", true
        );

        Resource resource = new Resource(
                "user-456",
                Resource.ResourceType.USER,
                "User Profile",
                before,
                null
        );

        assertThat(resource.before()).containsEntry("email", "old@example.com");
        assertThat(resource.before()).containsEntry("name", "Old Name");
        assertThat(resource.before()).containsEntry("active", true);
        assertThat(resource.after()).isNull();
    }

    @Test
    void shouldHandleAfterState() {
        Map<String, Object> after = Map.of(
                "email", "new@example.com",
                "name", "New Name",
                "active", false
        );

        Resource resource = new Resource(
                "user-456",
                Resource.ResourceType.USER,
                "User Profile",
                null,
                after
        );

        assertThat(resource.before()).isNull();
        assertThat(resource.after()).containsEntry("email", "new@example.com");
        assertThat(resource.after()).containsEntry("name", "New Name");
        assertThat(resource.after()).containsEntry("active", false);
    }

    @Test
    void shouldHandleBothStates() {
        Map<String, Object> before = Map.of("balance", 1000);
        Map<String, Object> after = Map.of("balance", 1500);

        Resource resource = new Resource(
                "txn-789",
                Resource.ResourceType.TRANSACTION,
                "Account Transfer",
                before,
                after
        );

        assertThat(resource.before()).containsEntry("balance", 1000);
        assertThat(resource.after()).containsEntry("balance", 1500);
    }

    @Test
    void shouldHandleNullStates() {
        Resource resource = new Resource(
                "config-001",
                Resource.ResourceType.CONFIG,
                "App Configuration",
                null,
                null
        );

        assertThat(resource.before()).isNull();
        assertThat(resource.after()).isNull();
    }

    @Test
    void shouldSupportAllResourceTypes() {
        assertThat(Resource.ResourceType.DOCUMENT).isNotNull();
        assertThat(Resource.ResourceType.USER).isNotNull();
        assertThat(Resource.ResourceType.TRANSACTION).isNotNull();
        assertThat(Resource.ResourceType.CONFIG).isNotNull();
        assertThat(Resource.ResourceType.FILE).isNotNull();
        assertThat(Resource.ResourceType.API).isNotNull();
        assertThat(Resource.ResourceType.DATABASE).isNotNull();
        assertThat(Resource.ResourceType.SYSTEM).isNotNull();
    }

    @Test
    void shouldHaveCorrectResourceTypeValues() {
        assertThat(Resource.ResourceType.values()).hasSize(8);
        assertThat(Resource.ResourceType.valueOf("DOCUMENT")).isEqualTo(Resource.ResourceType.DOCUMENT);
        assertThat(Resource.ResourceType.valueOf("DATABASE")).isEqualTo(Resource.ResourceType.DATABASE);
    }

    @Test
    void shouldSupportRecordEquality() {
        Resource resource1 = new Resource("id", Resource.ResourceType.FILE, "name", null, null);
        Resource resource2 = new Resource("id", Resource.ResourceType.FILE, "name", null, null);

        assertThat(resource1).isEqualTo(resource2);
        assertThat(resource1.hashCode()).isEqualTo(resource2.hashCode());
    }

    @Test
    void shouldDifferentiateNonEqualResources() {
        Resource resource1 = new Resource("id1", Resource.ResourceType.FILE, "name", null, null);
        Resource resource2 = new Resource("id2", Resource.ResourceType.FILE, "name", null, null);

        assertThat(resource1).isNotEqualTo(resource2);
    }

    @Test
    void shouldHandleComplexNestedStates() {
        Map<String, Object> before = Map.of(
                "metadata", Map.of("author", "John", "version", 1),
                "tags", "draft,internal"
        );
        Map<String, Object> after = Map.of(
                "metadata", Map.of("author", "John", "version", 2),
                "tags", "published,external"
        );

        Resource resource = new Resource(
                "doc-complex",
                Resource.ResourceType.DOCUMENT,
                "Complex Document",
                before,
                after
        );

        assertThat(resource.before()).containsKey("metadata");
        assertThat(resource.after()).containsKey("metadata");
    }
}
