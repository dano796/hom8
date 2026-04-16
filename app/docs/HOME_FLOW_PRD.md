# HomeFlow – PRD Completo
> **Versión:** 3.0  
> **Alcance:** Todos los módulos de la aplicación  
> **Plataforma:** Android (Kotlin + MVVM + Firebase/Firestore + Room)  
> **Uso:** Base de contexto para generación de código e IA  
> **Última actualización:** 2025

---

## Índice de módulos

| # | Módulo | Estado |
|---|---|---|
| 1 | Gastos compartidos | ✅ Completo (v2.0) |
| 2 | Tareas del hogar | ✅ Completo (v3.0) |
| 3 | Lista de compras | ✅ Completo (v3.0) |
| 4 | Calendario compartido | ✅ Completo (v3.0) |
| 5 | Hogar y miembros | ✅ Completo (v3.0) |
| 6 | Dashboard | ✅ Completo (v3.0) |
| 7 | Notificaciones | ✅ Completo (v3.0) |
| 8 | Perfil de usuario | ✅ Completo (v3.0) |

---

# MÓDULO 1 – GASTOS COMPARTIDOS
> *(Contenido completo de PRD v2.0 — ver documento original HomeFlow_PRD_v2.md)*  
> Este módulo está completamente especificado. Se incorpora aquí por referencia para mantener el documento unificado.

---

# MÓDULO 2 – TAREAS DEL HOGAR

## 2.1 Propósito del módulo

El módulo de Tareas permite a los miembros de un hogar **crear, asignar, organizar y completar tareas domésticas** de forma coordinada. Su objetivo es reducir la fricción en la distribución equitativa de responsabilidades del hogar.

### Objetivos funcionales
- Crear tareas con título, descripción, responsable y fecha límite
- Asignar tareas a uno o varios miembros del hogar
- Soportar tareas recurrentes (diaria, semanal, mensual)
- Marcar tareas como completadas
- Visualizar la carga de tareas por miembro (fairness)
- Mantener historial de tareas completadas

---

## 2.2 Modelo de datos

### Entidad `Task` (Firestore: `hogares/{hogarId}/tareas/{tareaId}`)

```kotlin
data class Task(
    val id: String,                        // UUID generado por Firestore
    val hogarId: String,                   // Referencia al hogar
    val titulo: String,                    // Texto libre, requerido, max 80 chars
    val descripcion: String? = null,       // Detalle opcional, max 300 chars
    val asignadoA: List<String>,           // Lista de userIds responsables
    val categoria: TaskCategory,           // Enum (ver sección 2.3)
    val prioridad: TaskPriority,           // ALTA, MEDIA, BAJA
    val estado: TaskStatus,                // PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA
    val fechaLimite: Timestamp? = null,    // Fecha límite (opcional)
    val recurrencia: Recurrence? = null,   // Config de repetición (null = no recurrente)
    val creadoPor: String,                 // userId
    val creadoEn: Timestamp,
    val actualizadoEn: Timestamp,
    val completadaPor: String? = null,     // userId de quien la marcó como completa
    val completadaEn: Timestamp? = null    // Cuándo se completó
)
```

### Entidad `Recurrence` (sub-documento embebido en Task)

```kotlin
data class Recurrence(
    val tipo: RecurrenceType,              // DIARIA, SEMANAL, MENSUAL, PERSONALIZADA
    val intervaloDias: Int? = null,        // Usado en PERSONALIZADA (ej: cada 3 días)
    val diasDeSemana: List<Int>? = null,   // Para SEMANAL: [1=Lunes, ..., 7=Domingo]
    val diaDelMes: Int? = null,            // Para MENSUAL: día del mes (1–31)
    val finRecurrencia: Timestamp? = null  // Fecha en que la recurrencia termina (null = indefinida)
)
```

> **Lógica de recurrencia:** Al completar una tarea recurrente, el sistema genera automáticamente la siguiente instancia con `estado = PENDIENTE` y la `fechaLimite` calculada según la regla de recurrencia. La instancia completada queda en el historial con `estado = COMPLETADA`.

### Enum `TaskCategory`

```kotlin
enum class TaskCategory(val label: String, val icon: String) {
    LIMPIEZA("Limpieza", "🧹"),
    COCINA("Cocina", "🍳"),
    COMPRAS("Compras", "🛒"),
    MANTENIMIENTO("Mantenimiento", "🔧"),
    MASCOTAS("Mascotas", "🐾"),
    JARDINERIA("Jardinería", "🌱"),
    ADMINISTRACION("Administración", "📄"),
    OTRO("Otro", "📌")
}
```

### Enum `TaskPriority`

```kotlin
enum class TaskPriority(val label: String, val color: String) {
    ALTA("Alta", "#E53935"),
    MEDIA("Media", "#FB8C00"),
    BAJA("Baja", "#43A047")
}
```

### Enum `TaskStatus`

```kotlin
enum class TaskStatus {
    PENDIENTE,      // Sin iniciar
    EN_PROGRESO,    // Alguien la marcó como iniciada
    COMPLETADA,     // Finalizada
    CANCELADA       // Descartada (soft-delete lógico)
}
```

---

## 2.3 Lógica de negocio

### Asignación de tareas

- Una tarea puede asignarse a **uno o varios** miembros del hogar.
- Si se asigna a múltiples, **cualquiera** de ellos puede marcarla como completada.
- El sistema registra en `completadaPor` al miembro que la completó.
- Si no se asigna a nadie, queda como tarea "libre" visible a todos los miembros.

### Cálculo de carga por miembro (Fairness Score)

```
Para cada miembro M en el período seleccionado:
  tareasPendientes[M]  = count(tareas donde asignadoA contiene M && estado == PENDIENTE)
  tareasCompletadas[M] = count(tareas donde completadaPor == M && período)
  cargaActual[M]       = tareasPendientes[M]
  contribución[M]      = tareasCompletadas[M]
```

Este score se muestra en la pantalla de balances de tareas como un indicador visual de equidad.

### Vencimiento de tareas

- Una tarea vence cuando `fechaLimite < now()` y `estado == PENDIENTE`.
- Las tareas vencidas se destacan visualmente con color rojo y ícono de alerta.
- Se envía notificación 24 horas antes del vencimiento al/los responsable(s).

---

## 2.4 Flujos de usuario

### Flujo: Crear tarea

```
PRECONDICIÓN: Usuario autenticado y pertenece a un hogar

1. Usuario toca FAB (+) en pantalla de Tareas
2. Se abre CreateTaskSheet (bottom sheet fullscreen)
3. Usuario completa:
   a. Título (obligatorio)
   b. Categoría (obligatorio, selector visual con íconos)
   c. Prioridad (obligatorio, default: MEDIA)
   d. Asignar a (opcional, multi-select de miembros)
   e. Fecha límite (opcional, date picker)
   f. Descripción (opcional)
   g. Recurrencia (opcional, toggle + configuración)
4. Toca "Crear tarea"
5. Validación:
   - Si título vacío → error inline
   - Si recurrencia PERSONALIZADA y intervaloDias < 1 → error inline
6. Guardar en Firestore y cerrar el sheet
7. Notificar a los asignados (excepto al creador si también está asignado)
```

### Flujo: Completar tarea

```
1. Usuario ve la tarea en la lista (estado PENDIENTE o EN_PROGRESO)
2. Desliza la tarjeta a la derecha (swipe-to-complete) O toca el checkbox
3. Sistema registra completadaPor = userId actual, completadaEn = now()
4. Estado cambia a COMPLETADA
5. Si es recurrente → generar nueva instancia (ver lógica de recurrencia)
6. Animación de celebración (confetti leve o check animado)
7. Snackbar "¡Tarea completada! +1 punto de contribución" con opción "Deshacer"
```

### Flujo: Editar / Cancelar tarea

```
Solo el creador o un admin puede editar o cancelar.
Editar → mismo formulario de creación con campos precargados.
Cancelar → confirmación "¿Cancelar esta tarea?" → estado = CANCELADA (soft-delete).
Si es recurrente y se cancela → confirmación adicional:
  "¿Cancelar solo esta instancia o todas las futuras?"
```

---

## 2.5 Estados de pantalla

### Lista de Tareas

| Estado | Condición | UI |
|---|---|---|
| **Cargando** | Fetch inicial | Skeleton de 3 tarjetas |
| **Vacío** | Sin tareas | Ilustración + "Sin tareas pendientes. ¡El hogar está al día! 🎉" |
| **Con datos** | ≥ 1 tarea | Lista agrupada por estado / fecha |
| **Filtro vacío** | Filtro activo sin resultados | "Ninguna tarea coincide" + "Limpiar filtros" |
| **Error** | Fallo de red | "Error al cargar" + "Reintentar" |

### Agrupación de la lista

```
[ Vencidas ]  ← color rojo, mostrar primero
[ Hoy ]
[ Esta semana ]
[ Próximamente ]
[ Sin fecha límite ]
```

---

## 2.6 Especificación UI

### Pantalla principal – Tareas

```
[ Tabs: Mis tareas | Todas | Completadas ]

[ Chips de filtro ]
  Todas | Alta prioridad | Por categoría | Por miembro

[ Lista de TaskCards ]

[ FAB "+" ]
```

### Componente `TaskCard`

```
┌──────────────────────────────────────────────┐
│ [Checkbox]  [🧹]  Limpiar baño    [ALTA ●]  │
│              Asignado a: [Av] Ana            │
│              📅 Vence: Hoy                   │
│                                   [⋮ menú]   │
└──────────────────────────────────────────────┘
```

- Swipe izquierda → opción "Cancelar"
- Swipe derecha → "Completar"
- Tap en tarjeta → Detalle de tarea

### Pantalla: Detalle de Tarea

| Sección | Contenido |
|---|---|
| Header | Título + categoría + prioridad + menú ⋮ |
| Estado | Chip de estado + botón de acción primaria contextual |
| Asignados | Avatares de los miembros asignados |
| Fecha límite | Fecha con indicador de urgencia |
| Recurrencia | Chip "Cada semana – Lunes y Miércoles" |
| Descripción | Texto de detalle si existe |
| Historial | Timeline de cambios de estado |

### Pantalla: Distribución de tareas (Fairness)

```
─── Este mes ──────────────────────────────
[Avatar] Ana        ████████  8 completadas
[Avatar] Bob        ████      4 completadas
[Avatar] Carlos     ██        2 completadas

─── Pendientes asignadas ──────────────────
[Avatar] Ana        ● ● ●     3 pendientes
[Avatar] Bob        ● ●       2 pendientes
[Avatar] Carlos     ●         1 pendiente
```

---

## 2.7 Reglas de negocio – Tareas

| # | Regla | Consecuencia |
|---|---|---|
| RT-01 | El título es obligatorio, máx. 80 chars | Error de validación |
| RT-02 | Solo creador o admin puede editar/cancelar | Opción no visible o 403 |
| RT-03 | Cualquier miembro asignado puede completar la tarea | — |
| RT-04 | Cancelar una tarea recurrente pregunta por alcance | Modal de confirmación |
| RT-05 | No se puede completar una tarea ya COMPLETADA o CANCELADA | Botón deshabilitado |
| RT-06 | Las tareas completadas se conservan en historial | No se eliminan físicamente |
| RT-07 | Una tarea sin asignar es visible para todos los miembros | — |

---

## 2.8 Reglas de seguridad – Firestore (Tareas)

```javascript
match /hogares/{hogarId}/tareas/{tareaId} {
  allow read: if isMemberOf(hogarId);

  allow create: if isMemberOf(hogarId)
    && request.resource.data.hogarId == hogarId
    && request.resource.data.creadoPor == request.auth.uid
    && request.resource.data.titulo.size() > 0;

  allow update: if isMemberOf(hogarId) && (
    // Cualquier miembro puede marcar como completada
    (request.resource.data.estado == 'COMPLETADA'
      && resource.data.estado in ['PENDIENTE', 'EN_PROGRESO'])
    ||
    // Solo creador o admin puede editar campos del formulario
    isCreatorOrAdmin(hogarId, resource.data.creadoPor)
  );

  allow delete: if false; // Soft delete únicamente
}
```

---

## 2.9 Notificaciones del módulo de Tareas

| Evento | Destinatarios | Título | Cuerpo |
|---|---|---|---|
| Tarea creada | Asignados (excepto creador) | "Nueva tarea asignada" | "{creador} te asignó: '{titulo}'" |
| Tarea completada | Creador (si ≠ quien completó) | "Tarea completada" | "{miembro} completó: '{titulo}'" |
| Tarea vence en 24h | Asignados con estado PENDIENTE | "¡Tarea por vencer!" | "'{titulo}' vence mañana" |
| Tarea vencida | Asignados | "Tarea vencida" | "'{titulo}' está vencida. ¿Puedes completarla?" |
| Tarea cancelada | Asignados | "Tarea cancelada" | "{creador} canceló: '{titulo}'" |

---

# MÓDULO 3 – LISTA DE COMPRAS

## 3.1 Propósito del módulo

El módulo de Lista de Compras permite a los miembros de un hogar **colaborar en tiempo real en una lista compartida**, agregar ítems, tacharlos mientras compran y mantener plantillas de productos frecuentes.

### Objetivos funcionales
- Crear y editar ítems de compra en tiempo real (colaborativo)
- Organizar por categorías de supermercado
- Marcar ítems como comprados durante la sesión de compra
- Crear plantillas reutilizables de listas frecuentes
- Ver historial de compras pasadas

---

## 3.2 Modelo de datos

### Entidad `ShoppingList` (Firestore: `hogares/{hogarId}/listas/{listaId}`)

```kotlin
data class ShoppingList(
    val id: String,
    val hogarId: String,
    val nombre: String,                    // "Lista semanal", "Barbacoa del sábado", etc.
    val estado: ShoppingListStatus,        // ACTIVA, COMPLETADA, ARCHIVADA
    val esPlantilla: Boolean = false,      // Si es plantilla reutilizable
    val creadoPor: String,
    val creadoEn: Timestamp,
    val actualizadoEn: Timestamp,
    val completadaEn: Timestamp? = null
)
```

### Entidad `ShoppingItem` (Firestore: `hogares/{hogarId}/listas/{listaId}/items/{itemId}`)

```kotlin
data class ShoppingItem(
    val id: String,
    val listaId: String,
    val nombre: String,                    // "Leche", "Pasta", etc.
    val cantidad: Double,                  // Cantidad numérica
    val unidad: ShoppingUnit,             // KG, L, UNIDAD, etc.
    val categoria: ShoppingCategory,       // LACTEOS, VERDURAS, LIMPIEZA, etc.
    val marcaPreferida: String? = null,    // Texto libre opcional
    val nota: String? = null,
    val comprado: Boolean = false,
    val compradoPor: String? = null,       // userId que lo tachó
    val compradoEn: Timestamp? = null,
    val agregadoPor: String,               // userId que agregó el ítem
    val agregadoEn: Timestamp,
    val orden: Int                         // Para ordenamiento manual en la lista
)
```

### Enum `ShoppingCategory`

```kotlin
enum class ShoppingCategory(val label: String, val icon: String) {
    FRUTAS_VERDURAS("Frutas y Verduras", "🥦"),
    CARNES("Carnes y Pescados", "🥩"),
    LACTEOS("Lácteos y Huevos", "🥛"),
    PANADERIA("Panadería", "🍞"),
    BEBIDAS("Bebidas", "🥤"),
    LIMPIEZA("Limpieza", "🧴"),
    HIGIENE("Higiene personal", "🪥"),
    CONGELADOS("Congelados", "🧊"),
    DESPENSA("Despensa / Granos", "🌾"),
    MASCOTAS("Mascotas", "🐾"),
    OTRO("Otro", "📦")
}
```

### Enum `ShoppingUnit`

```kotlin
enum class ShoppingUnit(val label: String) {
    UNIDAD("und"),
    KG("kg"),
    G("g"),
    L("L"),
    ML("mL"),
    PAQUETE("paq"),
    DOCENA("doc"),
    CAJA("caja")
}
```

### Enum `ShoppingListStatus`

```kotlin
enum class ShoppingListStatus {
    ACTIVA,       // Lista en uso
    COMPLETADA,   // Todos los ítems comprados o marcada como completa
    ARCHIVADA     // Guardada en historial, no editable
}
```

---

## 3.3 Lógica de negocio

### Colaboración en tiempo real

- Los cambios a `ShoppingItem` se sincronizan vía **Firestore `snapshotListener`** en la colección `items` de la lista activa.
- Cuando un miembro agrega o tacha un ítem, todos los que tienen la lista abierta lo ven en < 2 segundos.
- Los ítems tachados se mueven visualmente al final de la lista (sección "Comprado").

### Autocompletado de ítems frecuentes

- Mantener en Firestore una subcolección `hogares/{hogarId}/productosFrequentes` con los productos más añadidos.
- Al escribir en el campo de nuevo ítem, mostrar sugerencias de la lista de frecuentes del hogar + lista global de productos comunes.

### Generación de lista desde plantilla

```
1. Usuario selecciona "Usar plantilla"
2. Se muestra lista de plantillas del hogar
3. Al elegir una → crea nueva ShoppingList con los mismos ShoppingItems
   (todos con comprado = false)
4. Usuario puede editar antes de activar
```

### Completar lista de compras

```
Al marcar todos los ítems como comprado ó al tocar "Finalizar compra":
1. ShoppingList.estado = COMPLETADA
2. ShoppingList.completadaEn = now()
3. Opcional: preguntar "¿Guardar como plantilla para la próxima semana?"
4. Lista se mueve a historial
```

---

## 3.4 Flujos de usuario

### Flujo: Agregar ítem a la lista

```
PRECONDICIÓN: Existe una lista activa

1. Usuario toca "Agregar ítem" (campo de texto rápido en la parte inferior)
2. Escribe el nombre del ítem (con autocompletado)
3. Opcionalmente:
   - Ajustar cantidad y unidad
   - Seleccionar categoría (inferida automáticamente si se reconoce el producto)
   - Agregar nota o marca
4. Confirma con Enter o botón "+"
5. Ítem aparece en la lista inmediatamente (y en el dispositivo de otros miembros en < 2s)
```

### Flujo: Sesión de compras (modo compra)

```
1. Usuario toca "Iniciar compra" en la lista activa
2. La app entra en "Modo compra":
   - Vista simplificada, fuente más grande
   - Pantalla no se apaga (WakeLock)
   - Ítem agrupados por categoría de supermercado
3. Al tocar un ítem → se tacha (comprado = true) con animación
4. Al llegar a 0 ítems pendientes → animación de celebración + "¿Finalizar compra?"
5. Al finalizar → lista pasa a COMPLETADA
```

---

## 3.5 Estados de pantalla

### Lista de compras

| Estado | Condición | UI |
|---|---|---|
| **Vacío** | Sin ítems | "La lista está vacía. ¡Agrega el primer ítem!" |
| **Con ítems pendientes** | ≥ 1 ítem sin comprar | Lista activa con sección "Por comprar" |
| **Todos comprados** | Todos los ítems tachados | Sección "Comprado" con todos + CTA "Finalizar compra" |
| **Sin lista activa** | Ninguna lista en estado ACTIVA | "No hay lista activa" + botón "Crear nueva lista" |
| **Modo compra** | Iniciado por usuario | Vista simplificada agrupada por categoría |

---

## 3.6 Especificación UI

### Pantalla principal – Compras

```
[ Header: "Lista semanal"   [Historial] [⋮] ]

[ Progreso: 7 de 12 ítems comprados ████████░░░ ]

[ Por comprar (5) ]──────────────────────────────
  Frutas y Verduras 🥦
  ○  Manzanas          1 kg
  ○  Zanahorias        500 g

  Lácteos 🥛
  ○  Leche entera      2 L

[ Comprado (7) ]─────────────────────────────────
  ✓  Arroz             1 kg    (tachado)
  ✓  Pasta             2 paq   (tachado)

[ + Agregar ítem... ] ← campo siempre visible
```

### Componente `ShoppingItemCard`

```
┌────────────────────────────────────────────────┐
│ ○  Leche entera         2 L   Marca: Alpina    │
│    Nota: Sin lactosa                           │
│                         Agregado por: [Av] Ana │
└────────────────────────────────────────────────┘
```

- Tap en el círculo → tacha el ítem (comprado = true)
- Swipe izquierda → eliminar ítem
- Tap en el nombre → editar inline

---

## 3.7 Reglas de negocio – Compras

| # | Regla | Consecuencia |
|---|---|---|
| RC-01 | Solo puede haber una lista ACTIVA por hogar a la vez | Al crear nueva, se archiva la anterior |
| RC-02 | Cualquier miembro puede agregar, editar o tachar ítems | — |
| RC-03 | Solo creador o admin puede archivar o eliminar la lista | Opción no visible para otros |
| RC-04 | Los ítems tachados en modo offline se sincronizan al reconectar | Usar Room como caché local |
| RC-05 | Las plantillas no tienen estado ACTIVA; se instancian en una nueva lista | — |
| RC-06 | Cantidad mínima de un ítem es 0.01 (para pesos) y 1 para unidades enteras | Validación en UI |

---

## 3.8 Notificaciones del módulo de Compras

| Evento | Destinatarios | Título | Cuerpo |
|---|---|---|---|
| Ítem agregado a lista activa | Todos los miembros (excepto quien agregó) | "Lista actualizada" | "{miembro} agregó '{nombre}' a la lista" |
| Lista completada | Todos los miembros | "¡Compras completadas!" | "{miembro} finalizó la sesión de compras 🛒" |
| Nueva lista creada | Todos los miembros | "Nueva lista de compras" | "{miembro} creó la lista '{nombre}'" |

---

# MÓDULO 4 – CALENDARIO COMPARTIDO

## 4.1 Propósito del módulo

El módulo de Calendario permite a los miembros de un hogar **visualizar y coordinar eventos, recordatorios y vencimientos** en una vista compartida. Sirve como punto central de coordinación temporal del hogar.

### Objetivos funcionales
- Crear eventos con fecha, hora, duración y participantes
- Visualizar gastos, tareas y compras en el calendario
- Soportar eventos recurrentes
- Enviar recordatorios push antes de los eventos
- Vista mensual, semanal y por día

---

## 4.2 Modelo de datos

### Entidad `CalendarEvent` (Firestore: `hogares/{hogarId}/eventos/{eventoId}`)

```kotlin
data class CalendarEvent(
    val id: String,
    val hogarId: String,
    val titulo: String,                    // Max 80 chars
    val descripcion: String? = null,
    val tipo: EventType,                   // EVENTO, RECORDATORIO, TAREA, GASTO (cross-módulo)
    val fechaInicio: Timestamp,
    val fechaFin: Timestamp? = null,       // Null = evento de todo el día o recordatorio puntual
    val esTodoDia: Boolean = false,
    val participantes: List<String>,       // userIds
    val color: String,                     // Hex color asignado (por miembro o categoría)
    val ubicacion: String? = null,         // Texto libre
    val recurrencia: Recurrence? = null,   // Misma entidad que en Tareas
    val recordatorio: ReminderConfig? = null,
    val referenciaId: String? = null,      // ID de tarea/gasto vinculado (cross-módulo)
    val creadoPor: String,
    val creadoEn: Timestamp,
    val actualizadoEn: Timestamp,
    val estado: EventStatus                // ACTIVO, CANCELADO
)
```

### Entidad `ReminderConfig` (sub-documento)

```kotlin
data class ReminderConfig(
    val minutosAntes: Int,   // 15, 30, 60, 1440 (24h), 2880 (48h)
    val tipo: ReminderType   // PUSH, AMBOS
)
```

### Enum `EventType`

```kotlin
enum class EventType(val label: String, val icon: String) {
    EVENTO("Evento", "📅"),
    RECORDATORIO("Recordatorio", "🔔"),
    PAGO("Pago de servicio", "💰"),
    CITA("Cita", "🏥"),
    VIAJE("Viaje", "✈️"),
    OTRO("Otro", "📌")
}
```

---

## 4.3 Integración cross-módulo

El calendario consume datos de otros módulos para enriquecer la vista sin duplicar datos:

| Fuente | Se muestra como | Trigger |
|---|---|---|
| `Task` con `fechaLimite` | Evento de tipo TAREA en la fecha límite | Automático, no requiere acción del usuario |
| `Expense` con `fecha` | Punto en el calendario (indicador, no evento completo) | Opcional, configurable en ajustes del hogar |
| `CalendarEvent` manual | Evento completo con toda la información | Creado explícitamente por el usuario |

---

## 4.4 Especificación UI

### Pantalla principal – Calendario

```
[ Mes: Abril 2025    <  > ]

[ Vista mensual ]
  L   M   M   J   V   S   D
  .   1   2   3   4   5   6
  7   8   9  10  11  12  13
      ●       ●●           ← indicadores de eventos
 14  15  16  17  18  19  20
  ●   

[ Vista del día seleccionado (scroll vertical) ]
  08:00  ─────────────────
  09:00  [🔵] Pagar internet
  10:00  ─────────────────
  11:00  [🟢] Limpieza general (Tarea)
```

### Componente `EventCard` en vista de día

```
┌─────────────────────────────────────────────────┐
│ [●] Pagar internet                              │
│     📅 09:00 – Todo el día                      │
│     👥 Ana, Bob                                 │
│     📍 En línea                                 │
└─────────────────────────────────────────────────┘
```

---

## 4.5 Reglas de negocio – Calendario

| # | Regla | Consecuencia |
|---|---|---|
| RCal-01 | `fechaFin` no puede ser anterior a `fechaInicio` | Error de validación |
| RCal-02 | Solo creador o admin puede editar o cancelar un evento | Opción no visible |
| RCal-03 | Las tareas y gastos aparecen en el calendario automáticamente si tienen fecha | No requieren crear evento adicional |
| RCal-04 | Cancelar un evento recurrente pregunta por alcance (esta/todas las futuras) | Modal de confirmación |
| RCal-05 | Los eventos cancelados no se muestran en el calendario pero se conservan en Firestore | — |

---

## 4.6 Notificaciones del módulo de Calendario

| Evento | Destinatarios | Título | Cuerpo |
|---|---|---|---|
| Evento creado | Participantes (excepto creador) | "Nuevo evento en el hogar" | "{creador}: '{titulo}' el {fecha}" |
| Recordatorio configurado | Participantes del evento | "Recordatorio" | "'{titulo}' en {minutosAntes} minutos" |
| Evento cancelado | Participantes | "Evento cancelado" | "{creador} canceló '{titulo}'" |

---

# MÓDULO 5 – HOGAR Y MIEMBROS

## 5.1 Propósito del módulo

Este módulo gestiona la **creación, configuración y membresía del hogar**, que es la entidad central de la aplicación. Todo el contenido de HomeFlow existe dentro del contexto de un hogar.

### Objetivos funcionales
- Crear un hogar y configurar sus ajustes básicos
- Invitar miembros por código o link
- Gestionar roles y permisos de los miembros
- Configurar moneda, zona horaria y preferencias del hogar
- Permitir a un miembro abandonar o ser removido del hogar

---

## 5.2 Modelo de datos

### Entidad `Hogar` (Firestore: `hogares/{hogarId}`)

```kotlin
data class Hogar(
    val id: String,
    val nombre: String,                    // "Casa de los García", max 50 chars
    val descripcion: String? = null,
    val emoji: String = "🏠",             // Emoji representativo del hogar
    val moneda: String = "COP",           // ISO 4217 (COP, USD, EUR…)
    val zonaHoraria: String = "America/Bogota", // Zona horaria IANA
    val codigoInvitacion: String,          // Código alfanumérico de 6 chars, regenerable
    val linkInvitacion: String,            // URL de invitación con deep link
    val miembros: List<HogarMember>,       // Lista de miembros con sus roles
    val creadoPor: String,                 // userId del fundador
    val creadoEn: Timestamp,
    val actualizadoEn: Timestamp,
    val configuracion: HogarConfig
)
```

### Entidad `HogarMember` (sub-documento embebido)

```kotlin
data class HogarMember(
    val userId: String,
    val rol: MemberRole,                   // ADMIN, MIEMBRO
    val apodo: String? = null,             // Nombre personalizado dentro del hogar
    val colorAsignado: String,             // Hex color único en el hogar (para avatares y calendario)
    val uniodoEn: Timestamp,
    val estado: MemberStatus               // ACTIVO, INACTIVO (abandonó pero tiene deudas)
)
```

### Entidad `HogarConfig` (sub-documento)

```kotlin
data class HogarConfig(
    val gastosMostrarEnCalendario: Boolean = false,
    val notificacionesGlobalActivas: Boolean = true,
    val recordatorioTareaHoras: Int = 24,  // Horas de anticipación para notif. de vencimiento
    val permitirMiembrosInvitar: Boolean = true // Si solo admins pueden invitar
)
```

### Enum `MemberRole`

```kotlin
enum class MemberRole {
    ADMIN,    // Puede editar el hogar, remover miembros, editar/eliminar cualquier contenido
    MIEMBRO   // Puede crear contenido, editar el propio, no puede gestionar el hogar
}
```

### Enum `MemberStatus`

```kotlin
enum class MemberStatus {
    ACTIVO,   // Miembro activo
    INACTIVO  // Abandonó el hogar pero queda en registros históricos (deudas, gastos, etc.)
}
```

---

## 5.3 Flujos de usuario

### Flujo: Crear hogar

```
1. Usuario sin hogar abre la app
2. Pantalla de bienvenida: "Crear hogar" | "Unirse a un hogar"
3. Elige "Crear hogar"
4. Formulario:
   a. Nombre del hogar (obligatorio)
   b. Emoji representativo (picker, default: 🏠)
   c. Moneda (selector, default según locale)
5. Toca "Crear"
6. Sistema:
   - Genera hogarId en Firestore
   - Genera codigoInvitacion (6 chars alfanumérico único)
   - Genera linkInvitacion (deep link)
   - Asigna al creador como ADMIN
7. App entra al hogar recién creado
8. Modal: "¡Hogar creado! Invita a tus compañeros de hogar" con opciones de compartir
```

### Flujo: Unirse a un hogar por código

```
1. Usuario sin hogar elige "Unirse a un hogar"
2. Ingresa código de 6 caracteres
3. Sistema valida que el código existe y el hogar no está lleno (max 10 miembros)
4. Preview del hogar: nombre, emoji, número de miembros actuales
5. Confirma "Unirse"
6. Se agrega al usuario como MIEMBRO con MemberStatus.ACTIVO
7. Notificación a todos los miembros del hogar: "Nuevo miembro: {nombre}"
```

### Flujo: Gestión de miembros (solo ADMIN)

```
En Ajustes del hogar → Miembros:
- Ver lista de miembros con rol y fecha de unión
- Cambiar rol: MIEMBRO ↔ ADMIN (no se puede degradar al único admin)
- Remover miembro: confirmación → MemberStatus = INACTIVO
  - Si el miembro tiene deudas pendientes → advertencia antes de remover
- Regenerar código de invitación (invalida el anterior)
```

---

## 5.4 Especificación UI

### Pantalla: Ajustes del hogar

```
[ 🏠 Casa de los García ]

─── Miembros (3) ─────────────────────────────────
  [Av] Ana García     ADMIN      Fundadora
  [Av] Bob Martínez   Miembro    Desde: Mar 2025
  [Av] Carlos López   Miembro    Desde: Abr 2025
  [ + Invitar miembro ]

─── Invitación ───────────────────────────────────
  Código: ABC123   [Copiar] [Compartir] [Regenerar]

─── Configuración ────────────────────────────────
  Moneda              COP >
  Zona horaria        América/Bogotá >
  Gastos en calendario   [Toggle]
  Cualquier miembro puede invitar [Toggle]

─── Zona peligrosa ───────────────────────────────
  [ Abandonar el hogar ]
```

---

## 5.5 Reglas de negocio – Hogar y Miembros

| # | Regla | Consecuencia |
|---|---|---|
| RH-01 | Un hogar tiene mínimo 1 y máximo 10 miembros | Error si se intenta unir al hogar lleno |
| RH-02 | Siempre debe haber al menos 1 ADMIN activo | No se puede degradar al último admin |
| RH-03 | Al remover un miembro con deudas pendientes → advertencia | El admin confirma explícitamente |
| RH-04 | El código de invitación es único globalmente | Regenerado si hay colisión |
| RH-05 | Un usuario solo puede pertenecer a un hogar a la vez (MVP) | Error "Ya perteneces a un hogar" |
| RH-06 | Al abandonar el hogar, el historial del usuario queda asociado a su userId | Solo se pone MemberStatus = INACTIVO |
| RH-07 | El fundador no puede ser removido por otros admins | Botón "Remover" no aparece para el creador |

---

# MÓDULO 6 – DASHBOARD

## 6.1 Propósito del módulo

El Dashboard es la **pantalla de inicio** de HomeFlow. Su objetivo es ofrecer una visión rápida y accionable del estado actual del hogar: deudas urgentes, tareas pendientes, próximos eventos y actividad reciente.

---

## 6.2 Estructura de la pantalla

```
[ Saludo: "Buenos días, Ana 👋" ]
[ Hogar: 🏠 Casa de los García ]

─── Tu balance ─────────────────────────────────
  Debes: $45.00 a María   [ Saldar ]
  Te deben: $33.33         [ Ver detalle ]

─── Tareas pendientes ──────────────────────────
  🧹  Limpiar baño      Vence: Hoy       [Completar]
  🍳  Preparar cena     Vence: Mañana

  [ Ver todas las tareas ]

─── Lista de compras ───────────────────────────
  7 ítems en la lista semanal
  [ Ir a la lista ]

─── Próximos eventos ───────────────────────────
  📅  Pagar arriendo    Mañana - Todo el día
  📅  Reunión de hogar  Sáb 19 - 18:00

─── Actividad reciente ─────────────────────────
  [Av] Bob registró "Supermercado" $120.000   hace 2h
  [Av] Carlos completó "Sacar basura"         hace 5h
  [Av] Ana agregó "Detergente" a la lista     hace 1d

  [ Ver todo el historial ]
```

---

## 6.3 Modelo de datos del Dashboard

El dashboard **no tiene colección propia en Firestore**. Agrega datos de las demás colecciones en el cliente usando:

- `snapshotListener` en `gastos`, `tareas`, `listas`, `eventos` del hogar
- Cálculo de balance en cliente (igual que en el módulo de Gastos)
- Las últimas 10 acciones del `activityFeed` para la sección de actividad reciente

### Feed de actividad – Entidad `ActivityEvent` (Firestore: `hogares/{hogarId}/actividad/{actId}`)

```kotlin
data class ActivityEvent(
    val id: String,
    val hogarId: String,
    val tipo: ActivityType,               // GASTO_CREADO, TAREA_COMPLETADA, etc.
    val actorId: String,                  // userId que realizó la acción
    val descripcion: String,              // Texto ya formateado para mostrar
    val referenciaId: String? = null,     // ID del objeto relacionado
    val referenciaModulo: String? = null, // "gastos", "tareas", "listas"
    val creadoEn: Timestamp
)
```

### Enum `ActivityType`

```kotlin
enum class ActivityType {
    GASTO_CREADO, GASTO_EDITADO, GASTO_ELIMINADO,
    PAGO_REGISTRADO,
    TAREA_CREADA, TAREA_COMPLETADA, TAREA_CANCELADA,
    ITEM_AGREGADO, LISTA_COMPLETADA,
    EVENTO_CREADO, EVENTO_CANCELADO,
    MIEMBRO_UNIDO, MIEMBRO_REMOVIDO
}
```

---

## 6.4 Reglas de visualización

| Widget | Condición de visibilidad | Comportamiento vacío |
|---|---|---|
| Balance | Siempre visible | "Estás al día 🎉" |
| Tareas pendientes | Siempre visible | "Sin tareas pendientes" |
| Lista de compras | Solo si hay lista ACTIVA | Widget oculto si no hay lista activa |
| Próximos eventos | Solo si hay eventos en los próximos 7 días | Widget oculto |
| Actividad reciente | Siempre visible si hay ≥ 1 evento en los últimos 30 días | "Sin actividad reciente" |

---

# MÓDULO 7 – NOTIFICACIONES

## 7.1 Propósito del módulo

El módulo de Notificaciones gestiona de forma centralizada todas las **notificaciones push** de HomeFlow y las preferencias del usuario para recibirlas.

---

## 7.2 Infraestructura

- **FCM (Firebase Cloud Messaging):** Transporte de notificaciones push a Android.
- **Cloud Functions:** Trigger on Firestore `onCreate` / `onUpdate` para generar y enviar notificaciones.
- **Token FCM:** Almacenado en `usuarios/{userId}/tokens/{tokenId}` y actualizado en cada login.

### Entidad `NotificationPreference` (Firestore: `usuarios/{userId}/preferencias/notificaciones`)

```kotlin
data class NotificationPreference(
    val gastos: Boolean = true,
    val tareas: Boolean = true,
    val compras: Boolean = true,
    val calendario: Boolean = true,
    val hogar: Boolean = true,
    val resumenDiario: Boolean = false,    // Resumen diario (push a las 8am)
    val horaResumen: Int = 8              // Hora de envío del resumen diario (0–23)
)
```

---

## 7.3 Tipos de notificación y prioridades

| Canal | Prioridad FCM | Descripción |
|---|---|---|
| Deuda urgente (>$50.000) | HIGH | Balance con deuda alta sin saldar |
| Tarea vencida | HIGH | Tarea cuya fecha límite ya pasó |
| Nuevo gasto | NORMAL | Gasto registrado donde el usuario es participante |
| Tarea completada | NORMAL | Una tarea asignada al usuario fue completada por otro |
| Recordatorio de evento | HIGH | Recordatorio configurado antes de un evento |
| Nuevo miembro | LOW | Alguien se unió al hogar |
| Actividad de lista | LOW | Alguien agregó ítems a la lista de compras |
| Resumen diario | LOW | Resumen matutino de pendientes del hogar |

---

## 7.4 Centro de notificaciones in-app

Además de las notificaciones push, la app mantiene un **centro de notificaciones** in-app:

```
[ 🔔 Notificaciones ]

─── Hoy ──────────────────────────────────────
  [💰] Bob registró "Supermercado" – te corresponden $40.000     hace 2h
  [✅] Carlos completó "Sacar basura"                             hace 5h

─── Esta semana ──────────────────────────────
  [🔔] Recordatorio: Pagar arriendo mañana                       hace 1d
  [🏠] Ana se unió al hogar                                      hace 3d
```

### Entidad `InAppNotification` (Firestore: `usuarios/{userId}/notificaciones/{notifId}`)

```kotlin
data class InAppNotification(
    val id: String,
    val tipo: ActivityType,
    val titulo: String,
    val cuerpo: String,
    val leida: Boolean = false,
    val referenciaId: String? = null,
    val referenciaModulo: String? = null,
    val creadoEn: Timestamp
)
```

---

# MÓDULO 8 – PERFIL DE USUARIO

## 8.1 Propósito del módulo

El módulo de Perfil gestiona la **identidad del usuario dentro de HomeFlow**, sus preferencias personales y su resumen de actividad en el hogar.

---

## 8.2 Modelo de datos

### Entidad `UserProfile` (Firestore: `usuarios/{userId}`)

```kotlin
data class UserProfile(
    val id: String,                        // userId de Firebase Auth
    val nombre: String,                    // Nombre de display
    val email: String,
    val fotoPerfil: String? = null,        // URL de imagen en Firebase Storage
    val colorPerfil: String,               // Hex, asignado al unirse al hogar
    val hogarId: String? = null,           // ID del hogar actual (null si no pertenece a ninguno)
    val creadoEn: Timestamp,
    val actualizadoEn: Timestamp,
    val configuracion: UserConfig
)
```

### Entidad `UserConfig` (sub-documento)

```kotlin
data class UserConfig(
    val tema: AppTheme = AppTheme.SISTEMA, // CLARO, OSCURO, SISTEMA
    val idioma: String = "es",             // ISO 639-1
    val notificaciones: NotificationPreference
)
```

### Enum `AppTheme`

```kotlin
enum class AppTheme { CLARO, OSCURO, SISTEMA }
```

---

## 8.3 Especificación UI – Pantalla de Perfil

```
[ (Foto de perfil) ]
[ Ana García ]
[ ana@email.com ]
[ Miembro desde: Enero 2025 ]

─── Mi actividad (este mes) ─────────────────────
  💰 Gastos registrados:    8
  💵 Total pagado:          $320.000
  ✅ Tareas completadas:    12
  🛒 Ítems agregados:       25

─── Ajustes personales ──────────────────────────
  Tema de la app          Sistema >
  Notificaciones          Configurar >
  Idioma                  Español >

─── Cuenta ──────────────────────────────────────
  Cambiar foto de perfil
  Cambiar nombre
  [ Cerrar sesión ]
  [ Eliminar cuenta ]  ← color rojo, confirmación requerida
```

---

## 8.4 Flujo: Eliminar cuenta

```
1. Usuario toca "Eliminar cuenta"
2. Modal de advertencia:
   "Esta acción es irreversible. Tu historial de gastos y tareas
   permanecerá en el hogar asociado a tu nombre, pero ya no podrás
   acceder a la app."
3. Si el usuario es el único ADMIN de un hogar → advertencia adicional:
   "Debes designar otro administrador antes de eliminar tu cuenta."
4. Si procede:
   - Firebase Auth: elimina el usuario
   - Firestore: UserProfile.estado = ELIMINADO
   - HogarMember.estado = INACTIVO (los datos del hogar se conservan)
```

---

## 8.5 Reglas de negocio – Perfil

| # | Regla | Consecuencia |
|---|---|---|
| RU-01 | El nombre de usuario es obligatorio, máx. 50 chars | Error de validación |
| RU-02 | La foto de perfil se guarda en Firebase Storage bajo `perfiles/{userId}.jpg` | Max 5 MB, formatos: jpg, png, webp |
| RU-03 | No se puede eliminar la cuenta si se es el único admin del hogar | Bloqueo con mensaje explicativo |
| RU-04 | Los datos del hogar se conservan tras eliminar la cuenta | El userId queda como referencia histórica |
| RU-05 | Cambiar el email requiere reautenticación | Flujo de reautenticación de Firebase Auth |

---

# ARQUITECTURA TÉCNICA GLOBAL

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| Arquitectura | MVVM + Clean Architecture |
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Base de datos remota | Cloud Firestore |
| Base de datos local | Room (caché offline) |
| Autenticación | Firebase Auth (Email/Password + Google Sign-In) |
| Almacenamiento de archivos | Firebase Storage |
| Notificaciones push | Firebase Cloud Messaging (FCM) |
| Lógica de servidor | Firebase Cloud Functions (Node.js) |
| Inyección de dependencias | Hilt |
| Corrutinas | Kotlin Coroutines + Flow |
| Imágenes | Coil |
| Logging / Crashlytics | Firebase Crashlytics |
| Analytics | Firebase Analytics |

---

## Estructura de colecciones en Firestore

```
usuarios/
  {userId}/
    notificaciones/{notifId}
    preferencias/
      notificaciones

hogares/
  {hogarId}/
    gastos/{gastoId}
    pagos/{pagoId}
    tareas/{tareaId}
    listas/{listaId}/
      items/{itemId}
    eventos/{eventoId}
    actividad/{actId}
    productosFrequentes/{productoId}
```

---

## Convenciones de código

| Convención | Regla |
|---|---|
| Nombres de clases | PascalCase |
| Nombres de funciones y variables | camelCase |
| Constantes | SCREAMING_SNAKE_CASE |
| Repositorios | `{Modulo}Repository.kt` |
| ViewModels | `{Pantalla}ViewModel.kt` |
| Screens (Compose) | `{Pantalla}Screen.kt` |
| IDs de Firestore | UUID generado por `UUID.randomUUID().toString()` |
| Timestamps | Siempre `FieldValue.serverTimestamp()` en creación, Timestamp local en lectura |
| Soft delete | Nunca usar `.delete()` en Firestore desde el cliente; usar campo `estado = ELIMINADO` |

---

## Manejo de errores global

| Tipo de error | Comportamiento esperado |
|---|---|
| Sin conexión | Room como caché; banner "Sin conexión" en la parte superior |
| Timeout de Firestore (>10s) | Snackbar "Error al cargar. Toca para reintentar" |
| Error de validación en formulario | Mensajes inline bajo cada campo inválido |
| Error 403 (Firestore rules) | Snackbar "No tienes permiso para esta acción" |
| Error crítico / crash | Firebase Crashlytics captura el stack trace automáticamente |
| Token FCM expirado | Regenerado automáticamente en el próximo login |

---

*— HomeFlow PRD Completo v3.0 | Documento de referencia unificado para IA y desarrollo —*
