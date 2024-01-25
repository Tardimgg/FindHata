FROM gradle:8.3-jdk20-alpine
WORKDIR /app
COPY . .
RUN ./gradlew shadowJar
CMD ["echo", "result ready"]
