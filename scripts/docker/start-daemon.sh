#!/usr/bin/env bash
# start-daemon.sh - Lancer le daemon Docker sous WSL sans systemd
# Usage: ./scripts/docker/start-daemon.sh [--rootless]
# Démarre dockerd en arrière-plan et affiche un résumé.

set -euo pipefail

ROOTLESS=${1:-}

if [[ $EUID -ne 0 ]]; then
  echo "[start-daemon] Relance avec sudo pour démarrer dockerd (nécessaire si mode root)."
  exec sudo -E bash "$0" "$@"
fi

if command -v systemctl >/dev/null 2>&1 && systemctl is-active docker >/dev/null 2>&1; then
  echo "[start-daemon] Docker déjà actif via systemd."; exit 0; fi

SOCK=/var/run/docker.sock
PIDFILE=/var/run/docker.pid
LOG=/tmp/dockerd-wsl.log

if [[ -S "$SOCK" ]]; then
  echo "[start-daemon] Socket $SOCK existe déjà. docker info devrait fonctionner."; exit 0; fi

mkdir -p /etc/docker
# Optionnel: écrire une config minimale si absente
if [[ ! -f /etc/docker/daemon.json ]]; then
  cat > /etc/docker/daemon.json <<'CFG'
{
  "data-root": "/var/lib/docker",
  "log-driver": "json-file",
  "log-opts": {"max-size": "10m", "max-file": "3"},
  "features": {"buildkit": true}
}
CFG
fi

if [[ "$ROOTLESS" == "--rootless" ]]; then
  echo "[start-daemon] Mode rootless demandé. (Installation rootless non automatisée ici)"
  echo "Consultez: https://docs.docker.com/engine/security/rootless/"
  exit 1
fi

echo "[start-daemon] Lancement dockerd ... (logs: $LOG)"
nohup dockerd --pidfile $PIDFILE >> "$LOG" 2>&1 &

# Attente disponibilité
for i in {1..30}; do
  if docker info >/dev/null 2>&1; then
    echo "[start-daemon] Docker prêt.";
    docker version --format '{{.Server.Version}}' 2>/dev/null || true
    exit 0
  fi
  sleep 1
  printf '.'
done

echo "\n[start-daemon] Echec démarrage (voir $LOG)"
exit 2
