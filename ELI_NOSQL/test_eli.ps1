# Test de integracion ELI NOSQL
Set-Location 'C:\Users\octav\Documents\ELI_NOSQL_Ant'

$CP = 'build\classes;lib\java-cup-11b-runtime.jar;lib\java-cup-11b.jar'

# Crear un archivo de prueba
$codigoPrueba = @'
database tiendaDB {
    store at "databases/tiendaDB.json";
}

use tiendaDB;

table productos {
    id: int;
    nombre: string;
    precio: float;
    disponible: bool;
}

add productos { id: 1, nombre: "Manzana", precio: 2.5, disponible: true };
add productos { id: 2, nombre: "Pera", precio: 1.8, disponible: false };
add productos { id: 3, nombre: "Naranja", precio: 3.0, disponible: true };

read productos { fields: *; };

read productos { fields: id, nombre; filter: precio >= 2.0 && disponible == true; };

update productos { set: precio = 4.0; filter: id == 3; };

read productos { fields: *; };

export "output/resultado.json";

clear productos;

read productos { fields: *; };
'@

Set-Content -Path 'test.code' -Value $codigoPrueba -Encoding UTF8

# Script Java para test headless
$javaTest = @'
package analizadores;
public class TestCLI {
    public static void main(String[] args) throws Exception {
        String codigo = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("test.code")), java.nio.charset.StandardCharsets.UTF_8);
        Analizar motor = new Analizar();
        motor.analizar(codigo);
        System.out.println("=== TOKENS (" + motor.getTokens().size() + ") ===");
        System.out.println("=== ERRORES LEXICOS: " + motor.getErroresLexicos().size() + " ===");
        for (Lexico.ErrorLexico e : motor.getErroresLexicos()) {
            System.out.println("  LEX ERROR: " + e.descripcion + " L" + e.linea + ":C" + e.columna);
        }
        System.out.println("=== ERRORES SINTACTICOS: " + motor.getErroresSintacticos().size() + " ===");
        for (analizadores.errores.ErrorSintactico e : motor.getErroresSintacticos()) {
            System.out.println("  SYN ERROR: " + e.descripcion + " L" + e.linea + ":C" + e.columna);
        }
        System.out.println("=== ERRORES EJECUCION: " + motor.getErroresEjecucion().size() + " ===");
        for (String[] e : motor.getErroresEjecucion()) {
            System.out.println("  EXEC ERROR: " + e[0] + ": " + e[1] + " L" + e[2]);
        }
        System.out.println("=== CONSOLA ===");
        System.out.println(motor.getSalidaConsola());
    }
}
'@

Set-Content -Path 'src\analizadores\TestCLI.java' -Value $javaTest -Encoding UTF8

# Compilar TestCLI
$LIB = 'lib\java-cup-11b-runtime.jar;lib\java-cup-11b.jar'
$p = Start-Process -FilePath 'javac' `
    -ArgumentList '-J-Dfile.encoding=UTF-8', '-encoding', 'UTF-8', '-cp', "$LIB;build\classes", '-d', 'build\classes', 'src\analizadores\TestCLI.java' `
    -Wait -PassThru -NoNewWindow `
    -RedirectStandardError 'test_err.txt'
if ($p.ExitCode -ne 0) {
    Write-Host "Error compilando TestCLI:"
    Get-Content 'test_err.txt'
    exit 1
}

# Ejecutar test
Write-Host "Ejecutando test..."
$p2 = Start-Process -FilePath 'java' `
    -ArgumentList '-Dfile.encoding=UTF-8', '-cp', "build\classes;$LIB", 'analizadores.TestCLI' `
    -Wait -PassThru -NoNewWindow `
    -RedirectStandardOutput 'test_out.txt' `
    -RedirectStandardError 'test_err2.txt'

Get-Content 'test_out.txt' -ErrorAction SilentlyContinue
Get-Content 'test_err2.txt' -ErrorAction SilentlyContinue
Write-Host "Exit: $($p2.ExitCode)"
