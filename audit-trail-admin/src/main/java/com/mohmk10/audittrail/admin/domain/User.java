package com.mohmk10.audittrail.admin.domain;

import java.time.Instant;
import java.util.UUID;

public class User {
    private UUID id;
    private String tenantId;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private Role role;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private String lastLoginIp;
    private String oauthProvider;
    private String oauthId;

    public User() {
    }

    private User(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.role = builder.role;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.lastLoginAt = builder.lastLoginAt;
        this.lastLoginIp = builder.lastLoginIp;
        this.oauthProvider = builder.oauthProvider;
        this.oauthId = builder.oauthId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email;
        }
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public static class Builder {
        private UUID id;
        private String tenantId;
        private String email;
        private String passwordHash;
        private String firstName;
        private String lastName;
        private Role role;
        private UserStatus status;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant lastLoginAt;
        private String lastLoginIp;
        private String oauthProvider;
        private String oauthId;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder lastLoginAt(Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public Builder lastLoginIp(String lastLoginIp) {
            this.lastLoginIp = lastLoginIp;
            return this;
        }

        public Builder oauthProvider(String oauthProvider) {
            this.oauthProvider = oauthProvider;
            return this;
        }

        public Builder oauthId(String oauthId) {
            this.oauthId = oauthId;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
