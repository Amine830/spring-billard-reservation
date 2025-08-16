#!/usr/bin/env bash
# stop.sh - Arrêt propre Billard-Book
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

"${COMPOSE_CMD[@]}" down

if [[ "${1:-}" == "--prune" ]]; then
  docker system prune -f
fi

echo "Arrêt terminé."