# pgyer-gradle-plugin [![Version](https://img.shields.io/gradle-plugin-portal/v/io.github.5peak2me.gradle.pgyer?logo=gradle)](https://plugins.gradle.org/plugin/io.github.5peak2me.gradle.pgyer)

[![Kotlin](https://img.shields.io/badge/dynamic/toml?url=https://raw.githubusercontent.com/5peak2me/pgyer-gradle-plugin/main/gradle/libs.versions.toml&query=$.versions.kotlin&label=Kotlin&color=blue&logo=kotlin)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/dynamic/toml?url=https://raw.githubusercontent.com/5peak2me/pgyer-gradle-plugin/main/gradle/libs.versions.toml&query=$.versions.agp&label=AGP&color=blue&logo=android)](https://developer.android.com/build/releases/gradle-plugin)
[![Gradle](https://img.shields.io/badge/dynamic/regex?url=https://raw.githubusercontent.com/5peak2me/pgyer-gradle-plugin/main/gradle/wrapper/gradle-wrapper.properties&search=gradle-([0-9.]%2B)-(?:bin%7Call)\.zip&replace=$1&label=Gradle&color=blue&logo=gradle)](https://gradle.org)
[![Configuration Cache](https://img.shields.io/badge/Configuration%20Cache-supported-brightgreen.svg)](https://docs.gradle.org/current/userguide/configuration_cache.html)

Sube APKs de lanzamiento de Android a [Pgyer](https://www.pgyer.com/) desde Gradle.

## Instalación

Añade este plugin a tu construcción utilizando el DSL de plugins:

```kotlin
plugins {
    id("io.github.5peak2me.gradle.pgyer") version "1.0.3"
}
```

El plugin debe aplicarse a un módulo de aplicación Android.

## Configuración

Añade las siguientes propiedades a tu archivo de propiedades globales de Gradle:

| Parámetro               | Requerido | Predeterminado | Descripción                                                                                         |
|-------------------------|-----------|---------------|-----------------------------------------------------------------------------------------------------|
| `PGY_API_KEY`           | Sí        | -             | Clave API de Pgyer, obtenida desde [https://www.pgyer.com/account/api](https://www.pgyer.com/account/api) |
| `PGY_DOWNLOAD_PASSWORD` | No        | `1P@ssword`   | Contraseña de descarga del APK                                                                       |

```properties
PGY_API_KEY=tu_api_key_aqui
PGY_DOWNLOAD_PASSWORD=1P@ssword
```

Para la mayoría de las configuraciones locales, este archivo se encuentra en `~/.gradle/gradle.properties`.

## Uso

Construye y sube el APK de lanzamiento:

```shell
./gradlew uploadReleaseApk
```

El plugin registra tareas de subida para las variantes de lanzamiento (release). Para una variante de lanzamiento con sabor (flavor), utiliza el nombre de la tarea correspondiente, por ejemplo:

```shell
./gradlew uploadDemoReleaseApk
```

## Licencia

Este proyecto está bajo la licencia [Apache License 2.0](LICENSE).
