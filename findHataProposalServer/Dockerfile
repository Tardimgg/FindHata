FROM findhata-result AS result
FROM findhata-database-creator AS database-creator
FROM findhata-external-request-filter AS external-request-filter
FROM findhata-upload-server AS upload-server

FROM gradle:8.3-jdk20 AS build
WORKDIR /app

COPY ./ ./
COPY --from=result /app/build/libs/*.jar ./libs/
COPY --from=database-creator /app/build/libs/*.jar ./libs/
COPY --from=external-request-filter /app/build/libs/*.jar ./libs/

RUN ./gradlew bootJar

FROM eclipse-temurin:20-jre
WORKDIR /app

COPY --from=upload-server /app/build/libs/findHataUploadServer-0.0.1-SNAPSHOT.jar findHataUploadServer-0.0.1-SNAPSHOT.jar
COPY --from=build /app/build/libs/*.jar findHataProposalServer-0.0.1-SNAPSHOT.jar

COPY --from=build /app/docker/init.json init.json
COPY --from=build /app/docker/images/* images/

RUN echo "java -jar findHataProposalServer-0.0.1-SNAPSHOT.jar & java -jar findHataUploadServer-0.0.1-SNAPSHOT.jar" > start \
&& chmod +x ./start

CMD ["sh", "-c", "./start"]

