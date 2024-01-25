FROM findhata-web-api-client AS web-api-client
FROM findhata-result AS result

FROM gradle:8.3-jdk20-alpine AS build
WORKDIR /app


COPY ./ ./
COPY --from=web-api-client /app/build/libs/*.jar ./libs
COPY --from=result /app/build/libs/*.jar ./libs

RUN ./gradlew bootJar

FROM eclipse-temurin:20-jre-alpine
COPY --from=build /app/build/libs/*.jar program.jar
CMD ["java", "-jar", "program.jar"]

