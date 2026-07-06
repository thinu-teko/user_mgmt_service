# Stage 1: Build (Gradle + JAR)

FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN sed -i 's/\r//' gradlew && \
    chmod +x gradlew && \
    ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Custom-JRE via jlink

FROM eclipse-temurin:25-jdk-alpine AS jre-builder

WORKDIR /extracted

RUN apk add --no-cache unzip

COPY --from=builder /app/build/libs/*.jar app.jar
RUN unzip -q app.jar -d extracted

# jdeps analysiert den Bytecode (eigener Code + alle Bibliotheken) und ermittelt dynamisch die tatsächlich benötigten Java-Module. Das läuft bei
# JEDEM Build automatisch neu ab. Zwar wüssten wir was aktuell für Module nötig sind, jedoch weil sich die benötigten Module ändern können,
# sobald neue Dependencies in build.gradle hinzukommen.
# Manuell nachschauen kann man das Ergebnis so:
#   docker build --target jre-builder -t modules-check .
#   docker run --rm modules-check cat /tmp/modules.txt

RUN jdeps -q \
      --multi-release 25 \
      --ignore-missing-deps \
      --print-module-deps \
      --class-path 'extracted/BOOT-INF/lib/*' \
      extracted/BOOT-INF/classes > /tmp/modules.txt

# jdk.crypto.ec wird ergänzt: für TLS-Verbindungen oft nötig,
# von jdeps' statischer Analyse aber manchmal nicht erkannt
RUN jlink \
      --add-modules "$(cat /tmp/modules.txt),jdk.crypto.ec" \
      --strip-debug \
      --no-header-files \
      --no-man-pages \
      --compress=2 \
      --output /customjre


      # Stage 3: Runtime (reines Alpine + Custom-JRE)

      FROM alpine:3.23.5

RUN apk add --no-cache ca-certificates tzdata

WORKDIR /app

RUN addgroup -S appgroup && adduser -S -G appgroup appuser

COPY --from=jre-builder /customjre /opt/java
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]