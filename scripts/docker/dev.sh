#!/usr/bin/env bash
# dev.sh - Mode développement avec rebuild API sur changements
set -euo pipefail

DC="docker compose"
$DC up -d

rebuild() {
  echo "[dev] Rebuild API…"
  $DC build billard-api
  $DC up -d billard-api
}

if command -v inotifywait >/dev/null; then
  echo "Surveillance server/src (Ctrl+C pour quitter)"
  while inotifywait -r -e modify,create,delete --exclude 'target|tmp|.*swp' server/src; do
    rebuild
  done
else
  echo "inotifywait absent. Appuyez Entrée pour rebuild, Ctrl+C pour quitter."
  while read -r _; do rebuild; done
fi
