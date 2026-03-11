Set-Location 'C:\Users\octav\Documents\ELI_NOSQL_Ant'
$LIB = 'lib\javacup-runtime.jar;lib\javacup.jar'
$p = Start-Process -FilePath 'java' `
    -ArgumentList '-cp',"build\classes;$LIB",'analizadores.TestCLI','test_errors.code' `
    -Wait -PassThru -NoNewWindow `
    -RedirectStandardOutput 'errors_out.txt' -RedirectStandardError 'errors_err.txt'
Get-Content 'errors_out.txt' -ErrorAction SilentlyContinue
if ($p.ExitCode -ne 0) { Get-Content 'errors_err.txt' -ErrorAction SilentlyContinue }
