@echo off
echo Configurando repositorio Git...
git init
git add .
git commit -m "Initial commit - Bitcoin Script Interpreter"
git branch -M main
git remote add origin https://github.com/PJOS17/Proyecto1.git
echo.
echo Intentando subir al repositorio remoto...
git push -u origin main
echo.
pause
