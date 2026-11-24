#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
command -v docker >/dev/null || { echo "Docker is required. Use a GitHub Codespace."; exit 1; }
docker info >/dev/null || { echo "Docker is not running or accessible."; exit 1; }
docker compose version >/dev/null
if [[ ! -e .env ]]; then
  umask 077
  db_password=$(openssl rand -hex 24)
  class_code=$(openssl rand -hex 8)
  printf 'DB_PASSWORD=%s\nCLASS_CODE=%s\nENABLE_CHAT=false\n' "$db_password" "$class_code" > .env
fi
chmod 600 .env
# Local authorized art is automatically included by the Docker sprite builder.
if [[ "${1:-}" == "--download-authorized" ]]; then
  python3 scripts/prepare-sprites.py --download-authorized
fi
# Compose must use the saved credentials rather than a stale shell variable.
unset DB_PASSWORD CLASS_CODE ENABLE_CHAT
if ! docker compose up -d --build --wait --wait-timeout 180; then
  docker compose ps
  docker compose logs --tail=80
  exit 1
fi
echo
if ! curl -fsS --max-time 10 http://127.0.0.1:4567/health | grep -qx ok; then
  echo "Game container is healthy but the Codespace cannot reach port 4567."
  docker compose ps
  exit 1
fi
echo "Pokemon Orange is ready. Open port 4567 in the Codespaces Ports tab."
echo "Share the game URL only. Students choose an unused nickname."
echo "For supervised hosting, use: docker compose logs -f game"
echo "Stop with: docker compose down"
