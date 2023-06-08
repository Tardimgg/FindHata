FROM eclipse-temurin:20-jre-alpine
WORKDIR /app
COPY ./ ./

# && mv base.jpg ./images/base.jpg \
# && mv base1.jpg ./images/base1.jpg \
# && mv *.jpg ./images \
# RUN mkdir images

RUN echo "java -jar findHataProposalServer-0.0.1-SNAPSHOT.jar & java -jar findHataUploadServer-0.0.1-SNAPSHOT.jar" > start \
&& chmod +x ./start

CMD ["sh", "-c", "./start"]
