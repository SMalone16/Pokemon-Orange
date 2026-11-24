#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
base=http://127.0.0.1:4567
name="Test$(openssl rand -hex 4)"
curl -fsS --max-time 10 "$base/health" | grep -qx ok
curl -fsS --max-time 10 "$base/" | grep 'Pokemon Orange' >/dev/null
for asset in jquery.min.js js.cookie.min.js; do
  curl -fsS --max-time 10 "$base/js/lib/$asset" -o /dev/null
done
join() { curl -fsS --max-time 20 "$base/join" --data-urlencode "name=$1"; }
join "!" | grep -q '"success":false'
join "$name" | grep -q '"success":true'
join "$name" | grep -q '"success":false'
for asset in front-pidgey.png front-pidgey.json back-pidgey.png back-pidgey.json; do
  curl -fsS --max-time 10 "$base/assets/sprites/pokemon/$asset" -o /dev/null
done
echo "Smoke test passed: HTTP, assets, name-only join and name takeover rejection."
echo "Disposable test account created: $name"

python3 scripts/test-player-resume.py
