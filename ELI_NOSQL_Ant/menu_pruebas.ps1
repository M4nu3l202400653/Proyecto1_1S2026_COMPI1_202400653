# Menu principal de pruebas

while ($true) {
    Clear-Host
    Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║     ELI NOSQL - Entorno de Prueba     ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Configurar entorno (compilar)" -ForegroundColor Yellow
    Write-Host "2. Crear ejemplos de prueba" -ForegroundColor Yellow
    Write-Host "3. Ejecutar ejemplos automáticamente" -ForegroundColor Yellow
    Write-Host "4. Abrir GUI (interfaz gráfica)" -ForegroundColor Yellow
    Write-Host "5. Ver README" -ForegroundColor Yellow
    Write-Host "6. Salir" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "════════════════════════════════════════" -ForegroundColor Gray
    Write-Host "Selecciona una opción (1-6): " -NoNewline -ForegroundColor Cyan
    
    $opcion = Read-Host
    
    switch ($opcion) {
        "1" {
            Write-Host ""
            Write-Host "Compilando..." -ForegroundColor Yellow
            & .\setup_test_env.ps1
            Write-Host ""
            Read-Host "Presiona Enter para continuar"
        }
        "2" {
            Write-Host ""
            Write-Host "Creando ejemplos..." -ForegroundColor Yellow
            & .\create_examples.ps1
            Write-Host ""
            Read-Host "Presiona Enter para continuar"
        }
        "3" {
            Write-Host ""
            Write-Host "Ejecutando ejemplos..." -ForegroundColor Yellow
            & .\run_examples.ps1
            Write-Host ""
            Read-Host "Presiona Enter para continuar"
        }
        "4" {
            Write-Host ""
            Write-Host "Iniciando GUI..." -ForegroundColor Yellow
            & ant run 2>&1 | Out-Null
        }
        "5" {
            if (Test-Path "README.md") {
                Get-Content README.md | More
            } else {
                Write-Host "README.md no encontrado" -ForegroundColor Red
            }
            Write-Host ""
            Read-Host "Presiona Enter para continuar"
        }
        "6" {
            Write-Host ""
            Write-Host "¡Hasta luego!" -ForegroundColor Green
            exit 0
        }
        default {
            Write-Host "Opción no válida" -ForegroundColor Red
            Start-Sleep -Seconds 1
        }
    }
}
