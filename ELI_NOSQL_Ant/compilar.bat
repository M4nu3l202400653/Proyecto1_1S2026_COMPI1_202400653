@echo off
setlocal

REM ===== Configuracion de rutas =====
set PROJECT_DIR=%~dp0
set SRC=%PROJECT_DIR%src
set PKG=%SRC%\analizadores
set LIB=%PROJECT_DIR%lib
set BUILD=%PROJECT_DIR%build\classes
set CUP_RT=%LIB%\javacup-runtime.jar
set CUP_TOOL=%LIB%\javacup.jar
set JFLEX=%LIB%\jflex-1.9.1.jar

echo.
echo ====================================
echo  ELI NOSQL - Build completo
echo ====================================

echo.
echo [1/4] Generando Lexer con JFlex...
java -jar "%JFLEX%" -d "%PKG%" "%PKG%\Lexico.jflex"
if errorlevel 1 (
  echo ERROR: JFlex fallo.
  pause
  exit /b 1
)
echo JFlex OK.

echo.
echo [2/4] Generando Parser con CUP...
java -cp "%CUP_TOOL%" java_cup.Main -package analizadores -destdir "%PKG%" -parser Sintactico -symbols sym "%PKG%\Sintactico.cup"
if errorlevel 1 (
  echo ERROR: CUP fallo.
  pause
  exit /b 1
)
echo CUP OK.

echo.
echo [3/4] Creando directorio build...
if not exist "%BUILD%" mkdir "%BUILD%"

echo.
echo [4/4] Compilando fuentes Java...
REM Generar lista de archivos .java en un archivo temporal
dir /s /b "%PKG%\*.java" > "%PROJECT_DIR%sources.txt"
javac -J-Dfile.encoding=UTF-8 -encoding UTF-8 -cp "%CUP_RT%;%CUP_TOOL%" -d "%BUILD%" @"%PROJECT_DIR%sources.txt"
if errorlevel 1 (
  echo ERROR: Compilacion fallo.
  pause
  exit /b 1
)
del "%PROJECT_DIR%sources.txt"
echo Compilacion OK.

echo.
echo ====================================
echo  BUILD EXITOSO - Lanzando GUI...
echo ====================================
echo.

java -cp "%BUILD%;%CUP_RT%;%CUP_TOOL%" analizadores.Analizadores

endlocal
