<div align="center">

# 🏆 Álbum Mundial
### Aplicación Android Nativa — Experiencia de Colección Premium

<br>

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-00C853?style=for-the-badge)

<br>

> Combina la nostalgia de llenar un álbum de fútbol con mecánicas modernas de gamificación, animaciones AAA y un ecosistema de minijuegos diseñado para generar *engagement* y recompensas.

</div>

---

## 📸 Screenshots

| Splash Screen | Zona Arcade | Tanda de Penales |
|:---:|:---:|:---:|
| *(próximamente)* | *(próximamente)* | *(próximamente)* |

---

## 🧠 Arquitectura de Datos: Pipeline de Web Scraping

El núcleo de esta aplicación está alimentado por **datos reales y precisos**. La base de datos de jugadores —nombres, nacionalidades, estadísticas, equipos y fotografías— no fue introducida manualmente.

Se desarrolló un pipeline de **Web Scraping automatizado** que extrae, limpia y estructura información directamente desde bases de datos deportivas en la web, inyectándola en **Firebase Firestore** como backend en tiempo real.

| Principio | Descripción |
|---|---|
| 📈 **Escalabilidad** | Añade cientos de jugadores de nuevas ligas con mínimo esfuerzo |
| 🎯 **Precisión** | Estadísticas actualizadas extraídas desde las fuentes originales |
| 🗂️ **Estructura** | Datos procesados e inyectados directamente en Firestore |

---

## ✨ Características Principales

### 📖 Álbum y Colección

- **Álbum Principal** — Interfaz inmersiva para visualizar las cartas obtenidas, organizadas por selecciones y equipos.
- **Apertura de Sobres** — Experiencia de *unboxing* con animaciones dinámicas que revelan nuevos jugadores.
- **Almacén / Inventario** — Sistema para gestionar cartas repetidas y hacer seguimiento del progreso.

---

### 🎰 Casino VIP — Zona Arcade

Hub de entretenimiento diseñado con *Glassmorphism* y luces de neón donde los usuarios apuestan sus monedas en minijuegos:

<table>
<tr>
<td width="33%" align="center">

**⚽ Tanda de Penales**

Minijuego estilo estadio nocturno. Enfrenta al portero con físicas de deslizamiento, sistema de rachas 🔥 y multiplicadores de apuesta progresivos.

</td>
<td width="33%" align="center">

**🟩 Wordle Futbolero**

Adivina al jugador oculto en 6 intentos usando pistas basadas en sus características reales.

</td>
<td width="33%" align="center">

**🔮 Plinko Stake**

Deja caer la bola en la pirámide de la suerte y apunta al multiplicador **x100**.

</td>
</tr>
</table>

---

### 🎨 UI/UX Premium

| Elemento | Detalle |
|---|---|
| ✨ **Animaciones fluidas** | Transiciones cinematográficas con `Crossfade`, entradas/salidas y *idle animations* |
| 🌌 **Fondo global dinámico** | Malla tecnológica con iluminación radial persistente entre pantallas |
| 💊 **Dopamine Bottom Nav** | Barra flotante con respuestas hápticas y escalado visual |
| 🎵 **Music Player Widget** | Control de banda sonora (`.mp3` locales) accesible desde cualquier pantalla |

---

## 🛠️ Stack Tecnológico

```
📱 Lenguaje         →  Kotlin
🎨 UI Toolkit       →  Jetpack Compose (UI Declarativa)
☁️  Backend / DB     →  Firebase Firestore (Cloud NoSQL)
🖼️  Imágenes         →  Coil (carga asíncrona optimizada por red)
🎞️  Animaciones      →  Compose Animation APIs
                        (AnimatedVisibility, animateDpAsState, InfiniteTransition)
🏗️  Arquitectura     →  MVVM (Model-View-ViewModel)
```

---

## 🚀 Instalación

### 1. Clona el repositorio

```bash
git clone https://github.com/TuUsuario/AlbumMundial.git
cd AlbumMundial
```

### 2. Abre en Android Studio

Importa el proyecto desde **File → Open** y espera a que Gradle sincronice las dependencias.

### 3. Configura Firebase

> ⚠️ El archivo `google-services.json` **no está incluido** en este repositorio por razones de seguridad.

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Añade una aplicación Android con el package name `mx.bruko`.
3. Descarga `google-services.json` y colócalo dentro de la carpeta `app/`.

### 4. Ejecuta

Sincroniza Gradle y ejecuta la app en un emulador o dispositivo físico.

---

## 📁 Estructura del Proyecto

```
AlbumMundial/
├── app/
│   ├── src/main/
│   │   ├── java/mx/bruko/
│   │   │   ├── ui/          # Pantallas y componentes Compose
│   │   │   ├── viewmodel/   # ViewModels (MVVM)
│   │   │   ├── data/        # Repositorios y modelos
│   │   │   └── utils/       # Helpers y extensiones
│   │   └── res/
│   │       └── raw/         # Archivos .mp3 de banda sonora
│   └── google-services.json # ← NO incluido (ver instrucciones arriba)
└── scraper/                 # Pipeline de Web Scraping
```

---

<div align="center">

Desarrollado por **Bruno Yael Bravo Olmos**

</div>
