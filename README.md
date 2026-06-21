# pgyer-gradle-plugin

[![Version](https://img.shields.io/gradle-plugin-portal/v/io.github.5peak2me.gradle.pgyer?logo=gradle)](https://plugins.gradle.org/plugin/io.github.5peak2me.gradle.pgyer)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-green.svg?logo=kotlin)](https://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/Gradle-9.6.0-green.svg?logo=gradle)](https://gradle.org)
[![Configuration Cache](https://img.shields.io/badge/Configuration%20Cache-supported-brightgreen.svg)](https://docs.gradle.org/current/userguide/configuration_cache.html)

Upload Android release APKs to [Pgyer](https://www.pgyer.com/) from Gradle.

## Installation

Add this plugin to your build using the plugins DSL:

```kotlin
plugins {
    id("io.github.5peak2me.gradle.pgyer") version "1.0.3"
}
```

The plugin should be applied to an Android application module.

## Configuration

Add the following properties to your global Gradle properties file:

| Parameter               | Required | Default     | Description                                                                                         |
|-------------------------|----------|-------------|-----------------------------------------------------------------------------------------------------|
| `PGY_API_KEY`           | Yes      | -           | Pgyer API Key, obtained from [https://www.pgyer.com/account/api](https://www.pgyer.com/account/api) |
| `PGY_DOWNLOAD_PASSWORD` | No       | `1P@ssword` | APK download password                                                                               |

```properties
PGY_API_KEY=your_api_key_here
PGY_DOWNLOAD_PASSWORD=1P@ssword
```

For most local setups, this file is located at `~/.gradle/gradle.properties`.

## Usage

Build and upload the release APK:

```shell
./gradlew uploadReleaseApk
```

The plugin registers upload tasks for release variants. For a flavored release variant, use the matching task name, for example:

```shell
./gradlew uploadDemoReleaseApk
```

## License

This project is licensed under the [Apache License 2.0](LICENSE).
