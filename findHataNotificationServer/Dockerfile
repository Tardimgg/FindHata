FROM findhata-token-service AS token-service
FROM findhata-result AS result
FROM findhata-database-creator AS database-creator
FROM findhata-external-request-filter AS external-request-filter

FROM gradle:8.3-jdk20-alpine AS build
WORKDIR /app

COPY ./ ./
COPY --from=token-service /app/build/libs/*.jar ./libs
COPY --from=result /app/build/libs/*.jar ./libs
COPY --from=database-creator /app/build/libs/*.jar ./libs
COPY --from=external-request-filter /app/build/libs/*.jar ./libs

RUN ./gradlew bootJar

FROM eclipse-temurin:20-jre-alpine
COPY --from=build /app/build/libs/*.jar program.jar
CMD ["java", "-jar", "program.jar"]

