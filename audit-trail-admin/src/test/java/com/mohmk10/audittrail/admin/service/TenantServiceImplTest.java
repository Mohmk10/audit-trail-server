package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.TenantEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.mapper.TenantMapper;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.JpaTenantRepository;
import com.mohmk10.audittrail.admin.domain.Tenant;
import com.mohmk10.audittrail.admin.domain.TenantPlan;
import com.mohmk10.audittrail.admin.domain.TenantQuota;
import com.mohmk10.audittrail.admin.domain.TenantStatus;
import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceImplTest {

    @Mock
    private JpaTenantRepository repository;

    @Mock
    private TenantMapper mapper;

    private TenantServiceImpl tenantService;

    @BeforeEach
    void setUp() {
        tenantService = new TenantServiceImpl(repository, mapper);
    }

    @Test
    void shouldCreateTenantWithDefaults() {
        Tenant tenant = Tenant.builder()
                .name("New Tenant")
                .build();

        TenantEntity entity = new TenantEntity();
        when(mapper.toEntity(any(Tenant.class))).thenReturn(entity);
        when(repository.save(any(TenantEntity.class))).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(AdminTestFixtures.createTenant());

        Tenant result = tenantService.create(tenant);

        assertThat(result).isNotNull();

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(mapper).toEntity(captor.capture());

        Tenant captured = captor.getValue();
        assertThat(captured.getId()).isNotNull();
        assertThat(captured.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(captured.getPlan()).isEqualTo(TenantPlan.FREE);
        assertThat(captured.getQuota()).isNotNull();
        assertThat(captured.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCreateTenantWithProvidedValues() {
        UUID id = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
                .id(id)
                .name("My Tenant")
                .slug("my-tenant")
                .status(TenantStatus.PENDING)
                .plan(TenantPlan.PRO)
                .quota(AdminTestFixtures.createQuotaForPlan(TenantPlan.PRO))
                .build();

        TenantEntity entity = new TenantEntity();
        when(mapper.toEntity(any(Tenant.class))).thenReturn(entity);
        when(repository.save(any(TenantEntity.class))).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(tenant);

        tenantService.create(tenant);

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(mapper).toEntity(captor.capture());

        Tenant captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(id);
        assertThat(captured.getStatus()).isEqualTo(TenantStatus.PENDING);
        assertThat(captured.getPlan()).isEqualTo(TenantPlan.PRO);
    }

    @Test
    void shouldGenerateSlugFromName() {
        Tenant tenant = Tenant.builder()
                .name("My New Tenant")
                .build();

        TenantEntity entity = new TenantEntity();
        when(mapper.toEntity(any(Tenant.class))).thenReturn(entity);
        when(repository.save(any(TenantEntity.class))).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(AdminTestFixtures.createTenant());

        tenantService.create(tenant);

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(mapper).toEntity(captor.capture());

        assertThat(captor.getValue().getSlug()).isEqualTo("my-new-tenant");
    }

    @Test
    void shouldUpdateTenantName() {
        UUID id = UUID.randomUUID();
        TenantEntity existingEntity = mock(TenantEntity.class);
        Tenant updated = AdminTestFixtures.createTenant();

        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any(TenantEntity.class))).thenReturn(existingEntity);
        when(mapper.toDomain(existingEntity)).thenReturn(updated);

        Tenant updateData = Tenant.builder().name("Updated Name").build();
        Tenant result = tenantService.update(id, updateData);

        assertThat(result).isNotNull();
        verify(existingEntity).setName("Updated Name");
        verify(existingEntity).setUpdatedAt(any());
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentTenant() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Tenant updateData = Tenant.builder().name("Updated").build();

        assertThatThrownBy(() -> tenantService.update(id, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    void shouldFindTenantById() {
        UUID id = UUID.randomUUID();
        TenantEntity entity = new TenantEntity();
        Tenant tenant = AdminTestFixtures.createTenant();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(tenant);

        Optional<Tenant> result = tenantService.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(tenant);
    }

    @Test
    void shouldReturnEmptyWhenTenantNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Tenant> result = tenantService.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindTenantBySlug() {
        String slug = "test-tenant";
        TenantEntity entity = new TenantEntity();
        Tenant tenant = AdminTestFixtures.createTenant();

        when(repository.findBySlug(slug)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(tenant);

        Optional<Tenant> result = tenantService.findBySlug(slug);

        assertThat(result).isPresent();
    }

    @Test
    void shouldFindAllTenants() {
        TenantEntity entity1 = new TenantEntity();
        TenantEntity entity2 = new TenantEntity();
        Tenant tenant1 = AdminTestFixtures.createTenantWithPlan(TenantPlan.FREE);
        Tenant tenant2 = AdminTestFixtures.createTenantWithPlan(TenantPlan.PRO);

        when(repository.findAll()).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(tenant1);
        when(mapper.toDomain(entity2)).thenReturn(tenant2);

        List<Tenant> result = tenantService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldSuspendTenant() {
        UUID id = UUID.randomUUID();
        TenantEntity entity = mock(TenantEntity.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        tenantService.suspend(id);

        verify(entity).setStatus(TenantStatus.SUSPENDED);
        verify(entity).setUpdatedAt(any());
        verify(repository).save(entity);
    }

    @Test
    void shouldThrowWhenSuspendingNonExistentTenant() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.suspend(id))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldActivateTenant() {
        UUID id = UUID.randomUUID();
        TenantEntity entity = mock(TenantEntity.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        tenantService.activate(id);

        verify(entity).setStatus(TenantStatus.ACTIVE);
        verify(repository).save(entity);
    }

    @Test
    void shouldDeleteTenant() {
        UUID id = UUID.randomUUID();
        TenantEntity entity = mock(TenantEntity.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        tenantService.delete(id);

        verify(entity).setStatus(TenantStatus.DELETED);
        verify(repository).save(entity);
    }

    @Test
    void shouldReturnDefaultQuotaForFreePlan() {
        TenantQuota quota = tenantService.getDefaultQuota(TenantPlan.FREE);

        assertThat(quota.getMaxEventsPerDay()).isEqualTo(1000);
        assertThat(quota.getMaxEventsPerMonth()).isEqualTo(30000);
        assertThat(quota.getMaxSources()).isEqualTo(2);
        assertThat(quota.getMaxApiKeys()).isEqualTo(2);
        assertThat(quota.getMaxUsers()).isEqualTo(1);
        assertThat(quota.getRetentionDays()).isEqualTo(7);
    }

    @Test
    void shouldReturnDefaultQuotaForStarterPlan() {
        TenantQuota quota = tenantService.getDefaultQuota(TenantPlan.STARTER);

        assertThat(quota.getMaxEventsPerDay()).isEqualTo(10000);
        assertThat(quota.getRetentionDays()).isEqualTo(30);
    }

    @Test
    void shouldReturnDefaultQuotaForProPlan() {
        TenantQuota quota = tenantService.getDefaultQuota(TenantPlan.PRO);

        assertThat(quota.getMaxEventsPerDay()).isEqualTo(100000);
        assertThat(quota.getRetentionDays()).isEqualTo(90);
    }

    @Test
    void shouldReturnDefaultQuotaForEnterprisePlan() {
        TenantQuota quota = tenantService.getDefaultQuota(TenantPlan.ENTERPRISE);

        assertThat(quota.getMaxEventsPerDay()).isEqualTo(Long.MAX_VALUE);
        assertThat(quota.getRetentionDays()).isEqualTo(365);
    }
}
