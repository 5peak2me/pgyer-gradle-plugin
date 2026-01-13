package io.github.speak2me.plugin.gradle.pgyer

import com.android.build.api.variant.BuiltArtifactsLoader
import io.github.speak2me.plugin.gradle.pgyer.PgyerTask.UploadWorkAction.Parameters
import io.github.speak2me.plugin.gradle.pgyer.http.HttpService
import io.github.speak2me.plugin.gradle.pgyer.http.Response
import io.github.speak2me.plugin.gradle.pgyer.http.toResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.jetbrains.kotlin.com.google.gson.annotations.SerializedName
import java.io.File
import javax.inject.Inject

internal abstract class PgyerTask @Inject constructor(
    private val worker: WorkerExecutor,
) : DefaultTask() {

    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val password: Property<String>

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    @get:SkipWhenEmpty
    abstract val apkDir: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @TaskAction
    fun execute() {
        val builtArtifacts =
            builtArtifactsLoader.get().load(apkDir.get()) ?: error("Cannot load APKs")
        val builtArtifact = builtArtifacts.elements.singleOrNull() ?: error("Expected one APK !")

        worker.noIsolation().submit(UploadWorkAction::class) {
            apiKey.set(this@PgyerTask.apiKey)
            password.set(this@PgyerTask.password)
            apk.set(File(builtArtifact.outputFile))
        }
    }

    abstract class UploadWorkAction : WorkAction<Parameters> {

        abstract class Parameters : WorkParameters {
            abstract val apiKey: Property<String>
            abstract val password: Property<String>

            abstract val apk: RegularFileProperty
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun execute() {
            with(parameters) {
                var id = ""
                runBlocking {
                    getToken(apiKey.get(), password.get())
                        .flatMapLatest {
                            id = it.data.key.substringBeforeLast(".apk")
                            doUpload(apk.get().asFile, it.data)
                        }
                        .toResult()
                        .onStart { println("️⌛️⌛️⌛️ 上传中...") }
                        .collect {
                            when {
                                it.isFailure -> println("❗️❗️❗️ 上传失败: ${it.exceptionOrNull()?.stackTraceToString()}")

                                it.isSuccess -> println("🎉🎉🎉 上传成功: https://pgyer.com/${id}")
                            }
                        }
                }
            }
        }

        private fun getToken(apiKey: String, password: String): Flow<Response<Data>> {
            val formData = buildMap(3) {
                put("_api_key", apiKey)
                put("buildType", "apk")
                put("buildPassword", password) // Optional
            }
            return HttpService.post<Data>(URL_TOKEN, formData)
        }

        private fun doUpload(file: File, data: Data): Flow<Response<String>> {
            return HttpService.upload<String>(
                url = data.endpoint,
                file = file,
                parameters = buildMap(4) {
                    put("key", data.key)
                    put("signature", data.params.signature)
                    put("x-cos-security-token", data.params.token)
                    put("x-cos-meta-file-name", file.nameWithoutExtension) // Optional
                },
            )
        }
    }

    private companion object {
        private const val URL_TOKEN = "https://api.pgyer.com/apiv2/app/getCOSToken"
    }

}

private data class Data(
    val endpoint: String,
    val key: String,
    val params: Params,
) {
    data class Params(
        val key: String,
        val signature: String,
        @SerializedName("x-cos-security-token")
        val token: String,
    )
}
