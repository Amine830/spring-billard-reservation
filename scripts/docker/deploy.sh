#!/usr/bin/env bash
# deploy.sh - Déploiement (build + up) Billard-Book
set -euo pipefail

echo "[deploy] Build & start containers"
command -v docker >/dev/null || { echo "Docker requis"; exit 1; }

# Détection commande compose (plugin v2 ou binaire legacy)
if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
else
  echo "Docker Compose non trouvé (ni plugin v2 ni binaire docker-compose)" >&2
  exit 1
fi

# Vérifier support de l'option --remove-orphans
REMOVE_ORPHANS=""
if "${COMPOSE_CMD[@]}" down --help 2>&1 | grep -q -- '--remove-orphans'; then
  REMOVE_ORPHANS="--remove-orphans"
fi

echo "[deploy] Using compose command: ${COMPOSE_CMD[*]}"
if [[ -n "$REMOVE_ORPHANS" ]]; then
  echo "[deploy] Option --remove-orphans support: oui"
else
  echo "[deploy] Option --remove-orphans support: non"
fi

# Vérifier que le daemon Docker répond (socket disponible)
if ! docker info >/dev/null 2>&1; then
  echo "[deploy] Daemon Docker indisponible (socket non accessible)." >&2
  echo "Diagnostic rapide:" >&2
  echo "  - docker ps  (échouera probablement)" >&2
  echo "  - Assurez-vous que le service Docker tourne (systemd ou dockerd manuel)" >&2
  echo "  - Sous WSL sans service: lancez 'dockerd' dans un autre terminal ou utilisez Docker Desktop" >&2
  exit 2
fi

"${COMPOSE_CMD[@]}" down $REMOVE_ORPHANS || true
"${COMPOSE_CMD[@]}" build
mkdir -p logs
"${COMPOSE_CMD[@]}" up -d

# Attente simple (le healthcheck prendra ensuite le relai)
sleep 5 || true

if curl -sf http://localhost:8080/actuator/health >/dev/null; then
  echo "API UP: http://localhost:8080"
else
  echo "(Info) L'endpoint health ne répond pas encore. Vérifiez: ${COMPOSE_CMD[*]} logs -f billard-api"
fi

echo "Terminé. Logs: ${COMPOSE_CMD[*]} logs -f billard-api"
