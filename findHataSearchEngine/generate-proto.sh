#!/bin/sh
python -m grpc_tools.protoc -I . --python_out=./grpc-files \
  --pyi_out=./grpc-files --grpc_python_out=./grpc-files ./profile.proto
