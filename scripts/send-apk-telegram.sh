#!/usr/bin/env bash
# Sends an APK to Telegram with the Bot API sendDocument method.
#
# Usage:
#   TELEGRAM_BOT_TOKEN=... TELEGRAM_CHAT_ID=... \
#     scripts/send-apk-telegram.sh app/build/outputs/apk/debug/app-debug.apk "Sona build"
#
# Exits 0 without sending when the secrets are absent, so local/CI runs
# without secrets do not fail.
set -euo pipefail

APK="${1:-app/build/outputs/apk/debug/app-debug.apk}"
CAPTION="${2:-Sona build}"

if [[ -z "${TELEGRAM_BOT_TOKEN:-}" || -z "${TELEGRAM_CHAT_ID:-}" ]]; then
  echo "TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID is missing - skipping Telegram delivery."
  exit 0
fi

if [[ ! -f "$APK" ]]; then
  echo "APK not found: $APK" >&2
  exit 1
fi

SIZE="$(du -h "$APK" | cut -f1)"
echo "Sending $APK ($SIZE) to chat $TELEGRAM_CHAT_ID..."

curl --fail --silent --show-error \
  --max-time 120 \
  -F "chat_id=${TELEGRAM_CHAT_ID}" \
  -F "caption=${CAPTION} ($SIZE)" \
  -F "document=@${APK}" \
  "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendDocument"

echo
echo "APK delivered to Telegram."
