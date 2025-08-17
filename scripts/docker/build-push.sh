#!/usr/bin/env bash
# build-push.sh - Construction et push image API Billard-Book
# Usage: ./scripts/docker/build-push.sh <dockerhub_user> <version>
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <dockerhub_user> <version>" >&2
  exit 1
fi

ORIG_USER="$1"
# Normaliser en minuscules (Docker Hub exige lowercase)
USER="$(echo "$ORIG_USER" | tr 'A-Z' 'a-z')"
if [[ "$USER" != "$ORIG_USER" ]]; then
  echo "[info] Nom utilisateur normalisé en minuscules: $USER"
fi

# Validation simple (éviter interprétation comme registry custom)
if [[ "$USER" =~ [^a-z0-9] ]]; then
  echo "[error] Nom utilisateur invalide: $ORIG_USER (caractères autorisés: a-z0-9)" >&2
  exit 2
fi

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

echo "[push] Envoi vers Docker Hub (repo: $IMAGE)"
docker push "$IMAGE:$VERSION"
docker push "$IMAGE:latest"

echo "[done] $IMAGE:$VERSION disponible"
