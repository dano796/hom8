# HomeFlow – Product Requirements Document (PRD)
> **Versión:** 1.1  
> **Plataforma:** Android (Kotlin + MVVM + Firebase)  
> **Distribución:** Google Play Store  
> **Estado:** MVP en desarrollo  
> **Última actualización:** 2025

---

## 1. Visión del producto

**HomeFlow** es una aplicación móvil de gestión integral del hogar que centraliza tareas, finanzas compartidas, calendario y coordinación entre miembros en un entorno colaborativo.

### Propuesta de valor
> *"Que tu hogar funcione como un sistema organizado: sin olvidos, sin desigualdad, sin fricción."*

### Objetivos del MVP

| Objetivo | Métrica de éxito |
|---|---|
| Centralizar la gestión del hogar | ≥3 funciones usadas por sesión |
| Mejorar distribución de tareas | Reducción de tareas vencidas ≥30% |
| Reducir tareas olvidadas | Retención semanal ≥40% |
| Facilitar control de gastos compartidos | ≥1 gasto registrado por semana/usuario activo |

---

## 2. Usuarios objetivo

### Segmentos
- **Familias** (padres e hijos adolescentes/adultos)
- **Parejas** (con o sin hijos)
- **Roommates** (compañeros de piso)
- **Usuarios individuales** (organización personal)

### Personas

#### 🧑‍💼 Persona 1 – El Organizador del Hogar
- **Rol:** Administrador del hogar
- **Motivación:** Delegar tareas y tener visibilidad general del estado del hogar
- **Frustración:** No saber si alguien hizo algo, tener que hacer seguimiento manual
- **Uso clave:** Dashboard, asignación de tareas, resumen de gastos

#### 🙋 Persona 2 – El Miembro Ejecutor
- **Rol:** Miembro del hogar
- **Motivación:** Saber exactamente qué le toca hacer y cuándo
- **Frustración:** Olvidar tareas, no recibir avisos a tiempo
- **Uso clave:** Lista de tareas propias, notificaciones, marcar como completado

#### 💰 Persona 3 – El Usuario Financiero
- **Rol:** Administrador o miembro con enfoque en gastos
- **Motivación:** Dividir gastos de forma justa y transparente
- **Frustración:** Discusiones por quién debe qué, falta de registro
- **Uso clave:** Módulo de gastos, balances, historial

---

## 3. Arquitectura de información

### Navegación principal (Bottom Navigation Bar)
```
[ 🏠 Home ] [ ✅ Tareas ] [ 📅 Calendario ] [ 💰 Gastos ] [ 👤 Perfil ]
```

### Mapa de pantallas completo

```
App
├── Onboarding
│   ├── Splash / Bienvenida
│   ├── Registro / Login
│   └── Configurar hogar
│       ├── Crear hogar nuevo
│       └── Unirse a hogar existente (código/enlace)
│
├── Home (Dashboard)
│
├── Tareas
│   ├── Lista de tareas (filtros: estado, persona, prioridad, etiqueta)
│   ├── Detalle de tarea
│   ├── Crear / Editar tarea
│   └── Gestión de etiquetas
│
├── Calendario
│   ├── Vista mensual
│   └── Vista semanal
│
├── Gastos
│   ├── Lista de gastos
│   ├── Crear / Editar gasto
│   ├── Detalle de gasto
│   └── Balances entre miembros
│
└── Perfil
    ├── Perfil personal + estadísticas
    ├── Gestión de miembros del hogar
    └── Configuración
```

---

## 4. Pantallas del MVP – Especificaciones para Wireframes

> **Convención visual:**  
> - Diseñar en baja fidelidad (escala de grises)  
> - Dispositivo base: 390×844 pt (iPhone 14 / equivalente Android)  
> - Incluir siempre los estados: **vacío**, **cargando**, **con datos**, **error**  
> - Usar componentes reutilizables etiquetados

---

### 4.1 Onboarding

#### Pantalla: Splash / Bienvenida
- Logo centrado
- Tagline breve
- CTA: "Crear cuenta" (primario) + "Ya tengo cuenta" (secundario)

#### Pantalla: Registro / Login
- Campos: Email, contraseña
- Botón: "Entrar con Google"
- Link: Olvidé mi contraseña

#### Pantalla: Configurar Hogar
- Dos opciones con iconos grandes:
  - **Crear un nuevo hogar** → formulario: nombre del hogar
  - **Unirme a un hogar existente** → campo para código o enlace
- CTA: "Continuar"

---

### 4.2 Home – Dashboard

**Propósito:** Vista ejecutiva del estado del hogar. Primera pantalla tras el login.

**Componentes (de arriba abajo):**

| Componente | Descripción |
|---|---|
| Header | Saludo personalizado + avatar del usuario + icono de notificaciones |
| Banner de equilibrio | Barra visual que muestra distribución de tareas entre miembros |
| Sección "Mis tareas de hoy" | Tarjetas compactas de tareas del día con checkbox rápido |
| Sección "Próximas tareas" | Lista de las siguientes 3 tareas con fecha |
| Sección "Resumen de gastos" | Balance total del mes + deuda más urgente |
| Sección "Actividad reciente" | Feed de últimas acciones del hogar |
| FAB | Botón flotante (+) para creación rápida |

**Estados:**
- **Vacío (nuevo usuario):** Ilustración + "Tu hogar está listo. ¡Empieza creando una tarea!"
- **Con datos:** Layout completo descrito arriba

---

### 4.3 Tareas – Lista

**Propósito:** Ver, filtrar y gestionar todas las tareas del hogar.

**Componentes:**

| Componente | Descripción |
|---|---|
| Barra de búsqueda | Búsqueda por título o etiqueta |
| Chips de filtro horizontal | Todos / Mis tareas / Por persona / Por prioridad / Por etiqueta |
| Selector de ordenamiento | Fecha / Prioridad / Estado |
| Lista de tarjetas de tarea | Ver spec de Tarjeta de Tarea abajo |
| FAB | Botón "+" para crear tarea |

**Tarjeta de Tarea (componente reutilizable):**
- Checkbox de completado (izquierda)
- Título de la tarea
- Avatar del responsable
- Fecha límite (con color: verde = a tiempo, naranja = pronto, rojo = vencida)
- Badge de prioridad (Alta / Media / Baja)
- Chips de etiquetas

**Estados:**
- **Vacío:** "No hay tareas. ¡Crea la primera!"
- **Filtro sin resultados:** "Ninguna tarea coincide con este filtro"
- **Todas completadas:** Ilustración de celebración

---

### 4.4 Tareas – Crear / Editar Tarea

**Propósito:** Formulario completo para crear o modificar una tarea.

**Campos del formulario:**

| Campo | Tipo de input | Requerido |
|---|---|---|
| Título | Text input | ✅ |
| Descripción | Textarea (expandible) | ❌ |
| Responsable | Selector de miembro (avatares) | ✅ |
| Fecha límite | Date picker | ✅ |
| Prioridad | Segmented control (Alta / Media / Baja) | ✅ |
| Recurrencia | Toggle + selector (diaria/semanal/mensual/personalizada) | ❌ |
| Etiquetas | Multi-select chips | ❌ |
| Checklist interno | Items de subtareas (agregar/eliminar) | ❌ |
| Archivos adjuntos | Botón de adjuntar (imagen/documento) | ❌ |
| Comentario inicial | Textarea | ❌ |

**Acciones:**
- Guardar (primario)
- Cancelar (secundario)
- Eliminar (solo en edición, destructivo / rojo)

**Comportamiento recurrencia:**
- Toggle OFF → campos de recurrencia ocultos
- Toggle ON → aparece: frecuencia + día(s) + fecha fin (opcional)
- Modal de confirmación al editar tarea recurrente: "¿Editar solo esta ocurrencia o toda la serie?"

---

### 4.5 Tareas – Detalle de Tarea

**Propósito:** Vista completa de una tarea con historial y comentarios.

**Secciones:**

| Sección | Contenido |
|---|---|
| Header | Título + badge de estado + botón de menú (editar/eliminar) |
| Metadata | Responsable, fecha límite, prioridad, etiquetas |
| Descripción | Texto completo |
| Checklist | Subtareas con checkboxes |
| Archivos adjuntos | Thumbnails de archivos |
| Comentarios | Lista de comentarios con avatar + texto + timestamp |
| Input de comentario | Campo fijo en la parte inferior |
| Historial de actividad | Timeline de cambios de estado |

**Acciones:**
- Marcar como completada (botón prominente)
- Editar (icono en header)
- Eliminar (dentro del menú contextual)

---

### 4.6 Calendario

**Propósito:** Visualizar tareas y eventos distribuidos en el tiempo.

**Componentes:**

| Componente | Descripción |
|---|---|
| Toggle de vista | Mensual / Semanal |
| Calendario | Grid mensual con puntos de color por miembro en días con tareas |
| Selector de miembro | Filtrar por persona (chips con avatar) |
| Lista de tareas del día seleccionado | Aparece al tocar un día, debajo del calendario |

**Vista Mensual:**
- Grid 7 columnas × semanas del mes
- Días con tareas marcados con punto(s) de color según miembro responsable
- Día actual destacado

**Vista Semanal:**
- Columnas por día, filas por hora (tipo Google Calendar)
- Bloques de tareas arrastrables (post-MVP)

---

### 4.7 Gastos – Lista

**Propósito:** Ver el historial de gastos del hogar y los balances.

**Componentes:**

| Componente | Descripción |
|---|---|
| Resumen de balance | Cards horizontales: "Te deben X" / "Debes X" |
| Chips de filtro | Por categoría / Por miembro / Por mes |
| Lista de gastos | Tarjetas de gasto |
| FAB | "+" para crear gasto |

**Tarjeta de Gasto (componente reutilizable):**
- Icono de categoría (comida, servicios, limpieza, etc.)
- Descripción del gasto
- Quién pagó (avatar)
- Monto total + división
- Fecha

**Estados:**
- **Vacío:** "Registra el primer gasto del hogar"
- **Balance saldado:** "¡Todos están al día! 🎉"

---

### 4.8 Gastos – Crear / Editar Gasto

**Propósito:** Formulario para registrar un nuevo gasto compartido.

**Campos:**

| Campo | Tipo | Requerido |
|---|---|---|
| Descripción | Text input | ✅ |
| Monto total | Numeric input con símbolo de moneda | ✅ |
| Categoría | Selector con iconos (grilla) | ✅ |
| Pagador | Selector de miembro | ✅ |
| Participantes | Multi-select de miembros | ✅ |
| División | Toggle: Equitativa / Personalizada | ✅ |
| División personalizada | Si "Personalizada": campo de monto por participante | condicional |
| Fecha | Date picker (default: hoy) | ✅ |
| Nota | Textarea | ❌ |

**Categorías disponibles (con iconos):**
Comida · Supermercado · Servicios · Limpieza · Transporte · Entretenimiento · Salud · Otro

---

### 4.9 Gastos – Balances

**Propósito:** Ver claramente quién le debe a quién.

**Componentes:**
- Lista de deudas simplificadas (A → B: $X)
- Botón "Registrar pago" por deuda
- Historial de pagos realizados

---

### 4.10 Perfil

**Propósito:** Configuración personal y del hogar.

**Secciones:**

| Sección | Contenido |
|---|---|
| Info personal | Avatar, nombre, email |
| Estadísticas personales | Tareas completadas, racha actual, puntuación |
| Mi Hogar | Nombre del hogar, código de invitación, lista de miembros |
| Gestión de miembros | Ver rol, cambiar rol (admin), eliminar miembro |
| Notificaciones | Toggles por tipo de notificación |
| Configuración | Tema (claro/oscuro), idioma, moneda |
| Cerrar sesión | Botón destructivo |

---

### 4.11 Notificaciones

**Pantalla:** Lista de notificaciones con separación por fecha (Hoy / Ayer / Esta semana)

**Tipos de notificación:**
- Nueva tarea asignada
- Tarea próxima a vencer (24h antes)
- Tarea vencida
- Nuevo comentario en tarea propia
- Gasto registrado que te incluye
- Miembro se unió al hogar
- Resumen diario (morning digest)

---

## 5. Componentes UI – Librería de Diseño

> Definir como componentes en Figma para reutilización.

### Componentes principales

| Componente | Variantes |
|---|---|
| **TaskCard** | Default / Completada / Vencida / Compacta |
| **ExpenseCard** | Default / Pagada / Pendiente |
| **MemberAvatar** | Small (24pt) / Medium (40pt) / Large (56pt) + grupo de avatares |
| **PriorityBadge** | Alta (rojo) / Media (naranja) / Baja (verde) |
| **StatusChip** | Pendiente / En progreso / Completada / Vencida |
| **CategoryIcon** | 8 categorías de gastos |
| **BottomNav** | 5 estados (activo/inactivo por tab) |
| **FAB** | Simple (+) / Expandible (múltiples acciones) |
| **EmptyState** | Ilustración + título + subtítulo + CTA opcional |
| **LoadingState** | Skeleton screens por sección |
| **ErrorState** | Icono + mensaje + botón de reintentar |
| **InputField** | Default / Focus / Error / Disabled |
| **Modal / Bottom Sheet** | Confirmación / Selección / Formulario parcial |
| **DatePicker** | Inline calendar + input field |
| **Toggle** | On / Off |
| **SegmentedControl** | 2 o 3 opciones |

---

## 6. Flujos de usuario – Para prototipado

### Flujo 1: Crear una tarea
```
Home → FAB (+) → Modal de acción rápida → "Nueva tarea"
→ Pantalla Crear Tarea → Completar campos → Guardar
→ Vuelta a Lista de Tareas (tarea aparece al tope) + Toast "Tarea creada"
→ [Sistema envía notificación al responsable asignado]
```

### Flujo 2: Completar una tarea
```
Home o Lista de Tareas → Tarjeta de tarea → Tap en checkbox
→ Animación de completado → Estado actualizado
→ [Si tenía recurrencia → nueva instancia generada automáticamente]
```

### Flujo 3: Registrar un gasto
```
Gastos → FAB (+) → Pantalla Crear Gasto → Completar campos
→ Seleccionar participantes → Definir división
→ Guardar → Vista de Balance actualizada + Toast "Gasto registrado"
→ [Notificación a participantes]
```

### Flujo 4: Crear un hogar e invitar miembros
```
Onboarding → "Crear nuevo hogar" → Nombre del hogar → Guardar
→ Pantalla de invitación → Compartir código / enlace
→ Dashboard (vacío) con prompt de onboarding: "Invita a tu primer miembro"
```

### Flujo 5: Unirse a un hogar
```
Onboarding → "Unirme a un hogar" → Ingresar código
→ Previsualización del hogar (nombre + miembros actuales)
→ Confirmar → Dashboard del hogar
```

---

## 7. Estados de pantalla – Obligatorios en wireframes

Cada pantalla de lista o contenido debe mostrar estos estados:

| Estado | Descripción |
|---|---|
| **Vacío** | Primera vez o sin datos. Ilustración + mensaje + CTA |
| **Cargando** | Skeleton screen (no spinner genérico) |
| **Con datos** | Estado normal con contenido |
| **Error de red** | Mensaje + botón "Reintentar" |
| **Sin conexión** | Banner informativo de modo offline |

---

## 8. Notificaciones – Comportamiento

| Trigger | Timing | Tipo |
|---|---|---|
| Tarea asignada | Inmediato | Push |
| Tarea próxima a vencer | 24h antes | Push |
| Tarea vencida | Al momento del vencimiento | Push |
| Nuevo comentario | Inmediato | Push |
| Gasto que te incluye | Inmediato | Push |
| Resumen diario | 8:00 AM (configurable) | Push |

---

## 9. Modelo de datos – Alto nivel

```
Usuario
├── id, nombre, email, avatarUrl, rol

Hogar
├── id, nombre, código_invitación
└── miembros[] → Usuario

Tarea
├── id, título, descripción
├── responsable_id → Usuario
├── hogar_id → Hogar
├── fecha_límite, prioridad, estado
├── recurrencia { frecuencia, días, fecha_fin }
├── etiquetas[]
├── checklist[] { texto, completado }
├── comentarios[] { autor_id, texto, timestamp }
└── actividad[] { tipo, actor_id, timestamp }

Gasto
├── id, descripción, monto, categoría
├── pagador_id → Usuario
├── hogar_id → Hogar
├── fecha, nota
└── participantes[] { usuario_id, monto_asignado, pagado }

Etiqueta
└── id, nombre, color, hogar_id
```

---

## 10. KPIs del MVP

| KPI | Objetivo inicial |
|---|---|
| Usuarios activos diarios (DAU) | ≥30% de registrados |
| Retención semanal | ≥40% |
| Tareas creadas / usuario / semana | ≥3 |
| % tareas completadas a tiempo | ≥60% |
| Gastos registrados / hogar / semana | ≥2 |
| Tiempo promedio de sesión | ≥3 min |

---

## 11. Requisitos técnicos

| Capa | Tecnología |
|---|---|
| Plataforma | Android (API 26+) |
| Lenguaje | Kotlin |
| Arquitectura | MVVM + Clean Architecture |
| Backend | Firebase (Firestore + Auth + FCM + Storage) |
| Notificaciones | Firebase Cloud Messaging |
| Offline | Room DB (caché local) + sync al reconectar |
| Autenticación | Email/password + Google Sign-In |

---

## 12. Roadmap

### Fase 1 – MVP (actual)
- Gestión de tareas + recurrencia
- Sistema de miembros y roles
- Calendario
- Control de gastos compartidos
- Notificaciones push
- Dashboard
- Modo offline básico
- Publicación en Google Play Store

### Fase 2 – Crecimiento
- Gamificación (puntos, logros, ranking)
- Insights de hábitos del hogar
- Checklists predefinidos (plantillas)
- Búsqueda global
- Equilibrio de carga mejorado

### Fase 3 – Escala
- Sugerencias inteligentes (ML)
- Predicción de gastos
- Integración con asistentes virtuales
- Automatización de rutinas
- Reportes avanzados exportables

---

## 13. Consideraciones de seguridad

- Autenticación por Firebase Auth (tokens JWT)
- Reglas de Firestore: usuarios solo acceden a datos de su hogar
- No se almacenan datos de pago (solo registros de gastos)
- Datos sensibles cifrados en tránsito (HTTPS)
- Invitación al hogar con expiración de código (24-72h configurable)

---

## 14. Pantallas prioritarias para wireframes (Figma)

Orden sugerido de diseño:

1. `Home / Dashboard`
2. `Lista de Tareas`
3. `Crear Tarea`
4. `Detalle de Tarea`
5. `Calendario (vista mensual)`
6. `Lista de Gastos + Balances`
7. `Crear Gasto`
8. `Perfil + Gestión de Miembros`
9. `Onboarding (Bienvenida + Crear/Unirse a hogar)`
10. `Notificaciones`

---

*— HomeFlow PRD v1.1 | Para uso interno de diseño y desarrollo —*
