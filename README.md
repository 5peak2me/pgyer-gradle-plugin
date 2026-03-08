# pgyer-gradle-plugin

Add this plugin to your build using the plugins DSL:
```kotlin
plugins {
  id("io.github.5peak2me.gradle.pgyer") version "1.0.0-0"
}
```

## Configuration

Add the following parameters to `~/.gradle/gradle.properties`:

| Parameter | Required | Default | Description |
|---|---|---|---|
| `PGY_API_KEY` | Yes | - | Pgyer API Key, obtained from [https://www.pgyer.com/account/api](https://www.pgyer.com/account/api) |
| `PGY_DOWNLOAD_PASSWORD` | No | `1P@ssword` | APK download password |

```properties
PGY_API_KEY=your_api_key_here
PGY_DOWNLOAD_PASSWORD=1P@ssword
```