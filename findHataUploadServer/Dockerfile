FROM gradle:8.3-jdk20-alpine
WORKDIR /app
COPY . .
RUN ./gradlew bootJar
CMD ["echo", "uploadServer ready"]
