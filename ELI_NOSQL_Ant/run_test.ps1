Set-Location 'C:\Users\octav\Documents\ELI_NOSQL_Ant'
$LIB = 'lib\java-cup-11b-runtime.jar;lib\java-cup-11b.jar'

# Recompilar todo incluyendo TestCLI
$files = Get-ChildItem -Path src -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName
$p1 = Start-Process -FilePath 'javac' `
    -ArgumentList (@('-J-Dfile.encoding=UTF-8','-encoding','UTF-8','-cp',$LIB,'--source-path','src','-d','build\classes') + $files) `
    -Wait -PassThru -NoNewWindow `
    -RedirectStandardError 'compile_err.txt'
if ($p1.ExitCode -ne 0) {
    Write-Host "ERROR compilando:"
    Get-Content 'compile_err.txt'
    exit 1
}
Write-Host "Compilacion OK"

# Escribir test.code (sin comillas problemáticas)
$code = "database tiendaDB {}`r`n`r`nuse tiendaDB;`r`n`r`ntable productos {`r`n    id: int;`r`n    nombre: string;`r`n    precio: float;`r`n}`r`n`r`nadd productos { id: 1, nombre: `"Manzana`", precio: 2.5 };`r`nadd productos { id: 2, nombre: `"Pera`", precio: 1.8 };`r`nadd productos { id: 3, nombre: `"Naranja`", precio: 3.0 };`r`n`r`nread productos { fields: *; };`r`n`r`nread productos { fields: id, nombre; filter: precio >= 2.0; };`r`n`r`nupdate productos { set: precio = 4.0; filter: id == 3; };`r`n`r`nclear productos;`r`n`r`nread productos { fields: *; };`r`n"
[System.IO.File]::WriteAllText((Join-Path (Get-Location) 'test.code'), $code, (New-Object System.Text.UTF8Encoding $false))
Write-Host "test.code creado"

# Ejecutar test
$p2 = Start-Process -FilePath 'java' `
    -ArgumentList '-Dfile.encoding=UTF-8','-cp',"build\classes;$LIB",'analizadores.TestCLI' `
    -Wait -PassThru -NoNewWindow `
    -RedirectStandardOutput 'test_out.txt' `
    -RedirectStandardError 'test_err.txt'

Write-Host "Exit: $($p2.ExitCode)"
Write-Host "=== OUTPUT ==="
Get-Content 'test_out.txt' -ErrorAction SilentlyContinue
if ($p2.ExitCode -ne 0) {
    Write-Host "=== STDERR ==="
    Get-Content 'test_err.txt' -ErrorAction SilentlyContinue
}
