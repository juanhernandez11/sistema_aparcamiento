@echo off
title ParkingPro System - Despliegue Completo
color 0B

echo.
echo  ╔══════════════════════════════════════════════════════════╗
echo  ║            PARKINGPRO SYSTEM - DESPLIEGUE               ║
echo  ║                  Generador de Paquetes                  ║
echo  ╚══════════════════════════════════════════════════════════╝
echo.

set VERSION=1.0.0
set PACKAGE_NAME=ParkingPro-System-v%VERSION%

echo [INFO] Iniciando proceso de despliegue...
echo [INFO] Version: %VERSION%
echo [INFO] Paquete: %PACKAGE_NAME%
echo.

REM Verificar archivos necesarios
echo [1/8] Verificando archivos fuente...
if not exist "SistemaAparcamientoPro.java" (
    echo [ERROR] Archivo fuente no encontrado: SistemaAparcamientoPro.java
    pause
    exit /b 1
)
echo [✓] Archivos fuente verificados

REM Limpiar directorios anteriores
echo.
echo [2/8] Limpiando directorios anteriores...
if exist "target" rmdir /s /q "target"
if exist "dist" rmdir /s /q "dist"
if exist "%PACKAGE_NAME%" rmdir /s /q "%PACKAGE_NAME%"
echo [✓] Directorios limpiados

REM Compilar con Maven si está disponible
echo.
echo [3/8] Compilando aplicacion...
where mvn >nul 2>nul
if %ERRORLEVEL% == 0 (
    echo [INFO] Usando Maven para compilacion...
    call mvn clean package -q
    if %ERRORLEVEL% == 0 (
        echo [✓] Compilacion con Maven exitosa
        set COMPILED_WITH_MAVEN=true
    ) else (
        echo [WARN] Fallo Maven, usando javac...
        set COMPILED_WITH_MAVEN=false
    )
) else (
    echo [INFO] Maven no disponible, usando javac...
    set COMPILED_WITH_MAVEN=false
)

REM Compilacion manual si Maven fallo o no esta disponible
if "%COMPILED_WITH_MAVEN%"=="false" (
    javac -cp . SistemaAparcamientoPro.java
    if %ERRORLEVEL% == 0 (
        jar cfe SistemaAparcamientoPro-%VERSION%.jar SistemaAparcamientoPro *.class
        echo [✓] Compilacion manual exitosa
    ) else (
        echo [ERROR] Fallo la compilacion
        pause
        exit /b 1
    )
)

REM Crear estructura de directorios
echo.
echo [4/8] Creando estructura de paquete...
mkdir "%PACKAGE_NAME%" 2>nul
mkdir "%PACKAGE_NAME%\bin" 2>nul
mkdir "%PACKAGE_NAME%\config" 2>nul
mkdir "%PACKAGE_NAME%\docs" 2>nul
mkdir "%PACKAGE_NAME%\install" 2>nul
echo [✓] Estructura creada

REM Copiar archivos compilados
echo.
echo [5/8] Copiando archivos de aplicacion...
if "%COMPILED_WITH_MAVEN%"=="true" (
    if exist "target\SistemaAparcamientoPro-%VERSION%.jar" (
        copy "target\SistemaAparcamientoPro-%VERSION%.jar" "%PACKAGE_NAME%\bin\" >nul
        echo [✓] JAR de Maven copiado
    )
    if exist "target\SistemaAparcamientoPro.exe" (
        copy "target\SistemaAparcamientoPro.exe" "%PACKAGE_NAME%\bin\" >nul
        echo [✓] Ejecutable de Windows copiado
    )
) else (
    copy "SistemaAparcamientoPro-%VERSION%.jar" "%PACKAGE_NAME%\bin\" >nul
    echo [✓] JAR manual copiado
)

REM Crear archivos de configuracion
echo.
echo [6/8] Generando archivos de configuracion...
echo # Configuracion del Sistema de Aparcamiento Pro > "%PACKAGE_NAME%\config\config.properties"
echo # Generado automaticamente el %date% %time% >> "%PACKAGE_NAME%\config\config.properties"
echo. >> "%PACKAGE_NAME%\config\config.properties"
echo total.espacios=20 >> "%PACKAGE_NAME%\config\config.properties"
echo tarifa.hora=2.50 >> "%PACKAGE_NAME%\config\config.properties"
echo empresa.nombre=ParkingPro System >> "%PACKAGE_NAME%\config\config.properties"
echo moneda=USD >> "%PACKAGE_NAME%\config\config.properties"
echo idioma=es >> "%PACKAGE_NAME%\config\config.properties"
echo [✓] Configuracion por defecto creada

REM Crear scripts de ejecucion
echo @echo off > "%PACKAGE_NAME%\ParkingPro.bat"
echo title ParkingPro System v%VERSION% >> "%PACKAGE_NAME%\ParkingPro.bat"
echo cd /d "%%~dp0" >> "%PACKAGE_NAME%\ParkingPro.bat"
echo if exist config\config.properties copy config\config.properties . ^>nul >> "%PACKAGE_NAME%\ParkingPro.bat"
echo echo Iniciando ParkingPro System... >> "%PACKAGE_NAME%\ParkingPro.bat"
echo java -jar bin\SistemaAparcamientoPro-%VERSION%.jar >> "%PACKAGE_NAME%\ParkingPro.bat"
echo if errorlevel 1 ( >> "%PACKAGE_NAME%\ParkingPro.bat"
echo     echo. >> "%PACKAGE_NAME%\ParkingPro.bat"
echo     echo ERROR: No se pudo iniciar la aplicacion >> "%PACKAGE_NAME%\ParkingPro.bat"
echo     echo Verifique que Java 11+ este instalado >> "%PACKAGE_NAME%\ParkingPro.bat"
echo     pause >> "%PACKAGE_NAME%\ParkingPro.bat"
echo ^) >> "%PACKAGE_NAME%\ParkingPro.bat"

REM Script de verificacion de sistema
echo @echo off > "%PACKAGE_NAME%\VerificarSistema.bat"
echo title Verificacion de Sistema - ParkingPro >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo echo Verificando requisitos del sistema... >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo echo. >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo java -version 2^>^&1 ^| findstr "version" >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo if errorlevel 1 ( >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo     echo [ERROR] Java no esta instalado >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo     echo Descargue Java desde: https://adoptium.net/ >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo ^) else ( >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo     echo [OK] Java esta instalado >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo ^) >> "%PACKAGE_NAME%\VerificarSistema.bat"
echo pause >> "%PACKAGE_NAME%\VerificarSistema.bat"

echo [✓] Scripts de ejecucion creados

REM Copiar documentacion
echo.
echo [7/8] Copiando documentacion...
if exist "MANUAL_USUARIO.md" copy "MANUAL_USUARIO.md" "%PACKAGE_NAME%\docs\" >nul
copy "installer.bat" "%PACKAGE_NAME%\install\" >nul

REM Crear README principal
echo PARKINGPRO SYSTEM v%VERSION% > "%PACKAGE_NAME%\README.txt"
echo ========================== >> "%PACKAGE_NAME%\README.txt"
echo. >> "%PACKAGE_NAME%\README.txt"
echo Sistema profesional de gestion de aparcamientos >> "%PACKAGE_NAME%\README.txt"
echo Desarrollado con Java y tecnologias modernas >> "%PACKAGE_NAME%\README.txt"
echo. >> "%PACKAGE_NAME%\README.txt"
echo INICIO RAPIDO: >> "%PACKAGE_NAME%\README.txt"
echo 1. Ejecute VerificarSistema.bat para verificar Java >> "%PACKAGE_NAME%\README.txt"
echo 2. Ejecute ParkingPro.bat para iniciar la aplicacion >> "%PACKAGE_NAME%\README.txt"
echo 3. Para instalacion completa, use install\installer.bat >> "%PACKAGE_NAME%\README.txt"
echo. >> "%PACKAGE_NAME%\README.txt"
echo ESTRUCTURA DE ARCHIVOS: >> "%PACKAGE_NAME%\README.txt"
echo - bin\: Archivos ejecutables >> "%PACKAGE_NAME%\README.txt"
echo - config\: Archivos de configuracion >> "%PACKAGE_NAME%\README.txt"
echo - docs\: Documentacion completa >> "%PACKAGE_NAME%\README.txt"
echo - install\: Instalador automatico >> "%PACKAGE_NAME%\README.txt"
echo. >> "%PACKAGE_NAME%\README.txt"
echo SOPORTE: >> "%PACKAGE_NAME%\README.txt"
echo Consulte docs\MANUAL_USUARIO.md para instrucciones detalladas >> "%PACKAGE_NAME%\README.txt"
echo. >> "%PACKAGE_NAME%\README.txt"
echo Generado el %date% %time% >> "%PACKAGE_NAME%\README.txt"

echo [✓] Documentacion copiada

REM Crear paquete ZIP
echo.
echo [8/8] Creando paquete de distribucion...
where powershell >nul 2>nul
if %ERRORLEVEL% == 0 (
    powershell -command "Compress-Archive -Path '%PACKAGE_NAME%' -DestinationPath '%PACKAGE_NAME%.zip' -Force"
    if exist "%PACKAGE_NAME%.zip" (
        echo [✓] Paquete ZIP creado: %PACKAGE_NAME%.zip
    ) else (
        echo [WARN] No se pudo crear el ZIP automaticamente
    )
) else (
    echo [WARN] PowerShell no disponible, ZIP no creado automaticamente
)

REM Mostrar resumen
echo.
echo  ╔══════════════════════════════════════════════════════════╗
echo  ║                DESPLIEGUE COMPLETADO                    ║
echo  ╚══════════════════════════════════════════════════════════╝
echo.
echo Paquete generado: %PACKAGE_NAME%\
if exist "%PACKAGE_NAME%.zip" echo Archivo ZIP: %PACKAGE_NAME%.zip
echo.
echo CONTENIDO DEL PAQUETE:
dir "%PACKAGE_NAME%" /b
echo.
echo INSTRUCCIONES DE DISTRIBUCION:
echo 1. Distribuya la carpeta '%PACKAGE_NAME%' o el archivo ZIP
echo 2. Los usuarios deben ejecutar 'VerificarSistema.bat' primero
echo 3. Luego ejecutar 'ParkingPro.bat' para usar la aplicacion
echo 4. Para instalacion permanente: 'install\installer.bat'
echo.
echo PRUEBA LOCAL:
set /p test="¿Desea probar la aplicacion ahora? (s/n): "
if /i "%test%"=="s" (
    cd "%PACKAGE_NAME%"
    call ParkingPro.bat
)

echo.
echo Despliegue completado exitosamente!
pause