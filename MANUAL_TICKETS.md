# ParkingPro System con Tickets v1.1.0
## Manual de Usuario - Sistema de Tickets

### 🎫 **NUEVAS FUNCIONALIDADES**

#### **Sistema de Tickets Completo**
- ✅ Generación automática de códigos únicos
- ✅ Impresión de tickets físicos
- ✅ Lectura y validación de tickets
- ✅ Cálculo automático de tarifas
- ✅ Control total de entradas y salidas

### 🚗 **FLUJO DE TRABAJO**

#### **ENTRADA DE VEHÍCULO**
1. **Ingresar placa** del vehículo
2. **Asignar espacio** (automático o manual)
3. **Sistema genera** código único (formato: TK-YYYYMMDD-NNNN)
4. **¿Imprimir ticket?** → Confirmar SÍ/NO
5. **Entregar ticket** físico al cliente

#### **SALIDA DE VEHÍCULO**
1. **Leer código** del ticket del cliente
2. **Sistema calcula** tiempo transcurrido y tarifa
3. **Mostrar total** a cobrar
4. **Confirmar pago** → Liberar espacio automáticamente

### 🎯 **INTERFAZ ACTUALIZADA**

#### **Campos Nuevos:**
- **Código Ticket:** Para ingresar/buscar tickets
- **Leer Ticket:** Botón para procesar tickets
- **Reimprimir:** Para duplicar tickets perdidos

#### **Botones Principales:**
- **Entrada:** Registra vehículo y genera ticket
- **Salida:** Procesa salida por código de ticket
- **Buscar:** Localiza vehículo por placa
- **Leer Ticket:** Muestra información del ticket
- **Reimprimir:** Duplica ticket existente

### 🖨️ **SISTEMA DE IMPRESIÓN**

#### **Formato del Ticket:**
```
========================================
       PARKINGPRO SYSTEM
========================================
Dirección: Av. Principal 123
Teléfono: (555) 123-4567
----------------------------------------
TICKET DE ENTRADA
----------------------------------------
Código: TK-20241201-1234
Placa: ABC-123
Espacio: E5
Fecha: 01/12/2024
Hora: 14:30:25
Tarifa: $2.50 por hora
----------------------------------------
CONSERVE ESTE TICKET
Necesario para la salida
----------------------------------------
Código QR: [TK-20241201-1234]
========================================
```

#### **Configuración de Impresora:**
- Compatible con cualquier impresora Windows
- Formato optimizado para tickets pequeños
- Opción de reimprimir tickets perdidos

### 💰 **CÁLCULO DE TARIFAS**

#### **Sistema de Cobro:**
- **Mínimo:** 1 hora (aunque sea 1 minuto)
- **Redondeo:** Hacia arriba por hora iniciada
- **Ejemplo:** 2 horas 15 minutos = 3 horas

#### **Información de Cobro:**
```
INFORMACIÓN DE COBRO
==================
Placa: ABC-123
Espacio: E5
Código: TK-20241201-1234
Entrada: 01/12/2024 14:30
Salida: 01/12/2024 17:45
Tiempo: 4 horas (195 minutos)
Tarifa: $2.50 por hora
TOTAL A COBRAR: $10.00
```

### 🔍 **BÚSQUEDA Y VALIDACIÓN**

#### **Buscar por Código:**
1. Ingresar código en campo "Código Ticket"
2. Clic en "Leer Ticket"
3. Sistema muestra toda la información
4. Calcula tarifa actual en tiempo real

#### **Buscar por Placa:**
1. Ingresar placa en campo "Placa"
2. Clic en "Buscar"
3. Sistema completa automáticamente espacio y código

### 📊 **REPORTES MEJORADOS**

#### **Información Adicional:**
- Total de tickets activos
- Ingresos estimados por tickets pendientes
- Tiempo promedio de estacionamiento
- Estadísticas de ocupación

### ⚙️ **CONFIGURACIÓN EXTENDIDA**

#### **Nuevos Parámetros:**
```properties
empresa.direccion=Av. Principal 123
empresa.telefono=(555) 123-4567
```

#### **Personalización de Tickets:**
- Nombre de la empresa
- Dirección y teléfono
- Tarifa por hora
- Número de espacios

### 🛠️ **ARCHIVOS DEL SISTEMA**

#### **Archivos de Datos:**
- `aparcamiento_data.txt` - Estado de espacios
- `tickets_activos.txt` - Tickets pendientes
- `config.properties` - Configuración del sistema

#### **Respaldo Automático:**
- Los datos se guardan automáticamente
- Persistencia entre sesiones
- Recuperación automática al reiniciar

### 🚨 **CASOS ESPECIALES**

#### **Ticket Perdido:**
1. Buscar por placa del vehículo
2. Usar "Reimprimir" para generar duplicado
3. Procesar salida normalmente

#### **Error en Impresión:**
1. Usar "Reimprimir" con el código del ticket
2. Verificar conexión de impresora
3. El ticket se puede procesar sin impresión

#### **Código Ilegible:**
1. Buscar por placa del vehículo
2. Sistema muestra el código correcto
3. Procesar salida manualmente

### 💡 **VENTAJAS COMERCIALES**

#### **Control Total:**
- ✅ Imposible salir sin pagar
- ✅ Registro completo de movimientos
- ✅ Prevención de fraudes
- ✅ Cálculo exacto de tarifas

#### **Profesionalismo:**
- ✅ Tickets físicos de calidad
- ✅ Códigos únicos no duplicables
- ✅ Información completa y clara
- ✅ Proceso rápido y eficiente

### 📞 **SOPORTE TÉCNICO**

#### **Problemas Comunes:**
- **Impresora no responde:** Verificar conexión y drivers
- **Código no válido:** Verificar que el ticket esté activo
- **Error de cálculo:** Revisar configuración de tarifa

#### **Mantenimiento:**
- Hacer respaldo periódico de archivos de datos
- Limpiar tickets antiguos mensualmente
- Verificar configuración de impresora

---
**ParkingPro System con Tickets v1.1.0**  
*Sistema Profesional de Gestión de Aparcamientos con Control de Tickets*