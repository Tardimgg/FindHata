#!/bin/sh
python3 -m dostoevsky download fasttext-social-network-model &&
 python3 -m spacy download ru_core_news_lg &&
 pip cache purge
