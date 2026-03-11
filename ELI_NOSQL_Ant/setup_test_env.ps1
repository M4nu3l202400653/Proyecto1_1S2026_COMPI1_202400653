# Script para configurar entorno de prueba de ELI NOSQL

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Configurando entorno de prueba" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Crear directorios necesarios
Write-Host "[1/3] Creando estructura de directorios..." -ForegroundColor Yellow

$dirs = @(
    "test_examples",
    "test_output",
    "databases"
)

foreach ($dir in $dirs) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
        Write-Host "  ✓ Directorio '$dir' creado" -ForegroundColor Green
    } else {
        Write-Host "  ✓ Directorio '$dir' ya existe" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "[2/3] Compilando proyecto..." -ForegroundColor Yellow

# Compilar
& ant clean lexer parser compile 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Compilación exitosa" -ForegroundColor Green
} else {
    Write-Host "  ✗ Error compilando" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[3/3] Entorno listo para pruebas" -ForegroundColor Yellow
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "CÓMO PROBAR:" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Opción 1: Interfaz Gráfica"
Write-Host "  PS> run_app.ps1"
Write-Host ""
Write-Host "Opción 2: Ejecutar ejemplos"
Write-Host "  PS> .\run_examples.ps1"
Write-Host ""
Write-Host "Opción 3: Compilar manualmente"
Write-Host "  PS> ant run"
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
