package com.mohmk10.audittrail.admin.aspect;

import com.mohmk10.audittrail.admin.domain.AdminAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    AdminAction action();

    String resourceType();

    /**
     * SpEL expression to extract the resource ID from method arguments.
     * Example: "#id" or "#request.id"
     */
    String resourceId() default "";
}
