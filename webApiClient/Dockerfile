FROM findhata-token-service AS token-service

FROM gradle:8.3-jdk20-alpine
WORKDIR /app
COPY . .
COPY --from=token-service /app/build/libs/*.jar ./libs

RUN ./gradlew shadowJar
CMD ["echo", "webApiClient ready"]
