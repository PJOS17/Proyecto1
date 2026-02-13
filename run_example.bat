@echo off
echo Compiling...
call mvn compile
echo.
echo Running Example Script: 1 2 OP_ADD 5 OP_GREATERTHAN
echo.
java -cp "target\classes" com.bitcoin.Main --trace "1 2 OP_ADD 5 OP_GREATERTHAN"
echo.
pause
