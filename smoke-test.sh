#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
base=http://127.0.0.1:4567
code=$(sed -n 's/^CLASS_CODE=//p' .env)
test -n "$code"
name="Test$(openssl rand -hex 4)"
pass=$(openssl rand -hex 12)
curl -fsS --max-time 10 "$base/health" | grep -qx ok
curl -fsS --max-time 10 "$base/" | grep 'Pokemon Orange' >/dev/null
for asset in jquery.min.js js.cookie.min.js; do
  curl -fsS --max-time 10 "$base/js/lib/$asset" -o /dev/null
done
register() {
  curl -fsS --max-time 20 "$base/register" \
    --data-urlencode "username=$name" \
    --data-urlencode "password=$pass" \
    --data-urlencode "classCode=$1" \
    --data-urlencode "species=bulbasaur" \
    --data-urlencode "nickname=Sprout"
}
register "incorrect-code" | grep -q '"success":false'
register "$code" | grep -q '"success":true'
curl -fsS --max-time 20 "$base/login" \
  --data-urlencode "username=$name" \
  --data-urlencode "password=$pass" | grep -q '"success":true'
echo "Smoke test passed: HTTP, assets, registration gate, registration and login."
echo "Test account created: $name"
