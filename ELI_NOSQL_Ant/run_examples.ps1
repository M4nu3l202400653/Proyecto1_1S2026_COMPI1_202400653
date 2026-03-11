# Script para ejecutar ejemplos de prueba

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Ejecutando ejemplos de ELI NOSQL" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$CP = "build\classes;lib\javacup-runtime.jar;lib\javacup.jar;lib\jflex-1.9.1.jar"
$ejemplos = @(
    @{ nombre = "dificil.code"; desc = "E-commerce con tablas complejas" },
    @{ nombre = "medio.code"; desc = "Colegio con operaciones varias" },
    @{ nombre = "prueba.code"; desc = "Universidad (sin 'use'; sin BD activa)" }
)

$total = $ejemplos.Count
$exitosos = 0

foreach ($i in 0..($ejemplos.Count - 1)) {
    $ejemplo = $ejemplos[$i]
    $num = $i + 1
    
    Write-Host "[$num/$total] Ejecutando: $($ejemplo.nombre)" -ForegroundColor Yellow
    Write-Host "  Descripción: $($ejemplo.desc)" -ForegroundColor Gray
    
    if (-not (Test-Path $ejemplo.nombre)) {
        Write-Host "  ✗ Archivo no encontrado" -ForegroundColor Red
        continue
    }
    
    # Leer archivo
    $codigo = Get-Content $ejemplo.nombre -Raw -Encoding UTF8
    
    # Ejecutar
    $resultado = @"
package analizadores;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestEjemplo {
    public static void main(String[] args) throws Exception {
        String codigo = new String(Files.readAllBytes(Paths.get("$($ejemplo.nombre)")), "UTF-8");
        Analizar motor = new Analizar();
        motor.analizar(codigo);
        
        System.out.println("=== EJEMPLO: $($ejemplo.nombre) ===");
        System.out.println("TOKENS: " + motor.getTokens().size());
        System.out.println("ERRORES LEXICOS: " + motor.getErroresLexicos().size());
        System.out.println("ERRORES SINTACTICOS: " + motor.getErroresSintacticos().size());
        System.out.println("ERRORES SEMANTICOS: " + motor.getErroresEjecucion().size());
        
        if (motor.getTablaSimbolos() != null && motor.getTablaSimbolos().getDbActiva() != null) {
            String ruta = motor.getTablaSimbolos().getDbActiva().getRutaArchivo();
            System.out.println("BD GENERADA: " + ruta);
            System.out.println("STATUS: ✓ EXITO");
        } else {
            System.out.println("STATUS: ✗ Sin BD activa");
        }
        System.out.println();
    }
}
"@
    
    Set-Content -Path "build\classes\analizadores\TestEjemplo.java" -Value $resultado -Encoding UTF8
    
    # Compilar el test
    javac -cp $CP -d build\classes build\classes\analizadores\TestEjemplo.java 2>$null
    
    # Ejecutar
    java -cp $CP analizadores.TestEjemplo 2>&1
    
    $exitosos++
}

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Resumen: $exitosos/$total ejemplos procesados" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Archivos JSON generados:" -ForegroundColor Yellow
Get-ChildItem -Path "*.json" -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "  ✓ $($_.Name) ($([math]::Round($_.Length/1024, 2)) KB)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Para usar la interfaz gráfica, ejecuta: run_app.ps1" -ForegroundColor Cyan
