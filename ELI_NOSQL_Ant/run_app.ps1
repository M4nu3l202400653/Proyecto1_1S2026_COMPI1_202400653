Set-Location 'C:\Users\octav\Documents\ELI_NOSQL_Ant'

$CP = 'build\classes;lib\java-cup-11b-runtime.jar;lib\java-cup-11b.jar'

Start-Process -FilePath 'java' `
    -ArgumentList '-cp', $CP, 'analizadores.Analizadores' `
    -NoNewWindow
