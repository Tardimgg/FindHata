FROM gradle:8.3-jdk20 AS build
WORKDIR /app
COPY ./ ./
RUN ./build_dep && ./gradlew bootJar
# RUN ./gradlew bootJar && cd findHataUploadServer && ./gradlew bootJar

FROM eclipse-temurin:20-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar findHataProposalServer-0.0.1-SNAPSHOT.jar
COPY --from=build /app/libs/findHataUploadServer-0.0.1-SNAPSHOT.jar findHataUploadServer-0.0.1-SNAPSHOT.jar

COPY --from=build /app/docker/init.json init.json
COPY --from=build /app/docker/images/* images/

RUN echo "java -jar findHataProposalServer-0.0.1-SNAPSHOT.jar & java -jar findHataUploadServer-0.0.1-SNAPSHOT.jar" > start \
&& chmod +x ./start

CMD ["sh", "-c", "./start"]

