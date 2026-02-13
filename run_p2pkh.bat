@echo off
echo Compiling...
call mvn compile > nul
echo.
echo Running P2PKH (Pay to Public Key Hash) Demonstration...
echo.
java -cp "target\classes" com.bitcoin.Main --p2pkh --trace
echo.
pause
