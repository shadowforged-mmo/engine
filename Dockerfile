FROM gradle:9.3.1-jdk21 AS builder

WORKDIR /build

COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:21-jre

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/build/libs/*-all.jar app.jar

USER appuser

EXPOSE 25565

ENTRYPOINT ["java", "-Xms2G", "-Xmx2G", "-jar", "app.jar"]
CMD ["runtime", "/project"]
