package com.mohmk10.audittrail.admin.adapter.in.rest;

import com.mohmk10.audittrail.admin.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.admin.domain.Tenant;
import com.mohmk10.audittrail.admin.domain.TenantPlan;
import com.mohmk10.audittrail.admin.domain.TenantStatus;
import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import com.mohmk10.audittrail.admin.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @Mock
    private AdminDtoMapper dtoMapper;

    private TenantController controller;

    @BeforeEach
    void setUp() {
        controller = new TenantController(tenantService, dtoMapper);
    }

    @Test
    void shouldCreateTenantAndReturn201() {
        CreateTenantRequest request = new CreateTenantRequest("Test Tenant", "test-tenant", "A test tenant", TenantPlan.PRO, null, null);
        Tenant tenant = AdminTestFixtures.createTenant();
        TenantResponse response = createTenantResponse(tenant);

        when(dtoMapper.toTenantDomain(request)).thenReturn(tenant);
        when(tenantService.create(tenant)).thenReturn(tenant);
        when(dtoMapper.toTenantResponse(tenant)).thenReturn(response);

        ResponseEntity<TenantResponse> result = controller.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().name()).isEqualTo(tenant.getName());
    }

    @Test
    void shouldFindAllTenants() {
        Tenant tenant1 = AdminTestFixtures.createTenantWithPlan(TenantPlan.FREE);
        Tenant tenant2 = AdminTestFixtures.createTenantWithPlan(TenantPlan.PRO);
        TenantResponse response1 = createTenantResponse(tenant1);
        TenantResponse response2 = createTenantResponse(tenant2);

        when(tenantService.findAll()).thenReturn(List.of(tenant1, tenant2));
        when(dtoMapper.toTenantResponse(tenant1)).thenReturn(response1);
        when(dtoMapper.toTenantResponse(tenant2)).thenReturn(response2);

        ResponseEntity<List<TenantResponse>> result = controller.findAll();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(2);
    }

    @Test
    void shouldFindTenantByIdAndReturn200() {
        UUID id = UUID.randomUUID();
        Tenant tenant = AdminTestFixtures.createTenant();
        TenantResponse response = createTenantResponse(tenant);

        when(tenantService.findById(id)).thenReturn(Optional.of(tenant));
        when(dtoMapper.toTenantResponse(tenant)).thenReturn(response);

        ResponseEntity<TenantResponse> result = controller.findById(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
    }

    @Test
    void shouldReturn404WhenTenantNotFoundById() {
        UUID id = UUID.randomUUID();
        when(tenantService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<TenantResponse> result = controller.findById(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldFindTenantBySlugAndReturn200() {
        String slug = "test-tenant";
        Tenant tenant = AdminTestFixtures.createTenant();
        TenantResponse response = createTenantResponse(tenant);

        when(tenantService.findBySlug(slug)).thenReturn(Optional.of(tenant));
        when(dtoMapper.toTenantResponse(tenant)).thenReturn(response);

        ResponseEntity<TenantResponse> result = controller.findBySlug(slug);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturn404WhenTenantNotFoundBySlug() {
        String slug = "unknown-slug";
        when(tenantService.findBySlug(slug)).thenReturn(Optional.empty());

        ResponseEntity<TenantResponse> result = controller.findBySlug(slug);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateTenantAndReturn200() {
        UUID id = UUID.randomUUID();
        UpdateTenantRequest request = new UpdateTenantRequest("Updated Name", "Updated description", TenantPlan.ENTERPRISE, null, null);
        Tenant tenant = AdminTestFixtures.createTenant();
        TenantResponse response = createTenantResponse(tenant);

        when(dtoMapper.toTenantDomain(request)).thenReturn(tenant);
        when(tenantService.update(id, tenant)).thenReturn(tenant);
        when(dtoMapper.toTenantResponse(tenant)).thenReturn(response);

        ResponseEntity<TenantResponse> result = controller.update(id, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
    }

    @Test
    void shouldSuspendTenantAndReturn200() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.suspend(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(tenantService).suspend(id);
    }

    @Test
    void shouldActivateTenantAndReturn200() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.activate(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(tenantService).activate(id);
    }

    @Test
    void shouldDeleteTenantAndReturn204() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.delete(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(tenantService).delete(id);
    }

    private TenantResponse createTenantResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getDescription(),
                tenant.getStatus(),
                tenant.getPlan(),
                null, // quotaResponse
                tenant.getSettings(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
