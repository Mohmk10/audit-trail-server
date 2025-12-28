package com.mohmk10.audittrail.admin.aspect;

import com.mohmk10.audittrail.admin.application.AdminAuditService;
import com.mohmk10.audittrail.admin.domain.AdminAuditLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class AdminAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AdminAuditAspect.class);

    private final AdminAuditService adminAuditService;
    private final SpelExpressionParser expressionParser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AdminAuditAspect(AdminAuditService adminAuditService) {
        this.adminAuditService = adminAuditService;
    }

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        AuditContext.AuditInfo auditInfo = AuditContext.get();

        // If no audit context, just proceed without auditing
        if (auditInfo == null) {
            log.debug("No audit context available for {}", joinPoint.getSignature().getName());
            return joinPoint.proceed();
        }

        String resourceId = extractResourceId(joinPoint, audited.resourceId());
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                Map<String, Object> newState = new HashMap<>();
                if (result != null) {
                    newState.put("result", result.toString());
                }
                if (exception != null) {
                    newState.put("error", exception.getMessage());
                }

                AdminAuditLog auditLog = AdminAuditLog.builder()
                        .id(UUID.randomUUID())
                        .tenantId(auditInfo.tenantId())
                        .actorId(auditInfo.actorId())
                        .actorEmail(auditInfo.actorEmail())
                        .action(audited.action())
                        .resourceType(audited.resourceType())
                        .resourceId(resourceId)
                        .newState(newState.isEmpty() ? null : newState)
                        .ipAddress(auditInfo.ipAddress())
                        .userAgent(auditInfo.userAgent())
                        .timestamp(Instant.now())
                        .build();

                adminAuditService.log(auditLog);
                log.debug("Audited action {} on {} {}", audited.action(), audited.resourceType(), resourceId);
            } catch (Exception e) {
                log.error("Failed to create audit log for {} on {}", audited.action(), audited.resourceType(), e);
            }
        }
    }

    private String extractResourceId(ProceedingJoinPoint joinPoint, String resourceIdExpression) {
        if (resourceIdExpression == null || resourceIdExpression.isBlank()) {
            return null;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
            Object[] args = joinPoint.getArgs();

            EvaluationContext context = new StandardEvaluationContext();
            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    ((StandardEvaluationContext) context).setVariable(parameterNames[i], args[i]);
                }
            }

            Expression expression = expressionParser.parseExpression(resourceIdExpression);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract resource ID from expression '{}': {}", resourceIdExpression, e.getMessage());
            return null;
        }
    }
}
