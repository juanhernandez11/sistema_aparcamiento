@echo off
echo Creando paquete de distribucion...
echo.

REM Compilar el codigo con tickets
echo [1/4] Compilando aplicacion con sistema de tickets...
javac SistemaAparcamientoPro.java
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)

REM Crear JAR
echo [2/4] Creando archivo JAR...
jar cfe ParkingPro-ConTickets.jar SistemaAparcamientoPro *.class

REM Crear directorio de distribucion
echo [3/4] Creando paquete...
if exist ParkingPro-Distribucion rmdir /s /q ParkingPro-Distribucion
mkdir ParkingPro-Distribucion

REM Copiar archivos necesarios
copy ParkingPro-ConTickets.jar ParkingPro-Distribucion\
copy installer.bat ParkingPro-Distribucion\
copy verificar_java.bat ParkingPro-Distribucion\
copy MANUAL_USUARIO.md ParkingPro-Distribucion\
copy MANUAL_TICKETS.md ParkingPro-Distribucion\

REM Crear configuracion por defecto
echo total.espacios=20 > ParkingPro-Distribucion\config.properties
echo tarifa.hora=2.50 >> ParkingPro-Distribucion\config.properties
echo empresa.nombre=ParkingPro System >> ParkingPro-Distribucion\config.properties
echo empresa.direccion=Av. Principal 123 >> ParkingPro-Distribucion\config.properties
echo empresa.telefono=(555) 123-4567 >> ParkingPro-Distribucion\config.properties

REM Crear script de ejecucion simple
echo @echo off > ParkingPro-Distribucion\Ejecutar.bat
echo title ParkingPro System con Tickets v1.1.0 >> ParkingPro-Distribucion\Ejecutar.bat
echo echo Iniciando ParkingPro System con Tickets... >> ParkingPro-Distribucion\Ejecutar.bat
echo java -jar ParkingPro-ConTickets.jar >> ParkingPro-Distribucion\Ejecutar.bat
echo if errorlevel 1 pause >> ParkingPro-Distribucion\Ejecutar.bat

REM Crear README
echo PARKINGPRO SYSTEM CON TICKETS v1.1.0 > ParkingPro-Distribucion\LEEME.txt
echo ===================================== >> ParkingPro-Distribucion\LEEME.txt
echo. >> ParkingPro-Distribucion\LEEME.txt
echo NUEVAS FUNCIONALIDADES: >> ParkingPro-Distribucion\LEEME.txt
echo - Sistema completo de tickets >> ParkingPro-Distribucion\LEEME.txt
echo - Impresion de tickets fisicos >> ParkingPro-Distribucion\LEEME.txt
echo - Lectura de codigos de ticket >> ParkingPro-Distribucion\LEEME.txt
echo - Calculo automatico de tarifas >> ParkingPro-Distribucion\LEEME.txt
echo - Control total de entradas/salidas >> ParkingPro-Distribucion\LEEME.txt
echo. >> ParkingPro-Distribucion\LEEME.txt
echo INSTRUCCIONES DE USO: >> ParkingPro-Distribucion\LEEME.txt
echo 1. Asegurese de tener Java instalado >> ParkingPro-Distribucion\LEEME.txt
echo 2. Conecte una impresora (para tickets) >> ParkingPro-Distribucion\LEEME.txt
echo 3. Haga doble clic en Ejecutar.bat >> ParkingPro-Distribucion\LEEME.txt
echo 4. O ejecute: java -jar ParkingPro-ConTickets.jar >> ParkingPro-Distribucion\LEEME.txt
echo. >> ParkingPro-Distribucion\LEEME.txt
echo INSTALACION COMPLETA: >> ParkingPro-Distribucion\LEEME.txt
echo - Ejecute installer.bat como administrador >> ParkingPro-Distribucion\LEEME.txt
echo. >> ParkingPro-Distribucion\LEEME.txt
echo REQUISITOS: >> ParkingPro-Distribucion\LEEME.txt
echo - Java 8 o superior >> ParkingPro-Distribucion\LEEME.txt
echo - Windows 7/8/10/11 >> ParkingPro-Distribucion\LEEME.txt
echo - Impresora (recomendada para tickets) >> ParkingPro-Distribucion\LEEME.txt

echo [4/4] Limpiando archivos temporales...
del *.class 2>nul

echo.
echo ========================================
echo    PAQUETE CREADO EXITOSAMENTE
echo ========================================
echo.
echo Carpeta: ParkingPro-Distribucion\
echo.
echo CONTENIDO:
dir ParkingPro-Distribucion
echo.
echo PARA VENDER:
echo 1. Comprima la carpeta 'ParkingPro-Distribucion' en ZIP
echo 2. Envie el ZIP al cliente
echo 3. El cliente solo necesita descomprimir y ejecutar
echo.
pause