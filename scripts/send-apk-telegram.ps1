# Sends an APK to Telegram with the Bot API sendDocument method.
#
# Usage:
#   $env:TELEGRAM_BOT_TOKEN = "..."
#   $env:TELEGRAM_CHAT_ID = "..."
#   .\scripts\send-apk-telegram.ps1 -Apk app\build\outputs\apk\debug\app-debug.apk -Caption "Sona build"
#
# Exits 0 without sending when the secrets are absent.

param(
    [string]$Apk = "app/build/outputs/apk/debug/app-debug.apk",
    [string]$Caption = "Sona build"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:TELEGRAM_BOT_TOKEN) -or
    [string]::IsNullOrWhiteSpace($env:TELEGRAM_CHAT_ID)) {
    Write-Host "TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID is missing - skipping Telegram delivery."
    exit 0
}

if (-not (Test-Path $Apk)) {
    Write-Error "APK not found: $Apk"
    exit 1
}

$sizeMb = [math]::Round((Get-Item $Apk).Length / 1MB, 1)
Write-Host "Sending $Apk ($sizeMb MB) to chat $($env:TELEGRAM_CHAT_ID)..."

& curl.exe --fail --silent --show-error `
    --max-time 120 `
    -F "chat_id=$($env:TELEGRAM_CHAT_ID)" `
    -F "caption=$Caption ($sizeMb MB)" `
    -F "document=@$Apk" `
    "https://api.telegram.org/bot$($env:TELEGRAM_BOT_TOKEN)/sendDocument"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Telegram upload failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "APK delivered to Telegram."
