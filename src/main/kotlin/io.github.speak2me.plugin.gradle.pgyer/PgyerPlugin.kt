package io.github.speak2me.plugin.gradle.pgyer

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import java.util.Locale

@Suppress("unused")
internal class PgyerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.configure<ApplicationAndroidComponentsExtension> {
                onVariants(selector().withBuildType("release")) { variant ->
                    configureUploadApkTask(variant)
                }
            }
        }
    }

    internal fun Project.configureUploadApkTask(variant: ApplicationVariant) {
        val apiKey = providers.gradleProperty("PGY_API_KEY")
        val password = providers.gradleProperty("PGY_DOWNLOAD_PASSWORD").orElse("1P@ssword")

        @Suppress("DEPRECATION")
        tasks.register<PgyerTask>("upload${variant.name.capitalize(Locale.getDefault())}Apk") {
            group = "pgyer"
            description = "上传 APK 到蒲公英平台"
            this.apiKey.set(apiKey)
            this.password.set(password)
            builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
            apkDir.set(variant.artifacts.get(SingleArtifact.APK))
        }
    }

}