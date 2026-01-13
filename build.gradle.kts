import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.kotlin.jvm)
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.plugin.publish)
    id("fr.brouillard.oss.gradle.jgitver") version "0.10.0-rc03"
}

group = "io.github.5peak2me.plugin.gradle"

repositories {
    google()
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    explicitApi()

    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

fun Provider<PluginDependency>.toDep() = map {
    dependencyFactory.create(it.pluginId, "${it.pluginId}.gradle.plugin", it.version.toString())
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.kotlin.jvm.toDep())
    implementation(libs.truth)
}

gradlePlugin {
    website.set("https://daijinlin.com/pgyer-gradle-plugin")
    vcsUrl.set("https://github.com/5peak2me/pgyer-gradle-plugin")

    plugins {
        register("pgyer-gradle-plugin") {
            id = "io.github.5peak2me.gradle.pgyer"
            displayName = "pgyer-gradle-plugin"
            description = "上传 APK 到蒲公英平台"
            tags.set(listOf("android gradle plugin pgyer"))
            implementationClass = "io.github.speak2me.plugin.gradle.pgyer.PgyerPlugin"
        }
    }
}

jgitver {
    regexVersionTag = "v([0-9]+(?:\\.[0-9]+){0,2}(?:-[a-zA-Z0-9\\-_]+)?)"
}