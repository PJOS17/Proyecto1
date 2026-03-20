@echo off
echo ========================================
echo  Compilando Bitcoin Script Interpreter
echo ========================================
if not exist "target\classes" mkdir "target\classes"
javac -source 1.8 -target 1.8 -d target\classes src\main\java\com\bitcoin\*.java
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Fallo en la compilación.
    pause
    exit /b 1
)
echo.
echo Compilación exitosa.
echo Para ejecutar: java -cp "target\classes" com.bitcoin.Main
echo.
pause
