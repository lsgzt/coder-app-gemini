# 📱 PocketDev — Native Android Coding Workspace for Students

> **Code anywhere, learn everything.** A full-featured mobile IDE for Android that lets students write, execute, and improve code directly from their smartphones — no laptop required.

---

## 🚀 Features

### ✏️ Multi-Language Code Editor
- Professional syntax highlighting for **8 languages**: Python, JavaScript, HTML, CSS, Java, C++, Kotlin, JSON
- Real-time tokenized syntax coloring (VSCode dark theme palette)
- Line numbers panel
- Find & Replace functionality
- Horizontal scroll for long lines
- Auto-save every 30 seconds
- Configurable font size (10–22sp) and tab size (2/4/8 spaces)

### ⚡ Triple On-Device Execution Engines

| Language | Engine | Notes |
|---|---|---|
| **Python** | Chaquopy 14.0.2 | Python 3.11, captures stdout/stderr |
| **JavaScript** | Rhino 1.7.14 | ES6 support, `console.log()` capture |
| **HTML** | Android WebView | Full HTML5/CSS/JS rendering |

- 10-second execution timeout with graceful handling
- Beginner-friendly error messages
- Execution time display
- Console output panel

### 🤖 Groq-Powered AI Features
- **Fix Bug** — AI identifies and fixes code issues
- **Explain Code** — Step-by-step beginner-friendly explanation
- **Improve Code** — Best practices & optimization suggestions
- One-click "Apply Code" to insert AI-corrected code
- Loading indicators, offline detection, rate-limit handling
- API key encrypted with `EncryptedSharedPreferences` (AES-256)

### 📋 Project Management
- SQLite (Room) local storage for all projects
- Create, open, save, duplicate, delete projects
- Language badges and modification dates
- Search/filter project list
- Export code as `.py`, `.js`, `.html`, etc.
- Import code files from device storage

### 📚 Code Examples Library
- **15 built-in examples** across Python, JavaScript, and HTML
- Python: Hello World, Loops, Functions, Lists/Dicts, Classes/OOP
- JavaScript: Variables, Functions, Arrays/Objects, Promises/Async, Classes
- HTML: Basic Structure, Forms, CSS Styling, JavaScript Integration, Responsive Layout
- Preview before loading, with full syntax preview

### ⚙️ Settings
- Dark / Light / System theme
- Font size slider
- Tab size selection
- Line numbers toggle
- Word wrap toggle
- Auto-save toggle
- Autocomplete toggle
- API key management with masking
- Reset to defaults

---

## 🛠️ Technical Architecture

```
┌─────────────────────────────────────────┐
│           UI Layer (Jetpack Compose)     │
│  EditorScreen │ ProjectsScreen │ etc.   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│        ViewModel Layer (MVVM)            │
│  EditorViewModel │ SettingsViewModel     │
└──────────┬────────────────┬─────────────┘
           │                │
┌──────────▼─────┐  ┌───────▼──────────────┐
│  Repository    │  │  Execution Manager    │
│  (Room DB)     │  │  Python │ JS │ HTML   │
└────────────────┘  └──────────────────────┘
           │
┌──────────▼──────────┐
│  Groq API (Retrofit) │
│  llama-3.3-70b       │
└─────────────────────┘
```

**Tech Stack:**
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM + StateFlow
- **Database:** Room (SQLite)
- **Preferences:** DataStore
- **Networking:** Retrofit2 + OkHttp3
- **Security:** EncryptedSharedPreferences
- **Python Runtime:** Chaquopy 14.0.2 (Python 3.11)
- **JavaScript Runtime:** Mozilla Rhino 1.7.14
- **HTML Runtime:** Android WebView

---

## 📋 Requirements

- **Android:** 8.0+ (API 26+) — supports up to Android 14 (API 34)
- **RAM:** 2GB+ recommended (Python runtime needs memory)
- **Storage:** ~100MB (includes Python runtime)
- **Internet:** Required only for AI features (Groq API)

---

## 🔑 Getting a Groq API Key

The Groq API is **free** — no credit card required for the free tier.

1. Visit [console.groq.com](https://console.groq.com)
2. Create a free account or sign in with Google/GitHub
3. Navigate to **API Keys** in the left sidebar
4. Click **"Create API Key"**
5. Give it a name (e.g., "PocketDev")
6. Copy the key (starts with `gsk_`)
7. Open PocketDev → Settings → Set your API key

> 🔒 The key is stored encrypted on your device using AES-256-GCM.

**Models used:** `llama-3.3-70b-versatile` (primary)

---

## 🏗️ Building the APK

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK with API 26–34
- Python 3 (for Chaquopy build step)

### Steps

```bash
# 1. Clone the project
git clone <repo-url>
cd PocketDev

# 2. Open in Android Studio
# File → Open → select PocketDev folder

# 3. Sync Gradle
# Android Studio will auto-sync; if not: File → Sync Project with Gradle Files

# 4. Build Debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk

# 5. Build Release APK (signed with debug key for testing)
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Installing the APK

```bash
# Via ADB (Android Debug Bridge)
adb install app/build/outputs/apk/debug/app-debug.apk

# Or copy the APK file to your Android device and open it
# (Enable "Install from unknown sources" in Settings → Security)
```

---

## 📁 Project Structure

```
PocketDev/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pocketdev/app/
│   │   │   │   ├── activities/          # SplashActivity, MainActivity
│   │   │   │   ├── api/
│   │   │   │   │   ├── models/          # ChatRequest, ChatResponse
│   │   │   │   │   └── service/         # GroqApiService, RetrofitClient
│   │   │   │   ├── data/
│   │   │   │   │   ├── db/              # AppDatabase, ProjectDao, Converters
│   │   │   │   │   └── models/          # Project, Language, ExecutionResult
│   │   │   │   ├── editor/              # AutocompleteEngine, SyntaxHighlighter
│   │   │   │   ├── execution/           # ExecutionManager, PythonEngine, JSEngine, HtmlEngine
│   │   │   │   ├── repository/          # ProjectRepository, GroqRepository
│   │   │   │   ├── ui/
│   │   │   │   │   ├── navigation/      # AppNavigation (bottom nav)
│   │   │   │   │   ├── screens/         # EditorScreen, ProjectsScreen, ExamplesScreen, SettingsScreen
│   │   │   │   │   └── theme/           # Theme.kt, Typography.kt
│   │   │   │   ├── utils/               # SecureStorage, PreferencesManager, FileUtils, NetworkUtils, CodeExamples
│   │   │   │   ├── viewmodels/          # EditorViewModel, SettingsViewModel
│   │   │   │   └── PocketDevApplication.kt
│   │   │   ├── python/
│   │   │   │   └── code_runner.py       # Chaquopy Python execution module
│   │   │   ├── res/
│   │   │   │   ├── drawable/            # App icons (vector)
│   │   │   │   ├── mipmap-*/            # Launcher icons
│   │   │   │   ├── values/              # strings.xml, colors.xml, themes.xml
│   │   │   │   └── xml/                 # File paths, backup rules
│   │   │   └── AndroidManifest.xml
│   │   └── test/                        # Unit tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml               # Dependency catalog
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## ✅ Success Criteria Checklist

### Execution
- [x] Python executes and captures stdout/stderr (Chaquopy + `code_runner.py`)
- [x] JavaScript executes with `console.log()` capture (Rhino ES6)
- [x] HTML renders with CSS and embedded JavaScript (WebView)
- [x] All three engines handle errors gracefully
- [x] 10-second timeout protection
- [x] Non-executable languages show clear message

### Syntax Highlighting
- [x] Python — keywords, strings, comments, numbers, functions
- [x] JavaScript — keywords, strings, comments, ES6 features
- [x] HTML — tags, attributes, strings, comments
- [x] CSS — properties, values, selectors, colors
- [x] Java — keywords, types, annotations
- [x] C++ — keywords, preprocessor, STL types
- [x] Kotlin — keywords, types, lambdas
- [x] JSON — keys, strings, values

### Autocomplete
- [x] Python — 70+ completions: keywords, builtins, methods
- [x] JavaScript — 80+ completions: keywords, array methods, DOM
- [x] HTML — 60+ completions: tags, attributes
- [x] CSS — 60+ completions: properties, values
- [x] Java — 50+ completions: keywords, types, methods
- [x] C++ — 50+ completions: keywords, STL, streams
- [x] Kotlin — 70+ completions: keywords, stdlib, scope functions

### Groq AI
- [x] Fix Bug — sends code + language, parses corrected code block
- [x] Explain Code — beginner-friendly explanation
- [x] Improve Code — best practices suggestions
- [x] Apply Code button to replace editor content
- [x] Loading spinner during API calls
- [x] Offline detection
- [x] API key validation (must start with `gsk_`)
- [x] Exponential backoff retry (3 attempts)
- [x] Rate limit error handling

### Project Management
- [x] Create/save/load/delete/duplicate projects
- [x] Room SQLite database persistence
- [x] Search/filter project list
- [x] Auto-save every 30 seconds
- [x] Language badges and modification dates

### UI/UX
- [x] Material Design 3 components
- [x] Bottom navigation (Editor, Projects, Examples, Settings)
- [x] Dark theme (default) + Light theme
- [x] HTML preview in full-screen WebView dialog
- [x] Find & Replace bar
- [x] AI result dialog with apply button
- [x] Snackbar notifications for save events

---

## 🐛 Known Limitations

1. **Chaquopy Build Environment** — Chaquopy requires a Python installation on the build machine. The `buildPython` path in `app/build.gradle.kts` may need to be adjusted for your machine.

2. **Chaquopy ABI filters** — Only `arm64-v8a`, `x86_64`, `armeabi-v7a`, and `x86` are included. Additional ABIs may be needed for some devices.

3. **Python stdlib** — Only the core Python standard library is available. Third-party packages (numpy, pandas, etc.) would need to be added to `chaquopy.pip`.

4. **JavaScript ES2020+** — Rhino supports up to ES6 (ES2015) natively. Newer features like optional chaining (`?.`) and nullish coalescing (`??`) may not work.

5. **HTML Offline** — HTML files that reference external CDN resources (Bootstrap, etc.) require internet access to render fully.

6. **Autocomplete UI** — The autocomplete dropdown uses a Compose-integrated approach. For a more native experience, consider integrating the Sora Editor library.

---

## 🔧 Troubleshooting

| Issue | Solution |
|---|---|
| "Python not found" at build | Set `buildPython` in `build.gradle.kts` to your Python 3 path |
| Chaquopy build fails | Ensure Python 3.8+ is installed: `python3 --version` |
| APK doesn't install | Enable "Install from unknown sources" in Settings → Security |
| AI features return error | Check API key starts with `gsk_` and internet is connected |
| Python code hangs | 10-second timeout will terminate it automatically |
| Rhino crash on complex JS | Use `cx.optimizationLevel = -1` (already set — interpreted mode) |

---

## 📦 Dependencies

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2024.06.00 | Modern Android UI |
| Material3 | Latest | Material Design 3 |
| Navigation Compose | 2.7.7 | Screen navigation |
| Chaquopy | 14.0.2 | Python runtime |
| Rhino | 1.7.14 | JavaScript runtime |
| Retrofit2 | 2.9.0 | Groq API HTTP client |
| OkHttp3 | 4.12.0 | HTTP with logging |
| Gson | 2.10.1 | JSON parsing |
| Room | 2.6.1 | SQLite ORM |
| DataStore | 1.1.1 | Key-value preferences |
| Security Crypto | 1.1.0-alpha06 | Encrypted storage |
| Coroutines | 1.8.1 | Async/concurrent ops |
| Core Splash Screen | 1.0.1 | Launch screen |

---

## 📄 License

This project is open source. Feel free to use, modify, and distribute it for educational purposes.

---

*Built with ❤️ for students who want to code anywhere, on any device.*
