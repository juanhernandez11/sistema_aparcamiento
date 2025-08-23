# Sistema de Aparcamiento Pro v1.0.0
## Manual de Usuario

### 📋 Descripción
Sistema profesional de gestión de aparcamientos con interfaz gráfica, persistencia de datos y funcionalidades avanzadas para entornos comerciales.

### 🚀 Características Principales
- ✅ Gestión de hasta 20 espacios de aparcamiento (configurable)
- ✅ Registro de entrada y salida de vehículos
- ✅ Cálculo automático de tarifas por tiempo
- ✅ Búsqueda de vehículos por placa
- ✅ Reportes y estadísticas en tiempo real
- ✅ Exportación de datos
- ✅ Configuración personalizable
- ✅ Persistencia de datos automática
- ✅ Interfaz profesional y fácil de usar

### 💻 Requisitos del Sistema
- **Sistema Operativo:** Windows 7/8/10/11
- **Java:** Versión 11 o superior
- **RAM:** Mínimo 512 MB
- **Espacio en disco:** 50 MB

### 📦 Instalación

#### Opción 1: Instalación Automática
1. Descargar el paquete de instalación
2. Ejecutar `installer.bat` como administrador
3. Seguir las instrucciones en pantalla
4. La aplicación se instalará automáticamente

#### Opción 2: Instalación Manual
1. Descomprimir el archivo ZIP en una carpeta
2. Ejecutar `ejecutar.bat` o hacer doble clic en el archivo JAR
3. La aplicación iniciará inmediatamente

### 🎯 Guía de Uso

#### Pantalla Principal
La interfaz está dividida en cuatro secciones:
- **Panel de información:** Muestra estadísticas en tiempo real
- **Panel de controles:** Campos para ingresar datos y botones de acción
- **Área de estado:** Visualización del estado de todos los espacios
- **Panel de administración:** Botones para funciones avanzadas

#### Registrar Entrada de Vehículo
1. Ingresar la **placa del vehículo** en el campo correspondiente
2. Ingresar el **número de espacio** (E1-E20) o dejarlo vacío para asignación automática
3. Hacer clic en **"Entrada"**
4. El sistema confirmará el registro y actualizará el estado

#### Registrar Salida de Vehículo
1. Ingresar el **número de espacio** del vehículo que sale
2. Alternativamente, ingresar la **placa** para búsqueda automática del espacio
3. Hacer clic en **"Salida"**
4. El sistema mostrará el tiempo de permanencia y la tarifa calculada

#### Buscar Vehículo
1. Ingresar la **placa del vehículo** a buscar
2. Hacer clic en **"Buscar"**
3. El sistema mostrará la ubicación y hora de entrada

#### Generar Reportes
1. Hacer clic en **"Reporte"**
2. Se mostrará una ventana con:
   - Estadísticas de ocupación
   - Ingresos estimados
   - Información detallada del estado actual

### ⚙️ Configuración

#### Acceder a la Configuración
1. Hacer clic en **"Configuración"** en el panel inferior
2. Modificar los valores deseados:
   - **Total espacios:** Número de espacios disponibles (1-100)
   - **Tarifa por hora:** Precio por hora de estacionamiento
   - **Nombre empresa:** Nombre que aparece en el título

#### Archivo de Configuración
El archivo `config.properties` contiene:
```properties
total.espacios=20
tarifa.hora=2.50
empresa.nombre=ParkingPro System
```

### 💾 Gestión de Datos

#### Persistencia Automática
- Los datos se guardan automáticamente en `aparcamiento_data.txt`
- La información persiste entre sesiones
- No se requiere acción manual para guardar

#### Exportar Datos
1. Hacer clic en **"Exportar"**
2. Se generará un archivo `reporte_YYYYMMDD_HHMM.txt`
3. El archivo contiene el estado completo del aparcamiento

#### Respaldo de Datos
Se recomienda hacer copias de seguridad periódicas de:
- `aparcamiento_data.txt` (datos actuales)
- `config.properties` (configuración)

### 🔧 Solución de Problemas

#### La aplicación no inicia
- Verificar que Java 11+ esté instalado
- Ejecutar `java -version` en línea de comandos
- Reinstalar Java si es necesario

#### Error "Espacio no válido"
- Verificar que el espacio esté en el rango E1-E20 (o el configurado)
- Usar formato correcto: E1, E2, E3, etc.

#### Datos perdidos
- Verificar que el archivo `aparcamiento_data.txt` exista
- Restaurar desde respaldo si está disponible
- Los datos se guardan automáticamente al cerrar

#### Problemas de permisos
- Ejecutar como administrador
- Verificar permisos de escritura en la carpeta de instalación

### 📞 Soporte Técnico
Para asistencia, contactar a soporte.

#### Archivos de Log
En caso de errores, revisar:
- Mensajes de error en pantalla
- Archivos generados en la carpeta de instalación

#### Información del Sistema
Para soporte, proporcionar:
- Versión del sistema operativo
- Versión de Java instalada
- Descripción detallada del problema
- Pasos para reproducir el error

### 🔄 Actualizaciones
1. Descargar la nueva versión
2. Hacer respaldo de datos actuales
3. Ejecutar el nuevo instalador
4. Los datos se preservarán automáticamente

### 📄 Licencia
Este software es de uso comercial. Todos los derechos reservados.

### 👨‍💻 Developer By
- Nombre: JuanBv

### 📝 Notas Finales
- Mantener Java actualizado para mejor rendimiento
- Realizar respaldos periódicos de los datos
- Contactar soporte para cualquier duda o problema

---
**ParkingPro System v1.0.0**  
*Sistema Profesional de Gestión de Aparcamientos*