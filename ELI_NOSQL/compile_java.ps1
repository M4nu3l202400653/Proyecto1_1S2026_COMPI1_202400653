Set-Location 'C:\Users\octav\Documents\ELI_NOSQL_Ant'

New-Item -ItemType Directory -Force -Path 'build\classes' | Out-Null

$LIB = 'lib\java-cup-11b-runtime.jar;lib\java-cup-11b.jar'

$files = Get-ChildItem -Path src -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName
Write-Host "Archivos a compilar: $($files.Count)"

$args_list = @('-J-Dfile.encoding=UTF-8', '-cp', $LIB, '--source-path', 'src', '-d', 'build\classes', '-encoding', 'UTF-8') + $files

$p = Start-Process -FilePath 'javac' `
    -ArgumentList $args_list `
    -Wait -PassThru -NoNewWindow `
    -RedirectStandardOutput 'javac_out.txt' `
    -RedirectStandardError 'javac_err.txt'

Write-Host "javac exit: $($p.ExitCode)"
if (Test-Path 'javac_out.txt') { Get-Content 'javac_out.txt' }
if (Test-Path 'javac_err.txt') { Get-Content 'javac_err.txt' }

if ($p.ExitCode -eq 0) {
    Write-Host "`nCOMPILACION EXITOSA" -ForegroundColor Green
} else {
    Write-Host "`nERROR EN COMPILACION" -ForegroundColor Red
}
