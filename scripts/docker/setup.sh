#!/usr/bin/env bash
# setup.sh - Installation rapide Docker (Ubuntu/WSL)
set -euo pipefail

if ! command -v docker >/dev/null; then
  sudo apt update && sudo apt install -y docker.io docker-compose-plugin
fi
sudo usermod -aG docker "$USER" || true

echo "Docker version:"; docker --version
command -v docker compose >/dev/null && docker compose version || docker-compose --version || true

echo "Setup terminé. Reloguez ou 'newgrp docker'."
