@echo off
echo Compilando...
if not exist "target\classes" mkdir "target\classes"
javac -source 1.8 -target 1.8 -d target\classes src\main\java\com\bitcoin\*.java 2>&1
if %errorlevel% neq 0 (
    echo ERROR en compilación.
    pause
    exit /b 1
)
echo.
echo === Demostración P2PKH (Pay-to-Public-Key-Hash) ===
echo.
java -cp "target\classes" com.bitcoin.Main --p2pkh --trace
echo.
pause
