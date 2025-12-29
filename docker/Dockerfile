# ============================================
# Build Stage
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy Maven files for dependency caching
COPY pom.xml .
COPY audit-trail-core/pom.xml audit-trail-core/
COPY audit-trail-storage/pom.xml audit-trail-storage/
COPY audit-trail-ingestion/pom.xml audit-trail-ingestion/
COPY audit-trail-search/pom.xml audit-trail-search/
COPY audit-trail-reporting/pom.xml audit-trail-reporting/
COPY audit-trail-detection/pom.xml audit-trail-detection/
COPY audit-trail-admin/pom.xml audit-trail-admin/
COPY audit-trail-integration/pom.xml audit-trail-integration/
COPY audit-trail-app/pom.xml audit-trail-app/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY . .

# Build the application
RUN mvn clean package -DskipTests -B && \
    mv audit-trail-app/target/*.jar app.jar

# ============================================
# Runtime Stage
# ============================================
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="Audit Trail Server"
LABEL org.opencontainers.image.description="Universal audit logging system"
LABEL org.opencontainers.image.vendor="devmohmk"
LABEL org.opencontainers.image.source="https://github.com/Mohmk10/audit-trail-server"
LABEL org.opencontainers.image.licenses="MIT"

# Security: Run as non-root user
RUN addgroup -g 1001 audittrail && \
    adduser -u 1001 -G audittrail -D audittrail

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/app.jar app.jar

# Create directories for data
RUN mkdir -p /app/logs /app/reports /app/data && \
    chown -R audittrail:audittrail /app

USER audittrail

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs \
    -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
