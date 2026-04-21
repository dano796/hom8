<div align="center">
  <picture>
    <source media="(prefers-color-scheme: light)" srcset="preview/hom8_gh_black.svg">
    <source media="(prefers-color-scheme: dark)" srcset="preview/hom8_gh_white.svg">
    <img src="preview/hom8_gh_black.svg" alt="Hom8 Logo" width="96">
  </picture>
  <h1>Hom8</h1>
  <p><em>Aplicación Android para la gestión de tareas y gastos en el hogar</em></p>
</div>

## Características

- **Autenticación:** Acceso seguro y gestión de perfiles para cada miembro del hogar.
- **Gestión de tareas:** Creación, asignación y seguimiento de los quehaceres.
- **Control de gastos:** Registro y distribución de los gastos comunes.

## Stack Tecnológico

- Kotlin + Coroutines
- MVVM + LiveData / StateFlow
- Room Database (local-first)
- Firebase Auth + Firestore
- Hilt (inyección de dependencias)
- Navigation Component

## Arquitectura

El proyecto utiliza **Clean Architecture** y **MVVM**, dividiéndose en tres capas:

- **Dominio:** Reglas de negocio independientes del framework.
- **Datos:** Gestión de repositorios locales (Room) y remotos (Firebase).
- **Presentación:** Interfaz de usuario reactiva mediante `StateFlow` y `ViewModels`.

## Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/dano796/hom8.git
```

### 2. Configurar Firebase

Este proyecto requiere un archivo `google-services.json` que **no está incluido en el repositorio** por razones de seguridad. Para obtenerlo:

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Crea un proyecto (o usa uno existente)
3. Registra una aplicación Android con el nombre de paquete `com.hom8.app`
4. Descarga `google-services.json`
5. Agrega el archivo en la carpeta `app/`:

```
app/
└── google-services.json
```

6. En Firebase Console, habilita:

   - **Authentication** → Email/Password (opcional Google Auth)
   - **Firestore Database**

### 3. Compilar y ejecutar

Abre el proyecto en Android Studio y ejecútalo en un dispositivo o emulador (SDK 24 o superior).

---

### Desarrollado por

- Daniel Ortiz Aristizábal - 000186841
- Emanuel Londoño Osorio - 000507237
- Felipe Torres Montoya - 000524913

### Aplicaciones Móviles - Universidad Pontificia Bolivariana
