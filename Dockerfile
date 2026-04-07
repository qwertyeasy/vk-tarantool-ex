FROM gradle:8.3.0-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle
RUN rm -rf /root/.gradle/caches/
RUN gradle --no-daemon dependencies --stacktrace --info
COPY src ./src
RUN gradle --no-daemon build -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
