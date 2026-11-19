param([switch]$SkipDatabase)
$ErrorActionPreference = 'Stop'
function Read-Password([string]$Label) {
    $secure = Read-Host $Label -AsSecureString
    return [System.Net.NetworkCredential]::new('', $secure).Password
}
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    if (-not $env:PULSEPASS_E2E_DB_PASSWORD) { $env:PULSEPASS_E2E_DB_PASSWORD = Read-Password 'Contraseña de PostgreSQL E2E (conservar para próximos arranques)' }
    if (-not $env:PULSEPASS_E2E_ADMIN_PASSWORD) { $env:PULSEPASS_E2E_ADMIN_PASSWORD = Read-Password 'Contraseña de e2e-admin (mínimo 12 caracteres)' }
    if (-not $env:PULSEPASS_E2E_STAFF_PASSWORD) { $env:PULSEPASS_E2E_STAFF_PASSWORD = Read-Password 'Contraseña de e2e-staff (mínimo 12 caracteres)' }
    if (-not $env:PULSEPASS_E2E_INACTIVE_PASSWORD) { $env:PULSEPASS_E2E_INACTIVE_PASSWORD = Read-Password 'Contraseña de e2e-inactive (mínimo 12 caracteres)' }
    if (-not $env:PULSEPASS_JWT_SECRET_BASE64) {
        $bytes = New-Object byte[] 32
        $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
        $env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
    }
    $env:DB_URL = 'jdbc:postgresql://localhost:5433/pulsepass_e2e'
    $env:DB_USER = 'pulsepass'
    $env:DB_PASSWORD = $env:PULSEPASS_E2E_DB_PASSWORD
    $env:SPRING_PROFILES_ACTIVE = 'e2e'
    if (-not $SkipDatabase) {
        docker compose -f compose.e2e.yml up -d --wait
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar PostgreSQL E2E' }
    }
    & .\mvnw.cmd spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'PulsePass terminó con error' }
} finally { Pop-Location }
