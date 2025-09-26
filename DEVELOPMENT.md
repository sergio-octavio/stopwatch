# Development Guide 👨‍💻

## Quick Start

### Prerequisites Setup
```bash
# Verify Java installation
java -version  # Should be JDK 11+

# Check Android SDK
echo $ANDROID_HOME  # Should point to Android SDK

# Verify ADB
adb version

# Check Gradle
./gradlew --version
```

### Project Setup
```bash
# Clone and navigate
git clone <repository-url>
cd stopwatch

# Sync dependencies
./gradlew dependencies

# Build debug version
./gradlew assembleDebug

# Install on device/emulator
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Development Workflow

### Code Style & Standards
- **Language**: Kotlin with official code style
- **Architecture**: MVVM with Jetpack Compose
- **Naming**: camelCase for functions, PascalCase for classes
- **Comments**: KDoc for public APIs

### Git Workflow
```bash
# Feature development
git checkout -b feature/timer-precision-improvement
git commit -m "feat: improve timer precision to 5ms intervals"
git push origin feature/timer-precision-improvement

# Bug fixes
git checkout -b fix/memory-leak-in-timer
git commit -m "fix: resolve memory leak in timer coroutines"
```

### Testing Commands
```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# UI tests
./gradlew connectedDebugAndroidTest

# Code coverage
./gradlew jacocoTestReport
```

## Performance Monitoring

### Memory Profiling
```bash
# Monitor memory during development
adb shell am start -n com.example.stopwatch/.MainActivity
adb shell dumpsys meminfo com.example.stopwatch
```

### Timer Accuracy Testing
```kotlin
// Test timer precision
@Test
fun testTimerAccuracy() {
    val startTime = System.currentTimeMillis()
    // Run timer for 10 seconds
    delay(10000)
    val endTime = System.currentTimeMillis()
    val accuracy = abs((endTime - startTime) - 10000)
    assert(accuracy < 50) // Within 50ms tolerance
}
```

## Debugging Tips

### Common Issues
1. **Timer Drift**: Use `System.currentTimeMillis()` for calculations
2. **Memory Leaks**: Always cancel coroutine jobs in `onCleared()`
3. **UI Freezing**: Keep heavy operations off main thread

### Debug Build Configuration
```kotlin
// Enable debugging features
android {
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }
}
```

## Code Architecture Guidelines

### Adding New Features

#### 1. Model Layer Changes
```kotlin
// Add new property to model
data class Stopwatch(
    // ... existing properties
    val newFeature: String = ""
)
```

#### 2. ViewModel Updates
```kotlin
// Add business logic
fun updateNewFeature(id: String, value: String) {
    val stopwatch = findStopwatch(id) ?: return
    val updated = stopwatch.copy(newFeature = value)
    updateStopwatch(updated)
}
```

#### 3. UI Integration
```kotlin
// Update component
@Composable
fun StopwatchItem(
    // ... existing parameters
    onNewFeatureChange: (String) -> Unit
) {
    // UI implementation
}
```

### Performance Best Practices

#### State Management
```kotlin
// ✅ Good: Efficient state updates
private fun updateStopwatch(updated: Stopwatch) {
    _stopwatches.value = _stopwatches.value.map {
        if (it.id == updated.id) updated else it
    }
}

// ❌ Bad: Unnecessary list recreation
private fun updateStopwatchBad(updated: Stopwatch) {
    val newList = mutableListOf<Stopwatch>()
    _stopwatches.value.forEach { ... }
    _stopwatches.value = newList
}
```

#### Coroutine Usage
```kotlin
// ✅ Good: Structured concurrency
private fun startTimer(id: String) {
    timerJobs[id] = viewModelScope.launch {
        while (true) {
            delay(10)
            updateTime(id)
        }
    }
}

// ❌ Bad: Unmanaged coroutines
private fun startTimerBad(id: String) {
    GlobalScope.launch {  // Can cause memory leaks
        // timer logic
    }
}
```

## Build Variants

### Debug Configuration
```kotlin
buildTypes {
    debug {
        isDebuggable = true
        isMinifyEnabled = false
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-DEBUG"
    }
}
```

### Release Configuration
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

## Dependency Management

### Version Catalog (`libs.versions.toml`)
```toml
[versions]
kotlin = "1.9.20"
compose-bom = "2024.09.00"
lifecycle = "2.7.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "kotlin" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
```

### Adding Dependencies
```bash
# Add new dependency
# 1. Update libs.versions.toml
# 2. Add to app/build.gradle.kts
implementation(libs.new.dependency)

# Sync project
./gradlew --refresh-dependencies
```

## Deployment Checklist

### Pre-Release Testing
- [ ] **Unit tests pass**: `./gradlew test`
- [ ] **UI tests pass**: `./gradlew connectedAndroidTest`
- [ ] **Memory profiling**: No leaks detected
- [ ] **Performance testing**: Smooth 60fps
- [ ] **Device testing**: Multiple screen sizes

### Release Build
```bash
# Generate signed APK
./gradlew assembleRelease

# Verify APK
aapt dump badging app/build/outputs/apk/release/app-release.apk

# Test installation
adb install app/build/outputs/apk/release/app-release.apk
```

## Troubleshooting

### Build Issues
```bash
# Clean build
./gradlew clean

# Refresh dependencies
./gradlew --refresh-dependencies

# Check Gradle daemon
./gradlew --status
./gradlew --stop
```

### Runtime Issues
```bash
# Check logs
adb logcat | grep "MultiStopwatch"

# Monitor CPU usage
adb shell top | grep com.example.stopwatch

# Check memory usage
adb shell dumpsys meminfo com.example.stopwatch
```

## IDE Configuration

### Android Studio Settings
```
File → Settings → Editor → Code Style → Kotlin
- Use official Kotlin code style
- Line length: 120 characters
- Imports: Use single name import

Build, Execution, Deployment → Gradle
- Use Gradle 'wrapper' task configuration
- Gradle JVM: Project SDK (JDK 11)
```

### Useful Plugins
- **Kotlin Multiplatform Mobile**
- **ADB Idea** (ADB commands from IDE)
- **Git Commit Message Template**
- **Rainbow Brackets** (code readability)

---

## Monitoring & Analytics

### Performance Metrics
- **App startup time**: < 1 second cold start
- **Memory usage**: < 50MB with 10 active stopwatches
- **Timer accuracy**: ±10ms precision
- **UI responsiveness**: 60fps animations

### Quality Gates
- **Code coverage**: > 80%
- **Lint issues**: 0 errors, minimal warnings
- **Security scan**: No vulnerabilities
- **Performance regression**: < 5% degradation

---

*This development guide ensures consistent code quality and efficient development workflow for the MultiStopwatch project.*