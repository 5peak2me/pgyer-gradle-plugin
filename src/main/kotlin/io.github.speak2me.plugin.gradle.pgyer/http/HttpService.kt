package io.github.speak2me.plugin.gradle.pgyer.http

import com.google.common.net.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.kotlin.com.google.gson.Gson
import org.jetbrains.kotlin.com.google.gson.GsonBuilder
import org.jetbrains.kotlin.com.google.gson.reflect.TypeToken
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

// CompletableFuture 转 suspend 函数
private suspend fun <T> CompletableFuture<T>.await(): T = suspendCancellableCoroutine { cont ->
    whenComplete { result, exception ->
        if (exception != null) {
            cont.resumeWithException(exception)
        } else {
            cont.resume(result)
        }
    }
}

/**
 * HTTP 请求服务
 */
internal object HttpService {
    private const val TIMEOUT = 30L

    private val client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(TIMEOUT))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .serializeNulls() // 序列化 null 值
        .setPrettyPrinting() // 格式化输出（可选）
        .create()

    inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): Flow<Response<T>> = request(url, HttpRequest.newBuilder().GET().applyHeaders(headers))

    @Suppress("unused")
    inline fun <reified T> post(
        url: String,
        json: String,
        headers: Map<String, String> = emptyMap(),
    ): Flow<Response<T>> = sendWithBody(url, JsonBodyBuilder(json), headers)

    inline fun <reified T> post(
        url: String,
        form: Map<String, String>,
        headers: Map<String, String> = emptyMap(),
    ): Flow<Response<T>> = sendWithBody(url, FormBodyBuilder(form), headers)

    inline fun <reified T> upload(
        url: String,
        file: File,
        headers: Map<String, String> = emptyMap(),
        parameters: Map<String, String> = emptyMap(),
    ): Flow<Response<T>> =
        sendWithBody(url, MultipartBodyBuilder(mapOf("file" to file), parameters), headers)

    private inline fun <reified T> sendWithBody(
        url: String,
        bodyBuilder: RequestBodyBuilder,
        headers: Map<String, String>,
    ): Flow<Response<T>> {
        val builder = HttpRequest.newBuilder()
            .header(HttpHeaders.CONTENT_TYPE, bodyBuilder.contentType())
            .applyHeaders(headers)
            .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBuilder.build()))
        return request(url, builder)
    }

    /**
     * [Mastering Time with Kotlin](https://josiassena.com/mastering-time-with-kotlin-inside-the-new-timing-api/)
     */
    @OptIn(ExperimentalTime::class)
    private inline fun <reified T> request(
        url: String,
        builder: HttpRequest.Builder,
    ): Flow<Response<T>> = flow {
        val source = TimeSource.Monotonic.markNow()
        val request = builder.uri(URI.create(url)).build()
        val response = client.sendAsync(request, BodyHandlers.ofString()).await()

        val elapsed = source.elapsedNow()
        println(
            "[HttpService] ${request.method()} $url -> ${response.statusCode()} (${
                elapsed.toString(DurationUnit.MILLISECONDS, 2)
            })",
        )

        val body = response.body()
        if (response.statusCode() == 200) {
            val type = object : TypeToken<Response<T>>() {}.type
            emit(gson.fromJson<Response<T>>(body, type))
        } else {
            emit(Response(response.statusCode(), response.body(), body as T))
        }
    }

    private fun HttpRequest.Builder.applyHeaders(headers: Map<String, String>) = apply {
        headers.forEach { (k, v) -> header(k, v) }
    }

}
