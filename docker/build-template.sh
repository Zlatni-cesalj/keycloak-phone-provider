#!/bin/sh

if [ "$1" = "test" ] || [[ @project.version@ = *"snapshot" ]] || [[ @project.version@ = *"SNAPSHOT" ]]
then
  docker build -t dpranjic/keycloak:@version.keycloak@_phone-@project.version@ .
else
  docker buildx build . --platform linux/amd64,linux/arm64 --push -t dpranjic/keycloak:@version.keycloak@_phone-@project.version@
fi
