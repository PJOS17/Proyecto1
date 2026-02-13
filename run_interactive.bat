@echo off
echo Compiling...
call mvn compile > nul
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
)
echo.
echo Starting Interactive Bitcoin Script Interpreter...
echo.
java -cp "target\classes" com.bitcoin.Main
pause
