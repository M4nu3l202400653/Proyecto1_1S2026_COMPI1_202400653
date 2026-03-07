Set-Location 'C:\Users\octav\Documents\ELI_NOSQL_Ant'
$LIB = 'lib\javacup-runtime.jar;lib\javacup.jar'

# Regenerar CUP
$p1 = Start-Process -FilePath 'java' `
    -ArgumentList '-cp','lib\javacup.jar','java_cup.Main','-package','analizadores','-destdir','src\analizadores','-parser','Sintactico','-symbols','sym','src\analizadores\Sintactico.cup' `
    -Wait -PassThru -NoNewWindow -RedirectStandardError 'cup_err.txt'
if ($p1.ExitCode -ne 0) { Write-Host 'CUP FALLO:'; Get-Content 'cup_err.txt'; exit 1 }
Write-Host 'CUP OK'

# Recompilar
$files = Get-ChildItem -Path src -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName
$p2 = Start-Process -FilePath 'javac' `
    -ArgumentList (@('-J-Dfile.encoding=UTF-8','-encoding','UTF-8','-cp',$LIB,'--source-path','src','-d','build\classes') + $files) `
    -Wait -PassThru -NoNewWindow -RedirectStandardError 'compile_err.txt'
if ($p2.ExitCode -ne 0) { Write-Host 'COMPILE FALLO:'; Get-Content 'compile_err.txt'; exit 1 }
Write-Host 'COMPILE OK'

# Ejecutar test de errores
$p3 = Start-Process -FilePath 'java' `
    -ArgumentList '-cp',"build\classes;$LIB",'analizadores.TestCLI','test_errors.code' `
    -Wait -PassThru -NoNewWindow `
    -RedirectStandardOutput 'errors_out.txt' -RedirectStandardError 'errors_err.txt'
Get-Content 'errors_out.txt' -ErrorAction SilentlyContinue
if ($p3.ExitCode -ne 0) { Get-Content 'errors_err.txt' -ErrorAction SilentlyContinue }
