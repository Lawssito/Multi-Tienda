# StockMulti (Multi-Tienda)

Aplicación Android para controlar el inventario de una cadena con varias sucursales y una bodega. Cada persona entra con su rol (Operador, Encargado o Gerencia) y ve solo las pantallas de su trabajo: el operador mueve y cuenta el stock con la cámara, el encargado revisa lo que ocurre en su sucursal y la gerencia supervisa toda la red.

Proyecto de la asignatura **Desarrollo de Aplicaciones Móviles** (Duoc UC).

## Integrantes

- Andrés Cereño
- Diego Rivera

## Problema que resuelve

En una cadena con varias tiendas el stock real se separa del registrado: llegan cajas incompletas o con productos dañados, hay mermas y errores de conteo, y nadie sabe a tiempo qué producto se está quebrando en qué sucursal. StockMulti registra cada movimiento por sucursal, pide revisión del encargado cuando hay diferencias y avisa a la gerencia cuando un producto baja de su mínimo.

## Funcionalidades por rol

### Operador
- **Elegir sucursal** antes de trabajar.
- **Escáner** con la cámara (códigos de barras y QR) para registrar **entradas, salidas y ajustes**. El ajuste exige un motivo (merma, dañado, contado mal u otro).
- **Código manual** cuando el código no se puede leer, y **linterna** para lugares oscuros.
- **Conteo masivo**: cuenta los productos de la sucursal y lo envía al encargado. Los productos que no se cuentan no se modifican.
- **Recepción de cajas**: escanea el código del despacho (por ejemplo `DSP-0042`), registra lo recibido y marca las unidades dañadas con su motivo. Solo las unidades buenas entran al stock.

### Encargado
- **Stock** de su sucursal con filtros Todos / Quiebre / Bajo stock / Normal. Al tocar un producto puede **declarar una reposición** a gerencia.
- **Diferencias** del último conteo (faltantes y sobrantes).
- **Alertas**: cajas por revisar (decide por línea: aceptar, reclamar faltante, esperar segundo envío, devolver, dar de baja o liberar), conteos por aprobar o rechazar, productos bajo el mínimo, ajustes del operador de las últimas 24 h y el estado de sus reposiciones.
- **Cuenta** y cambio de sucursal.

### Gerencia
- **Panel** con el stock total de la red, alertas activas, movimientos del período, stock por sucursal y reportes PDF (reposición sugerida y mermas y pérdidas).
- **Stock** global con el detalle por sucursal y edición del **stock mínimo** de cada producto.
- **Alertas**: aprueba o rechaza reposiciones (aprobar crea un despacho `DSP-xxxx`), crea despachos para productos bajo el mínimo y marca alertas como revisadas con una nota.
- **Historial** de movimientos con filtros por tienda, fecha y tipo, incidencias de cajas y exportación a PDF.
- **Tiendas habilitadas**: activar o desactivar sucursales y registrar nuevas.

## Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Operador | operador@stockmulti.com | opedemo123 |
| Encargado | encargado@stockmulti.com | opedemo123 |
| Gerencia | gerencia@stockmulti.com | opedemo123 |

La primera vez que se abre, la app carga datos de prueba: 3 sucursales (Sucursal Centro, Sucursal Norte y Bodega Central), productos con stock repartido entre ellas y dos despachos para probar la recepción de cajas: `DSP-0042` y `DSP-0043`.

## Tecnologías

- Kotlin y **Jetpack Compose** con **Material Design 3**
- **Navigation Compose** con un grafo por rol
- **MVVM**: ViewModel + `StateFlow`
- **Room** (SQLite) con KSP
- **CameraX** + **ML Kit Barcode Scanning** para leer códigos
- `PdfDocument` + `FileProvider` para generar y compartir reportes PDF

Versiones principales (ver `gradle/libs.versions.toml`): AGP 9.4.1, Kotlin 2.2.10, Compose BOM 2026.02.01, Room 2.7.0, CameraX 1.6.2. `minSdk` 24, `targetSdk` 37.

## Arquitectura y carpetas

```
app/src/main/java/com/bexai/multitienda/
├── model/            Entidades de Room y clases de datos (Producto, Sucursal, Movimiento, Conteo, Recepcion…)
├── data/
│   ├── dao/          Un DAO por tabla (consultas SQL de Room)
│   ├── repository/   Reglas de negocio que tocan varias tablas (Existencias, Conteo, Recepcion, Usuario)
│   ├── local/        AppDatabase (Singleton) y DemoDataSeeder (datos de prueba)
│   └── sesion/       Estado en memoria compartido entre pantallas (SesionSucursal, HistorialFiltro)
├── viewmodel/
│   ├── comun/        LoginViewModel y el manejador de errores de corrutinas
│   ├── operador/     Escaner, Conteo, Recepcion, Inicio y SeleccionarSucursal
│   ├── encargado/    EncargadoViewModel
│   └── gerencia/     GerenciaViewModel, armado de alertas y modelos de estado
├── ui/
│   ├── components/   Componentes reutilizables (barra inferior, campo validado, pastilla de sucursal…)
│   ├── navigation/   Rutas, NavHost, un grafo por rol y ConBarraRol (barra inferior común)
│   ├── theme/        Colores, tipografía y tema Material 3
│   └── view/
│       ├── comun/        Login y Cuenta
│       ├── operador/     inicio/, escaner/, conteo/ y recepcion/
│       ├── encargado/    Stock, Diferencias, Alertas y revisión de cajas
│       └── gerencia/     Panel, Stock, Alertas, Historial, Tiendas y reportes PDF
└── util/             Generación y envío de PDF
```

- **Vista (Composable)**: solo muestra el estado y avisa las acciones del usuario.
- **ViewModel**: valida los datos, mantiene el estado en `StateFlow` y llama a los repositorios.
- **Repositorio / DAO**: lee y escribe en Room. Las operaciones que tocan varias tablas (por ejemplo aprobar un conteo o enviar una recepción) se hacen en una transacción.
- **Errores**: las corrutinas de los ViewModel usan un `CoroutineExceptionHandler` común (`viewmodel/comun/ManejoErrores.kt`); si falla una operación la app muestra un aviso o lo registra en Logcat en vez de cerrarse. La cámara y los PDF también capturan sus errores.

## Persistencia local (Room)

Base de datos `AppDatabase` con, entre otras, las tablas `producto`, `sucursales`, `StockSucursal` (stock de cada producto en cada sucursal), `movimiento`, `Conteo`/`ConteoDetalle`, `Despacho`, `Recepcion`/`RecepcionDetalle`, `Reposicion` y `AlertaRevisada`. Las pantallas observan los datos con `Flow`, así que se actualizan solas cuando cambia la base.

## Recursos nativos

- **Cámara**: permiso `CAMERA` pedido en tiempo de ejecución. Si se rechaza, la pantalla lo explica y permite reintentar.
- **Linterna (flash)** de la cámara, desde el escáner.
- **Archivos y compartir**: los reportes PDF se guardan en la caché de la app y se comparten con el selector de Android mediante `FileProvider`.

## Validaciones de formularios

Los campos con error se marcan en rojo con un mensaje debajo. El formulario de sucursal usa además el componente `ValidatedTextField`, que agrega un ícono de error. En el login y en el registro de sucursal la lógica de validación vive en el ViewModel y la pantalla solo muestra el resultado. Ejemplos:

- **Login**: correo con formato válido y contraseña de al menos 8 caracteres.
- **Registrar sucursal**: nombre de 3 a 40 caracteres, sin repetir, y dirección de al menos 5 caracteres.
- **Ajuste de stock**: motivo obligatorio.

## Animaciones

- En **Alertas** (Gerencia y Encargado), cuando una alerta se resuelve su fila se desvanece y las de abajo suben a ocupar su lugar (`Modifier.animateItem`).

## Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Lawssito/Multi-Tienda.git
   ```
2. Abrir la carpeta en **Android Studio** y esperar a que Gradle sincronice.
3. Ejecutar la configuración `app` en un emulador o un teléfono con Android 7.0 (API 24) o superior.
4. Iniciar sesión con una de las cuentas de prueba.
5. Para probar el escáner en un emulador, usar la cámara virtual o el botón **Código manual**.

## Planificación

Las tareas se organizaron en Jira (épicas: base del proyecto, persistencia con Room, flujo del operador, flujo de encargado y gerencia, recursos nativos y animaciones, calidad y entrega). Los commits siguen el formato `tipo(área): descripción [SM-xx]`.
