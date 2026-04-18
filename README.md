# Hom8

Aplicación Android para gestión de tareas y gastos del hogar, construida con Kotlin MVVM, Room, Firebase y Hilt.

## Stack Tecnológico

- Kotlin + Coroutines
- MVVM + LiveData / StateFlow
- Room Database (local-first)
- Firebase Auth + Firestore + Storage
- Hilt (inyección de dependencias)
- Navigation Component

## Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/dano796/hom8.git
```

### 2. Configurar Firebase

Este proyecto requiere un archivo `google-services.json` que **no está incluido en el repositorio** por razones de seguridad.

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Crea un proyecto (o usa uno existente)
3. Registra una aplicación Android con el nombre de paquete `com.homeflow.app`
4. Descarga `google-services.json`
5. Colócalo en la carpeta `app/`:

```
app/
└── google-services.json   ← colócalo aquí
```

6. En Firebase Console, habilita:
   - **Authentication** → Email/Password
   - **Firestore Database**
   - **Storage**

### 3. Compilar y ejecutar

Abre el proyecto en Android Studio y ejecútalo en un dispositivo o emulador (SDK mínimo 24).

## Archivos NO incluidos en este repositorio

| Archivo                      | Razón                                                                                   |
| ---------------------------- | ---------------------------------------------------------------------------------------- |
| `app/google-services.json` | Contiene claves API de Firebase — genera el tuyo desde Firebase Console                 |
| `local.properties`         | Contiene la ruta local de tu Android SDK — generado automáticamente por Android Studio |
| `*.keystore` / `*.jks`   | Claves de firma de la app — nunca las compartas                                         |
| `keystore.properties`      | Configuración de firma con contraseñas                                                 |
| `build/`                   | Salida compilada                                                                         |

## Licencia

[![Licence](https://img.shields.io/github/license/Ileriayo/markdown-badges?style=for-the-badge)](./LICENSE)
