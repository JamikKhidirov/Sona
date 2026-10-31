# Live Audius API smoke test for Windows PowerShell.
# Exit code 0 = API answered, 1 = blocked or failed.
# Cloudflare may return 403 for datacenter/VPN egress; the app still works
# from a real device.

param(
    [string]$BaseUrl = "https://api.audius.co/v1",
    [string]$AppName = "Sona"
)

$ErrorActionPreference = "Stop"
$failures = 0

function Test-Endpoint {
    param([string]$Name, [string]$Path)

    $url = "$BaseUrl$Path"
    $body = Join-Path $env:TEMP "sona-api-smoke.json"

    $code = & curl.exe --silent --show-error --location `
        --max-time 25 `
        --output $body `
        --write-out "%{http_code}" `
        --header "Accept: application/json" `
        --user-agent "Sona-CI/1.1" `
        $url 2>$null

    if ($code -eq "200") {
        Write-Host "OK   $Name -> HTTP 200 ($url)"
        return $true
    }

    Write-Host "FAIL $Name -> HTTP $code ($url)" -ForegroundColor Yellow
    if (Test-Path $body) {
        Get-Content $body -TotalCount 3 -ErrorAction SilentlyContinue
    }
    return $false
}

if (-not (Test-Endpoint "tracks/trending" "/tracks/trending?app_name=$AppName&limit=3&time=week")) { $failures++ }
if (-not (Test-Endpoint "playlists/trending" "/playlists/trending?app_name=$AppName&limit=3&time=week")) { $failures++ }
if (-not (Test-Endpoint "users/search" "/users/search?query=lofi&app_name=$AppName")) { $failures++ }

if ($failures -gt 0) {
    Write-Host ""
    Write-Host "Audius API is not reachable from this machine ($failures endpoint(s) failed)." -ForegroundColor Red
    Write-Host "This is usually Cloudflare blocking datacenter/VPN IPs; the app still works from a device."
    exit 1
}

Write-Host "All Audius endpoints answered with HTTP 200."
