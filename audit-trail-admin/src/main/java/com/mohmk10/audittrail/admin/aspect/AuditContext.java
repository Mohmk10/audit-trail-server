package com.mohmk10.audittrail.admin.aspect;

import java.util.UUID;

/**
 * Thread-local context for audit information.
 * Should be set by authentication filters/interceptors.
 */
public final class AuditContext {

    private static final ThreadLocal<AuditInfo> CONTEXT = new ThreadLocal<>();

    private AuditContext() {
    }

    public static void set(String tenantId, UUID actorId, String actorEmail, String ipAddress, String userAgent) {
        CONTEXT.set(new AuditInfo(tenantId, actorId, actorEmail, ipAddress, userAgent));
    }

    public static AuditInfo get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public record AuditInfo(
            String tenantId,
            UUID actorId,
            String actorEmail,
            String ipAddress,
            String userAgent
    ) {
    }
}
