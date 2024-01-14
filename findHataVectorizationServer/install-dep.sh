#!/bin/sh
pip install --no-cache-dir -U pip setuptools wheel &&
 pip install --no-cache-dir -U spacy &&
 pip install --no-cache-dir dostoevsky &&
 pip install --no-cache-dir grpcio-tools &&
 pip cache purge
