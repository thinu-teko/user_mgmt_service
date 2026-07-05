# ================================
# Stage 1: Build
# ================================
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Zeilenenden korrigieren und Dependencies cachen
RUN sed -i 's/\r//' gradlew && \
    chmod +x gradlew && \
    ./gradlew dependencies --no-daemon

# Source bauen
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ================================
# Stage 2: Runtime
# ================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S -G appgroup appuser

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

ENV SPRING_DATASOURCE_URL=""
ENV SPRING_DATASOURCE_USERNAME=""
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=""
ENV JWT_ISSUER=""
ENV JWT_EXPIRATION_MILLIS=""

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]