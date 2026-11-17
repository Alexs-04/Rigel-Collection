# ========== STAGE 1: Build ==========
FROM docker.io/library/gradle:8.14-jdk21 AS build

WORKDIR /app

# Resolve dependencies first: this layer stays cached until the build files change.
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon > /dev/null

COPY src ./src
RUN gradle bootJar --no-daemon

# ========== STAGE 2: Run ==========
FROM docker.io/library/eclipse-temurin:21-jre

WORKDIR /app

# Run as an unprivileged user that owns the local image storage.
RUN useradd --system --uid 10001 --home-dir /app rigel \
    && mkdir -p /app/uploads \
    && chown -R rigel /app

COPY --from=build /app/build/libs/*.jar app.jar

USER rigel

ENV IMAGE_STORAGE_PATH=/app/uploads
VOLUME /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
