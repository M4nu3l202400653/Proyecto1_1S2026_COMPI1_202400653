@echo off
setlocal

set PROJECT_DIR=%~dp0
set SRC=%PROJECT_DIR%src
set PKG=%SRC%\analizadores
set LIB=%PROJECT_DIR%lib
set BUILD=%PROJECT_DIR%build\classes
set LOG=%PROJECT_DIR%build_log.txt

echo. > "%LOG%"
echo [BUILD LOG] >> "%LOG%"

echo [1/3] Generando Lexer con JFlex... >> "%LOG%"
java -jar "%LIB%\jflex-1.9.1.jar" -d "%PKG%" "%PKG%\Lexico.jflex" >> "%LOG%" 2>&1
if errorlevel 1 (
  echo ERROR: JFlex fallo. Ver build_log.txt >> "%LOG%"
  type "%LOG%"
  exit /b 1
)
echo JFlex OK >> "%LOG%"

echo [2/3] Generando Parser con CUP... >> "%LOG%"
java -cp "%LIB%\java-cup-11b.jar" java_cup.Main -package analizadores -destdir "%PKG%" -parser Sintactico -symbols sym "%PKG%\Sintactico.cup" >> "%LOG%" 2>&1
if errorlevel 1 (
  echo ERROR: CUP fallo. Ver build_log.txt >> "%LOG%"
  type "%LOG%"
  exit /b 1
)
echo CUP OK >> "%LOG%"

if not exist "%BUILD%" mkdir "%BUILD%"

echo [3/3] Compilando fuentes Java... >> "%LOG%"
javac -J-Dfile.encoding=UTF-8 --source-path "%SRC%" -cp "%LIB%\java-cup-11b-runtime.jar;%LIB%\java-cup-11b.jar" -d "%BUILD%" -encoding UTF-8 "%PKG%\*.java" "%PKG%\ast\*.java" "%PKG%\instrucciones\*.java" "%PKG%\expresiones\*.java" "%PKG%\data\*.java" "%PKG%\tablas\*.java" "%PKG%\errores\*.java" "%PKG%\persistencia\*.java" >> "%LOG%" 2>&1
if errorlevel 1 (
  echo ERROR: Compilacion fallo. Ver build_log.txt >> "%LOG%"
  type "%LOG%"
  exit /b 1
)
echo COMPILACION OK >> "%LOG%"

type "%LOG%"
echo.
echo Compilacion exitosa. Para ejecutar:
echo   java -cp build\classes;lib\java-cup-11b-runtime.jar analizadores.Analizadores

endlocal
