# WeatherSnap

A production-quality Android weather app built with modern Android architecture.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Async | Coroutines + StateFlow |
| Navigation | Navigation Compose |
| Networking | Retrofit + Gson + OkHttp |
| Local DB | Room |
| Camera | CameraX |
| Image Loading | Coil |

## Setup Instructions

1. Clone the repository
2. Open in Android Studio Hedgehog or later
3. Sync Gradle dependencies
4. Run on a physical device or emulator with API 26+
5. Grant camera permission when prompted

No API keys required — uses Open-Meteo (free, no auth).

## Architecture

```
com.weathersnap
├── data
│   ├── remote/         # Retrofit APIs + DTOs
│   ├── local/          # Room DB, DAOs, Entities
│   └── repository/     # Repository implementations + Mappers
├── domain/model/       # Pure domain models (City, WeatherSnapshot, WeatherReport, Result)
├── ui
│   ├── weather/        # WeatherScreen + WeatherViewModel
│   ├── report/         # CreateReportScreen + CreateReportViewModel
│   ├── camera/         # CameraScreen + CameraViewModel (CameraX)
│   ├── savedreports/   # SavedReportsScreen + SavedReportsViewModel
│   ├── components/     # Shared Composables (WeatherCard)
│   └── theme/          # Material 3 theme, colors, typography
├── navigation/         # NavHost + Screen sealed class
├── di/                 # Hilt modules (NetworkModule, DatabaseModule)
└── utils/              # ImageCompressor, WeatherConditionMapper, Extensions
```

### Key Patterns

- **Repository Pattern**: All data access goes through repositories. ViewModels never touch APIs or DAOs directly.
- **Sealed Result**: `Result<T>` wraps all async operations — `Success`, `Error`, `Loading`.
- **StateFlow UI State**: Each screen has a sealed `UiState` class driven by `StateFlow`.
- **Shared ViewModel**: `SharedWeatherViewModel` is scoped to the NavGraph to pass the immutable weather snapshot between Weather → CreateReport screens without re-fetching.

## Developer Judgment Challenge: Draft Recovery

### Scenario
User selects weather → opens Create Report → captures photo → types notes → rotates device or backgrounds app before saving.

### Approach: Draft Table in Room

A `draft_report` table (singleton row, id=1) persists the in-progress report state to disk.

**Why Room draft table over alternatives:**

| Approach | Tradeoff |
|----------|----------|
| `SavedStateHandle` | Survives rotation but NOT process death. Limited to Parcelable/primitive types. |
| Room draft table ✅ | Survives rotation AND process death. Stores full structured data. Cleared on save/discard. |
| DataStore | Good for simple key-value, but awkward for structured report data. |

**Behavior:**
1. Every notes change and image capture auto-saves to the draft table.
2. On `initWithWeather()`, if a draft exists for the same city, it restores notes + image path.
3. The weather snapshot is **always** taken from the `SharedWeatherViewModel` (the original selected weather) — never from the draft. This prevents stale/updated weather from silently replacing the user's original selection.
4. On save: draft is cleared, temp camera file is deleted.
5. On back/discard: draft is cleared, compressed image file is deleted.
6. No duplicate reports: `saveReport()` is only called once from the Save button; the draft is cleared immediately after.

## Image Compression

`ImageCompressor` runs on `Dispatchers.IO`:
1. Decodes with `inJustDecodeBounds` to get dimensions without loading pixels.
2. Calculates `inSampleSize` to downsample large images.
3. Scales to max 1080px on the longest side.
4. Compresses to JPEG at 75% quality.
5. Shows original vs compressed size in the UI.

## API

- **Geocoding**: `https://geocoding-api.open-meteo.com/v1/search`
- **Weather**: `https://api.open-meteo.com/v1/forecast`
- Both are free, no API key required.
- City suggestions are debounced (400ms) and cached in-memory per query.

## Features

- Live city search with debounced autocomplete
- Animated suggestion dropdown
- Weather details: temperature, condition, humidity, wind speed, pressure
- Custom CameraX camera (no external intent)
- Real image compression with size display
- Draft recovery across rotation and process death
- Room-persisted reports with full weather snapshot
- Smooth animated transitions between states
- Material 3 dark/light mode support
- Offline access to saved reports
- Debug-only OkHttp network logging
