FROM eclipse-temurin:20-jre-alpine
WORKDIR /app
COPY ./ ./
RUN mv *.jar program.jar
CMD ["java", "-jar", "program.jar"]
