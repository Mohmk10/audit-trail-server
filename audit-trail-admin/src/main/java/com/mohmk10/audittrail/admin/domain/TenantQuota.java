package com.mohmk10.audittrail.admin.domain;

public class TenantQuota {
    private long maxEventsPerDay;
    private long maxEventsPerMonth;
    private int maxSources;
    private int maxApiKeys;
    private int maxUsers;
    private int retentionDays;

    public TenantQuota() {
    }

    public TenantQuota(long maxEventsPerDay, long maxEventsPerMonth, int maxSources,
                       int maxApiKeys, int maxUsers, int retentionDays) {
        this.maxEventsPerDay = maxEventsPerDay;
        this.maxEventsPerMonth = maxEventsPerMonth;
        this.maxSources = maxSources;
        this.maxApiKeys = maxApiKeys;
        this.maxUsers = maxUsers;
        this.retentionDays = retentionDays;
    }

    public long getMaxEventsPerDay() {
        return maxEventsPerDay;
    }

    public void setMaxEventsPerDay(long maxEventsPerDay) {
        this.maxEventsPerDay = maxEventsPerDay;
    }

    public long getMaxEventsPerMonth() {
        return maxEventsPerMonth;
    }

    public void setMaxEventsPerMonth(long maxEventsPerMonth) {
        this.maxEventsPerMonth = maxEventsPerMonth;
    }

    public int getMaxSources() {
        return maxSources;
    }

    public void setMaxSources(int maxSources) {
        this.maxSources = maxSources;
    }

    public int getMaxApiKeys() {
        return maxApiKeys;
    }

    public void setMaxApiKeys(int maxApiKeys) {
        this.maxApiKeys = maxApiKeys;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }
}
