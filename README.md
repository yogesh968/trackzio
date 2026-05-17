<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"/>
<img src="https://img.shields.io/badge/Min%20SDK-26-orange?style=for-the-badge"/>
<img src="https://img.shields.io/badge/API-No%20Key%20Required-success?style=for-the-badge"/>

<br/><br/>

# 🌍 WeatherSnap

### A production-quality Android weather app built with modern architecture, elite dark UI, and zero API keys required.

<br/>

</div>

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔍 **Live City Search** | Debounced autocomplete with 400ms delay and in-memory caching |
| 🌡️ **Real-time Weather** | Temperature, condition, humidity, wind speed, pressure |
| 🤔 **Feels Like** | Steadman heat-index formula derived from temp + humidity |
| 👁️ **Visibility** | Derived from weather code — Excellent to Poor |
| 💨 **Wind Description** | Calm → Light breeze → Moderate → Strong → Storm force |
| ☀️ **UV Risk** | Estimated from weather code + temperature |
| 😌 **Comfort Index** | 0–100 score with animated progress bar |
| 💡 **Smart Suggestion** | One-line actionable tip (e.g. "Bring an umbrella") |
| 📸 **Custom Camera** | Full CameraX integration — no external intent |
| 🗜️ **Image Compression** | Auto-compress to JPEG 75% with size display |
| 📋 **Weather Reports** | Save reports with photo, notes, and full weather snapshot |
| 💾 **Draft Recovery** | Survives rotation AND process death via Room draft table |
| 🔄 **Refresh** | Re-fetch weather for the current city with one tap |
| 🗑️ **Clear All** | Delete all saved reports with confirmation dialog |
| 📴 **Offline Access** | Saved reports available without internet |
| 🌙 **Elite Dark Theme** | Obsidian background, amber accent, premium typography |

---

## 📱 Screens

```
🏠 Weather Screen      →  Search city, view live weather, comfort index, suggestion
📝 Create Report       →  Capture photo, add notes, save with weather snapshot
📷 Camera Screen       →  Custom CameraX viewfinder with shutter button
📂 Saved Reports       →  Browse all saved reports, clear all option
```

---

## 🏗️ Architecture

This app follows **Clean Architecture** with **MVVM** pattern, strictly separating concerns across three layers.

```
com.weathersnap
│
├── 📡 data/
│   ├── remote/
│   │   ├── api/            GeocodingApi.kt · WeatherApi.kt
│   │   └── dto/            GeocodingDto.kt · WeatherDto.kt
│   ├── local/
│   │   ├── dao/            ReportDao.kt · DraftDao.kt
│   │   ├── entity/         ReportEntity.kt · DraftReportEntity.kt
│   │   └── WeatherSnapDatabase.kt
│   └── repository/
│       ├── CityRepository.kt
│       ├── WeatherRepository.kt
│       ├── ReportRepository.kt
│       └── Mappers.kt
│
├── 🧠 domain/
│   └── model/              City · WeatherSnapshot · WeatherReport · Result<T>
│
├── 🎨 ui/
│   ├── weather/            WeatherScreen · WeatherViewModel · WeatherUiState
│   ├── report/             CreateReportScreen · CreateReportViewModel
│   ├── camera/             CameraScreen · CameraViewModel
│   ├── savedreports/       SavedReportsScreen · SavedReportsViewModel
│   ├── components/         WeatherCard
│   ├── theme/              Color · Type · Theme
│   └── SharedWeatherViewModel.kt
│
├── 🧭 navigation/          NavHost · Screen
├── 💉 di/                  NetworkModule · DatabaseModule
└── 🛠️ utils/               ImageCompressor · WeatherConditionMapper · Extensions
```

### Key Patterns

- **Repository Pattern** — ViewModels never touch APIs or DAOs directly. All data flows through repositories.
- **Sealed `Result<T>`** — Every async operation returns `Success`, `Error`, or `Loading`. No raw exceptions in the UI layer.
- **StateFlow UI State** — Each screen has a sealed `UiState` driven by `StateFlow`, collected with `collectAsStateWithLifecycle()`.
- **Shared ViewModel** — `SharedWeatherViewModel` is scoped to the NavGraph, passing the immutable weather snapshot from Weather → CreateReport without re-fetching.

---

## 🌐 API

No API key required. Uses **[Open-Meteo](https://open-meteo.com/)** — a free, open-source weather API.

| Endpoint | URL |
|---|---|
| 🔍 Geocoding | `https://geocoding-api.open-meteo.com/v1/search` |
| 🌤️ Weather | `https://api.open-meteo.com/v1/forecast` |

**Fields used from the API:**

```
temperature_2m · relative_humidity_2m · wind_speed_10m · surface_pressure · weather_code
```

**Everything else is derived locally** — feels like, visibility, wind description, UV risk, comfort index, and suggestion banner. That means just **2 API calls** per city search, with no extra endpoints.

City suggestions are **debounced at 400ms** and **cached in-memory** per query string.

---

## 💾 Draft Recovery

> **Scenario:** User selects weather → opens Create Report → captures photo → types notes → rotates device or backgrounds app before saving.

### Solution: Singleton Draft Row in Room

A `draft_report` table with a single row (`id = 1`) persists the in-progress report state to disk automatically.

| Approach | Survives Rotation | Survives Process Death | Notes |
|---|:---:|:---:|---|
| `SavedStateHandle` | ✅ | ❌ | Limited to primitives/Parcelable |
| **Room draft table** | ✅ | ✅ | Full structured data, cleared on save/discard |
| DataStore | ✅ | ✅ | Awkward for structured report data |

**How it works:**

1. Every notes change and image capture auto-saves to the draft table
2. On `initWithWeather()`, if a draft exists for the same city, notes + image path are restored
3. The weather snapshot is **always** taken from `SharedWeatherViewModel` — never from the draft, preventing stale data
4. On **save** → draft cleared, temp camera file deleted
5. On **back/discard** → draft cleared, compressed image deleted
6. `saveReport()` is called exactly once — no duplicate reports possible

---

## 🗜️ Image Compression

`ImageCompressor` runs entirely on `Dispatchers.IO`:

```
1. Decode with inJustDecodeBounds  →  get dimensions without loading pixels
2. Calculate inSampleSize          →  downsample large images efficiently
3. Scale to max 1080px             →  on the longest side
4. Compress to JPEG 75%            →  write to app's files directory
5. Display original vs compressed  →  shown in the UI (e.g. 4.2 MB → 380 KB)
```

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Kotlin | 2.0.21 |
| UI | Jetpack Compose + Material 3 | BOM 2024.09.03 |
| Architecture | MVVM + Clean Architecture | — |
| DI | Hilt | 2.52 |
| Async | Coroutines + StateFlow | 1.9.0 |
| Navigation | Navigation Compose | 2.8.2 |
| Networking | Retrofit + Gson + OkHttp | 2.11.0 / 4.12.0 |
| Local DB | Room | 2.6.1 |
| Camera | CameraX | 1.3.4 |
| Image Loading | Coil | 2.7.0 |
| Code Gen | KSP | 2.0.21-1.0.27 |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio **Hedgehog** or later
- JDK is bundled inside Android Studio — no separate install needed
- Android device or emulator with **API 26+**

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/yogesh968/trackzio.git

# 2. Open in Android Studio
# File → Open → select the cloned folder

# 3. Let Gradle sync complete (~2–5 min on first run)

# 4. Run on a device or emulator
# Click the green ▶ Run button
```

> **No API keys, no `.env` files, no Firebase setup.** The app works out of the box.

### First Launch

- Grant **camera permission** when prompted to use the photo capture feature
- Camera permission is optional — reports can be saved without a photo

---

## 📁 Project Structure at a Glance

```
WeatherSnap/
├── app/
│   ├── src/main/
│   │   ├── java/com/weathersnap/   ← All Kotlin source
│   │   ├── res/                    ← Resources, icons, themes
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          ← Version catalog
└── README.md
```

---

## 🔒 Permissions

| Permission | Required | Reason |
|---|:---:|---|
| `INTERNET` | ✅ | Fetch weather and city data from Open-Meteo |
| `CAMERA` | ❌ Optional | Capture weather photos in Create Report |
| `READ_EXTERNAL_STORAGE` | ❌ | API ≤ 32 only, for image access |
| `WRITE_EXTERNAL_STORAGE` | ❌ | API ≤ 29 only, for image saving |

---

## 👨‍💻 Author

**Yogesh Kumar**
- GitHub: [@yogesh968](https://github.com/yogesh968)

---

<div align="center">

Built with ❤️ using Kotlin + Jetpack Compose

</div>
