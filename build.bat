@echo off
echo ========================================
echo    SISTEMA DE APARCAMIENTO PRO
echo    Script de Construccion y Despliegue
echo ========================================
echo.

REM Verificar si Maven esta instalado
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven no esta instalado o no esta en el PATH
    echo Descargue Maven desde: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Verificar si Java esta instalado
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java no esta instalado o no esta en el PATH
    echo Descargue Java desde: https://adoptium.net/
    pause
    exit /b 1
)

echo [1/4] Limpiando proyecto anterior...
if exist target rmdir /s /q target
if exist dist rmdir /s /q dist

echo [2/4] Compilando proyecto con Maven...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)

echo [3/4] Creando JAR ejecutable...
call mvn package
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo la creacion del JAR
    pause
    exit /b 1
)

echo [4/4] Creando paquete de distribucion...
mkdir dist 2>nul
copy target\SistemaAparcamientoPro-1.0.0.jar dist\
if exist target\SistemaAparcamientoPro.exe copy target\SistemaAparcamientoPro.exe dist\

REM Crear archivos de configuracion por defecto
echo total.espacios=20 > dist\config.properties
echo tarifa.hora=2.50 >> dist\config.properties
echo empresa.nombre=ParkingPro System >> dist\config.properties

REM Crear script de ejecucion
echo @echo off > dist\ejecutar.bat
echo echo Iniciando Sistema de Aparcamiento Pro... >> dist\ejecutar.bat
echo java -jar SistemaAparcamientoPro-1.0.0.jar >> dist\ejecutar.bat
echo pause >> dist\ejecutar.bat

REM Crear README
echo SISTEMA DE APARCAMIENTO PRO v1.0.0 > dist\README.txt
echo ================================== >> dist\README.txt
echo. >> dist\README.txt
echo INSTALACION: >> dist\README.txt
echo 1. Asegurese de tener Java 11 o superior instalado >> dist\README.txt
echo 2. Ejecute 'ejecutar.bat' o haga doble clic en SistemaAparcamientoPro.exe >> dist\README.txt
echo. >> dist\README.txt
echo ARCHIVOS: >> dist\README.txt
echo - SistemaAparcamientoPro-1.0.0.jar: Aplicacion principal >> dist\README.txt
echo - SistemaAparcamientoPro.exe: Ejecutable para Windows (si disponible) >> dist\README.txt
echo - config.properties: Archivo de configuracion >> dist\README.txt
echo - ejecutar.bat: Script de ejecucion >> dist\README.txt
echo. >> dist\README.txt
echo SOPORTE: >> dist\README.txt
echo Para soporte tecnico, contacte al administrador del sistema. >> dist\README.txt

echo.
echo ========================================
echo    CONSTRUCCION COMPLETADA
echo ========================================
echo.
echo Archivos generados en la carpeta 'dist':
dir dist
echo.
echo Para distribuir el sistema:
echo 1. Comprima la carpeta 'dist' en un archivo ZIP
echo 2. Distribuya el archivo ZIP a los usuarios finales
echo 3. Los usuarios deben descomprimir y ejecutar 'ejecutar.bat'
echo.
pause