FROM ubuntu:latest
LABEL authors="marcinochmanski"

ENTRYPOINT ["top", "-b"]