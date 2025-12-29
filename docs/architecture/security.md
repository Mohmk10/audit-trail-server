# Security Architecture

Architecture de securite d'Audit Trail.

## Vue d'ensemble

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Security Layers                               │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │
│  │   Network   │  │    API      │  │Application  │  │   Data     │ │
│  │  Security   │  │  Security   │  │  Security   │  │ Security   │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │
│                                                                      │
│  - TLS 1.3        - API Keys      - Input Valid.   - Encryption    │
│  - Firewall       - Rate Limit    - RBAC           - Hashing       │
│  - WAF            - CORS          - Audit Log      - Isolation     │
└─────────────────────────────────────────────────────────────────────┘
```

## Authentication

### API Key Authentication

Chaque requete doit inclure une cle API valide.

#### Format de la cle

```
atk_live_a1b2c3d4e5f6789012345678901234567890
│    │    │
│    │    └── Random bytes (32 bytes, base64)
│    └─────── Environment (live/test)
└──────────── Prefix (audit trail key)
```

#### Flux d'authentification

```
1. Client                    2. API Gateway              3. Auth Service
   ┌───────┐                    ┌───────┐                   ┌───────┐
   │       │──X-API-Key────────►│       │──validate────────►│       │
   │Client │                    │ Filter│                   │  Auth │
   │       │◄─200 OK───────────│       │◄─valid/invalid───│       │
   └───────┘                    └───────┘                   └───────┘
```

#### Implementation

```java
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) {
        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        Optional<ApiKeyDetails> details = apiKeyService.validate(apiKey);

        if (details.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        ApiKeyDetails key = details.get();

        if (!key.isEnabled() || key.isExpired()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        // Set security context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new ApiKeyAuthentication(key));
        SecurityContextHolder.setContext(context);

        chain.doFilter(request, response);
    }
}
```

### Stockage des cles

Les cles sont hashees avant stockage avec HMAC-SHA256.

```java
public class ApiKeyHasher {

    public String hash(String apiKey) {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );
        mac.init(keySpec);
        byte[] hash = mac.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public boolean verify(String apiKey, String storedHash) {
        String computed = hash(apiKey);
        return MessageDigest.isEqual(
            computed.getBytes(),
            storedHash.getBytes()
        );
    }
}
```

---

## Authorization (RBAC)

### Roles

| Role | Description | Permissions |
|------|-------------|-------------|
| ADMIN | Administrateur systeme | Toutes |
| MANAGER | Gestionnaire tenant | CRUD tenants, sources, users |
| OPERATOR | Operateur | Lecture + gestion alertes |
| VIEWER | Lecteur | Lecture seule |

### Permissions

```java
public enum Permission {
    // Events
    EVENT_READ,
    EVENT_WRITE,

    // Search
    SEARCH_READ,

    // Reports
    REPORT_READ,
    REPORT_WRITE,

    // Rules & Alerts
    RULE_READ,
    RULE_WRITE,
    ALERT_READ,
    ALERT_MANAGE,

    // Admin
    TENANT_READ,
    TENANT_WRITE,
    SOURCE_READ,
    SOURCE_WRITE,
    API_KEY_READ,
    API_KEY_WRITE,
    USER_READ,
    USER_WRITE
}
```

### Matrice Role-Permission

```
              EVENT  SEARCH  REPORT  RULE   ALERT  ADMIN
              R  W   R       R  W    R  W   R  M   T S K U
ADMIN         ✓  ✓   ✓       ✓  ✓    ✓  ✓   ✓  ✓   ✓ ✓ ✓ ✓
MANAGER       ✓  ✓   ✓       ✓  ✓    ✓  ✓   ✓  ✓   ✓ ✓ ✓ ✓
OPERATOR      ✓  -   ✓       ✓  -    ✓  -   ✓  ✓   - - - -
VIEWER        ✓  -   ✓       ✓  -    ✓  -   ✓  -   - - - -
```

### Implementation

```java
@PreAuthorize("hasPermission('EVENT_WRITE')")
@PostMapping("/api/v1/events")
public EventResponse createEvent(@RequestBody EventRequest request) {
    // ...
}

@PreAuthorize("hasRole('ADMIN') or @tenantAccessChecker.canAccess(#tenantId)")
@GetMapping("/api/v1/tenants/{tenantId}")
public TenantResponse getTenant(@PathVariable String tenantId) {
    // ...
}
```

---

## Multi-tenancy Security

### Isolation des donnees

Chaque tenant a ses donnees isolees par `tenantId`.

```java
@Entity
@Where(clause = "tenant_id = :tenantId")
public class Event {
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
}
```

### Tenant Context

```java
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static void clear() {
        currentTenant.remove();
    }
}
```

### Filtre automatique

```java
@Aspect
@Component
public class TenantFilterAspect {

    @Before("execution(* com.mohmk10.audittrail..*Repository.*(..))")
    public void applyTenantFilter(JoinPoint joinPoint) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            entityManager.unwrap(Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", tenantId);
        }
    }
}
```

---

## Data Integrity

### Event Hashing

Chaque evenement est hashe pour garantir l'integrite.

```java
public class EventHashService {

    public String computeHash(Event event, String previousHash) {
        String content = String.join("|",
            event.getId().toString(),
            event.getTimestamp().toString(),
            event.getActor().getId(),
            event.getAction().getType(),
            event.getResource().getId(),
            event.getMetadata().getTenantId(),
            previousHash != null ? previousHash : ""
        );

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }
}
```

### Hash Chain (Blockchain-like)

```
Event 1          Event 2          Event 3
┌─────────┐      ┌─────────┐      ┌─────────┐
│ hash: A │──────│prevHash:A│─────│prevHash:B│
│         │      │ hash: B  │     │ hash: C  │
└─────────┘      └─────────┘      └─────────┘
```

### Verification d'integrite

```java
public boolean verifyChain(List<Event> events) {
    String previousHash = null;

    for (Event event : events) {
        String computedHash = computeHash(event, previousHash);

        if (!computedHash.equals(event.getHash())) {
            return false; // Tampered!
        }

        previousHash = event.getHash();
    }

    return true;
}
```

### Signature numerique (optionnel)

```java
public class EventSignatureService {

    public String sign(Event event, PrivateKey privateKey) {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(event.getHash().getBytes());
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public boolean verify(Event event, String signatureBase64, PublicKey publicKey) {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(event.getHash().getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(signatureBytes);
    }
}
```

---

## Encryption

### At Rest

```yaml
# PostgreSQL avec encryption
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/audittrail?ssl=true&sslmode=require

# Elasticsearch avec encryption
spring:
  elasticsearch:
    uris: https://localhost:9200
    ssl:
      verification-mode: certificate
```

### In Transit

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    protocol: TLS
    enabled-protocols: TLSv1.3
```

### Sensitive Fields

```java
@Entity
public class ApiKey {

    @Convert(converter = EncryptedStringConverter.class)
    private String keyHash;

    @Convert(converter = EncryptedStringConverter.class)
    private String secret;
}
```

---

## Rate Limiting

### Par API Key

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) {
        String apiKeyId = getApiKeyId(request);
        ApiKeyDetails key = apiKeyService.get(apiKeyId);

        if (!rateLimiter.tryAcquire(apiKeyId, key.getRateLimit())) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            return;
        }

        chain.doFilter(request, response);
    }
}
```

### Configuration

```yaml
rate-limiting:
  default-limit: 1000        # req/min par defaut
  burst-capacity: 100        # Burst autorise
  refill-rate: 10            # Tokens/seconde

  # Limites par tier
  tiers:
    free:
      limit: 100
    standard:
      limit: 1000
    enterprise:
      limit: 10000
```

---

## Audit Logging

### Logs d'acces

```java
@Aspect
@Component
public class SecurityAuditAspect {

    @AfterReturning(
        pointcut = "@annotation(org.springframework.web.bind.annotation.PostMapping)",
        returning = "result"
    )
    public void auditWrite(JoinPoint joinPoint, Object result) {
        auditLogger.log(SecurityAuditEvent.builder()
            .type("API_WRITE")
            .principal(getCurrentPrincipal())
            .action(joinPoint.getSignature().getName())
            .resource(extractResource(joinPoint))
            .result("SUCCESS")
            .timestamp(Instant.now())
            .build());
    }

    @AfterThrowing(
        pointcut = "@annotation(org.springframework.web.bind.annotation.*Mapping)",
        throwing = "ex"
    )
    public void auditFailure(JoinPoint joinPoint, Exception ex) {
        auditLogger.log(SecurityAuditEvent.builder()
            .type("API_ERROR")
            .principal(getCurrentPrincipal())
            .action(joinPoint.getSignature().getName())
            .error(ex.getMessage())
            .result("FAILURE")
            .timestamp(Instant.now())
            .build());
    }
}
```

### Format de log

```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "type": "API_WRITE",
  "principal": {
    "apiKeyId": "ak_123",
    "tenantId": "tenant-001"
  },
  "action": "createEvent",
  "resource": "/api/v1/events",
  "method": "POST",
  "ip": "192.168.1.100",
  "userAgent": "AuditTrailSDK/1.0",
  "result": "SUCCESS",
  "duration": 45
}
```

---

## Security Headers

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'"))
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.block(true))
                .contentTypeOptions(content -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
            )
            .build();
    }
}
```

## Vulnerability Management

### Dependency Scanning

```yaml
# .github/workflows/security.yml
- name: Run Snyk
  uses: snyk/actions/maven@master
  with:
    args: --severity-threshold=high

- name: Run OWASP Dependency Check
  uses: dependency-check/Dependency-Check_Action@main
```

### SAST (Static Analysis)

```yaml
- name: Run CodeQL
  uses: github/codeql-action/analyze@v2
  with:
    languages: java
```

### Secret Scanning

```yaml
- name: Detect secrets
  uses: trufflesecurity/trufflehog@main
  with:
    path: ./
```
