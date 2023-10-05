FROM gradle:8.3-jdk20-alpine AS build
WORKDIR /app
COPY ./ ./

RUN ./build_dep && ./gradlew bootJar

FROM eclipse-temurin:20-jre-alpine
COPY --from=build /app/build/libs/*.jar program.jar
CMD ["java", "-jar", "program.jar"]

