#!/usr/bin/env bash
# build-push.sh - Construction et push image API Billard-Book
# Usage: ./scripts/docker/build-push.sh <dockerhub_user> <version>
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <dockerhub_user> <version>" >&2
  exit 1
fi
USER="$1"
VERSION="$2"
IMAGE="$USER/billard-book-api"

echo "[build] Image: $IMAGE:$VERSION"

docker build -t "$IMAGE:$VERSION" -t "$IMAGE:latest" server/

echo "[test] Lancement conteneur éphémère pour healthcheck"
CID=$(docker run -d -p 8080:8080 "$IMAGE:$VERSION")
trap 'docker rm -f $CID >/dev/null 2>&1 || true' EXIT

printf "[wait] Attente démarrage"; for i in {1..30}; do
  if curl -sf http://localhost:8080/actuator/health >/dev/null; then echo " => UP"; break; fi
  sleep 2; printf '.'
  if [[ $i -eq 30 ]]; then echo "\n[error] Healthcheck KO"; exit 2; fi
done

echo "[push] Envoi vers Docker Hub"
docker push "$IMAGE:$VERSION"
docker push "$IMAGE:latest"

echo "[done] $IMAGE:$VERSION disponible"
