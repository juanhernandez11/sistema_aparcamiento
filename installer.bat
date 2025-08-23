@echo off
title Instalador - Sistema de Aparcamiento Pro
color 0A

echo.
echo  ╔══════════════════════════════════════════════════════════╗
echo  ║              SISTEMA DE APARCAMIENTO PRO                 ║
echo  ║                    INSTALADOR v1.0                      ║
echo  ╚══════════════════════════════════════════════════════════╝
echo.

REM Verificar permisos de administrador
net session >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] Ejecutando con permisos de administrador
) else (
    echo [!] Se recomienda ejecutar como administrador para mejor instalacion
)

echo.
echo [1/5] Verificando requisitos del sistema...

REM Verificar Java
java -version >nul 2>&1
if %errorLevel% == 0 (
    echo [✓] Java encontrado
    java -version 2>&1 | findstr "version" | head -1
) else (
    echo [✗] Java no encontrado
    echo.
    echo ATENCION: Java es requerido para ejecutar la aplicacion
    echo Descargue Java desde: https://adoptium.net/
    echo.
    set /p continuar="¿Desea continuar sin Java? (s/n): "
    if /i not "%continuar%"=="s" exit /b 1
)

echo.
echo [2/5] Creando directorio de instalacion...
set INSTALL_DIR=%PROGRAMFILES%\ParkingPro
if not exist "%INSTALL_DIR%" (
    mkdir "%INSTALL_DIR%" 2>nul
    if %errorLevel% == 0 (
        echo [✓] Directorio creado: %INSTALL_DIR%
    ) else (
        set INSTALL_DIR=%USERPROFILE%\ParkingPro
        mkdir "%INSTALL_DIR%" 2>nul
        echo [!] Instalando en directorio de usuario: %INSTALL_DIR%
    )
) else (
    echo [✓] Directorio existe: %INSTALL_DIR%
)

echo.
echo [3/5] Copiando archivos...
if exist "ParkingPro-ConTickets.jar" (
    copy "ParkingPro-ConTickets.jar" "%INSTALL_DIR%\" >nul
    echo [✓] Aplicacion con tickets copiada
) else (
    echo [✗] Archivo JAR no encontrado
    pause
    exit /b 1
)

if exist "config.properties" (
    copy "config.properties" "%INSTALL_DIR%\" >nul
    echo [✓] Configuracion copiada
) else (
    echo total.espacios=20 > "%INSTALL_DIR%\config.properties"
    echo tarifa.hora=2.50 >> "%INSTALL_DIR%\config.properties"
    echo empresa.nombre=ParkingPro System >> "%INSTALL_DIR%\config.properties"
    echo [✓] Configuracion por defecto creada
)

echo.
echo [4/5] Creando accesos directos...

REM Crear script de ejecucion
echo @echo off > "%INSTALL_DIR%\ParkingPro.bat"
echo cd /d "%INSTALL_DIR%" >> "%INSTALL_DIR%\ParkingPro.bat"
echo title ParkingPro System con Tickets v1.1.0 >> "%INSTALL_DIR%\ParkingPro.bat"
echo java -jar ParkingPro-ConTickets.jar >> "%INSTALL_DIR%\ParkingPro.bat"
echo if errorlevel 1 pause >> "%INSTALL_DIR%\ParkingPro.bat"

REM Crear acceso directo en el escritorio
set DESKTOP=%USERPROFILE%\Desktop
echo Set oWS = WScript.CreateObject("WScript.Shell") > "%TEMP%\CreateShortcut.vbs"
echo sLinkFile = "%DESKTOP%\ParkingPro System.lnk" >> "%TEMP%\CreateShortcut.vbs"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%TEMP%\CreateShortcut.vbs"
echo oLink.TargetPath = "%INSTALL_DIR%\ParkingPro.bat" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.WorkingDirectory = "%INSTALL_DIR%" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.Description = "Sistema de Aparcamiento Profesional" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.Save >> "%TEMP%\CreateShortcut.vbs"
cscript "%TEMP%\CreateShortcut.vbs" >nul 2>&1
del "%TEMP%\CreateShortcut.vbs" >nul 2>&1

if exist "%DESKTOP%\ParkingPro System.lnk" (
    echo [✓] Acceso directo creado en el escritorio
) else (
    echo [!] No se pudo crear el acceso directo
)

REM Crear entrada en el menu inicio
set STARTMENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs
if not exist "%STARTMENU%\ParkingPro" mkdir "%STARTMENU%\ParkingPro" 2>nul
echo Set oWS = WScript.CreateObject("WScript.Shell") > "%TEMP%\CreateStartMenu.vbs"
echo sLinkFile = "%STARTMENU%\ParkingPro\ParkingPro System.lnk" >> "%TEMP%\CreateStartMenu.vbs"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%TEMP%\CreateStartMenu.vbs"
echo oLink.TargetPath = "%INSTALL_DIR%\ParkingPro.bat" >> "%TEMP%\CreateStartMenu.vbs"
echo oLink.WorkingDirectory = "%INSTALL_DIR%" >> "%TEMP%\CreateStartMenu.vbs"
echo oLink.Description = "Sistema de Aparcamiento Profesional" >> "%TEMP%\CreateStartMenu.vbs"
echo oLink.Save >> "%TEMP%\CreateStartMenu.vbs"
cscript "%TEMP%\CreateStartMenu.vbs" >nul 2>&1
del "%TEMP%\CreateStartMenu.vbs" >nul 2>&1

echo.
echo [5/5] Finalizando instalacion...

REM Crear desinstalador
echo @echo off > "%INSTALL_DIR%\Desinstalar.bat"
echo title Desinstalador - ParkingPro System >> "%INSTALL_DIR%\Desinstalar.bat"
echo echo Desinstalando ParkingPro System... >> "%INSTALL_DIR%\Desinstalar.bat"
echo del "%DESKTOP%\ParkingPro System.lnk" 2^>nul >> "%INSTALL_DIR%\Desinstalar.bat"
echo rmdir /s /q "%STARTMENU%\ParkingPro" 2^>nul >> "%INSTALL_DIR%\Desinstalar.bat"
echo cd /d "%TEMP%" >> "%INSTALL_DIR%\Desinstalar.bat"
echo rmdir /s /q "%INSTALL_DIR%" >> "%INSTALL_DIR%\Desinstalar.bat"
echo echo Desinstalacion completada. >> "%INSTALL_DIR%\Desinstalar.bat"
echo pause >> "%INSTALL_DIR%\Desinstalar.bat"

echo.
echo  ╔══════════════════════════════════════════════════════════╗
echo  ║                 INSTALACION COMPLETADA                  ║
echo  ╚══════════════════════════════════════════════════════════╝
echo.
echo Ubicacion de instalacion: %INSTALL_DIR%
echo.
echo Para ejecutar la aplicacion:
echo - Haga doble clic en el icono del escritorio
echo - Use el menu Inicio ^> ParkingPro ^> ParkingPro System
echo - Ejecute: %INSTALL_DIR%\ParkingPro.bat
echo.
echo Para desinstalar: %INSTALL_DIR%\Desinstalar.bat
echo.
set /p ejecutar="¿Desea ejecutar la aplicacion ahora? (s/n): "
if /i "%ejecutar%"=="s" (
    start "" "%INSTALL_DIR%\ParkingPro.bat"
)

echo.
echo Gracias por usar ParkingPro System!
pause