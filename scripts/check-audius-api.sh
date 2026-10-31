#!/usr/bin/env bash
# Live Audius API smoke test.
#
# The public API is fronted by Cloudflare; datacenter egress can get a 403.
# The GitHub Actions job is marked continue-on-error, so a block here never
# fails the build. Exit codes: 0 = API answered, 1 = blocked/failed.
set -euo pipefail

BASE_URL="${AUDIUS_BASE_URL:-https://api.audius.co/v1}"
APP_NAME="${AUDIUS_APP_NAME:-Sona}"
TMP_BODY="$(mktemp)"
trap 'rm -f "$TMP_BODY"' EXIT

check() {
  local name="$1"
  local path="$2"
  local url="${BASE_URL}${path}"

  local code
  code="$(curl --silent --show-error --location \
    --max-time 25 \
    --output "$TMP_BODY" \
    --write-out '%{http_code}' \
    --header 'Accept: application/json' \
    --user-agent 'Sona-CI/1.1 (+https://github.com/JamikKhidirov/Sona)' \
    "$url" || echo "000")"

  if [[ "$code" == "200" ]]; then
    echo "OK   $name -> HTTP 200 ($url)"
    return 0
  fi

  echo "FAIL $name -> HTTP $code ($url)" >&2
  head -c 200 "$TMP_BODY" >&2 || true
  echo >&2
  return 1
}

failures=0
check "tracks/trending" "/tracks/trending?app_name=${APP_NAME}&limit=3&time=week" || failures=$((failures + 1))
check "playlists/trending" "/playlists/trending?app_name=${APP_NAME}&limit=3&time=week" || failures=$((failures + 1))
check "users/search" "/users/search?query=lofi&app_name=${APP_NAME}" || failures=$((failures + 1))

if [[ "$failures" -gt 0 ]]; then
  echo >&2
  echo "Audius API is not reachable from this runner ($failures endpoint(s) failed)." >&2
  echo "This is usually Cloudflare blocking datacenter IPs; the app still works from a device." >&2
  exit 1
fi

echo "All Audius endpoints answered with HTTP 200."
