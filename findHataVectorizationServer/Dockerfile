FROM python:3.8-slim as build
WORKDIR /app

ENV VIRTUAL_ENV=venv
RUN python3 -m venv $VIRTUAL_ENV
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

RUN apt update && apt install -y build-essential && rm -rf /var/lib/apt/lists/*

COPY install-dep.sh .
RUN chmod +x install-dep.sh
RUN ./install-dep.sh

COPY profile.proto .
RUN mkdir grpc-files
COPY generate-proto.sh .
RUN chmod +x generate-proto.sh
RUN ./generate-proto.sh

#
COPY download_models.sh .
RUN chmod +x download_models.sh
RUN ./download_models.sh
#

FROM python:3.8-slim
WORKDIR /app

COPY --from=build /app/venv /app/venv
ENV PATH="/app/venv/bin:$PATH"

COPY --from=build /app/grpc-files /app/src

COPY ./src/ ./src/

CMD ["python", "src/main.py"]


