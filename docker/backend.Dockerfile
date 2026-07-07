# Shared build for all Spring Boot modules in the mizan-parent Maven reactor.
# Build with: docker build -f docker/backend.Dockerfile --build-arg SERVICE=auth-service --build-arg PORT=8081 .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
ARG SERVICE
RUN chmod +x mvnw && ./mvnw -q -pl ${SERVICE} -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
ARG SERVICE
ARG PORT
ENV PORT=${PORT}
RUN addgroup -S mizan && adduser -S mizan -G mizan
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-*.jar app.jar
USER mizan
EXPOSE ${PORT}
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s CMD wget -qO- http://localhost:$PORT/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
