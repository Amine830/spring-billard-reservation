#!/usr/bin/env bash
# status.sh - Résumé état Docker Billard-Book
set -euo pipefail

# Détection commande compose (plugin v2 ou binaire legacy)
if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
else
  echo "Docker Compose non trouvé" >&2
  exit 1
fi

echo "== Services =="
"${COMPOSE_CMD[@]}" ps || true

echo "== Health =="
if curl -sf http://localhost:8080/actuator/health; then echo; else echo "(indispo)"; fi

echo "== Images liées (approx) =="
docker images | grep -i billard || true

echo "== Logs récents API =="
"${COMPOSE_CMD[@]}" logs --tail=30 billard-api || true
