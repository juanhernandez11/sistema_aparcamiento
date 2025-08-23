@echo off
echo Verificando Java...
java -version >nul 2>&1
if %errorLevel% == 0 (
    echo [OK] Java esta instalado
    java -version
) else (
    echo [ERROR] Java no encontrado
    echo Descargue Java desde: https://java.com/download
)
pause