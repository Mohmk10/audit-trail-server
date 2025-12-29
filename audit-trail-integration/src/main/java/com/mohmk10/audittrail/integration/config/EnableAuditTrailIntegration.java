package com.mohmk10.audittrail.integration.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
    WebhookAutoConfiguration.class,
    ExporterAutoConfiguration.class
})
public @interface EnableAuditTrailIntegration {
}
