FROM eclipse-temurin
WORKDIR /app
COPY ./ ./
RUN mv *.jar program.jar
CMD ["java", "-jar", "program.jar"]
