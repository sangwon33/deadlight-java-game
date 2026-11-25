FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY . .
RUN sh ./gradlew --no-daemon :server:installDist --console=plain

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system deadlight && useradd --system --gid deadlight --home-dir /app deadlight
COPY --from=build --chown=deadlight:deadlight /workspace/server/build/install/server/ ./
USER deadlight
ENV SERVER_ADDRESS=0.0.0.0
ENV SPRING_PROFILES_ACTIVE=postgres
ENV JAVA_OPTS="-XX:MaxRAMPercentage=60 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["/app/bin/server"]
