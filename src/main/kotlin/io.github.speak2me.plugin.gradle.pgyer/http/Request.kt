package io.github.speak2me.plugin.gradle.pgyer.http

import java.io.File
import java.net.URLEncoder
import java.nio.file.Files
import java.util.*

/**
 * RequestBody 构建策略接口
 */
internal interface RequestBodyBuilder {
    fun contentType(): String
    fun build(): ByteArray
}

/**
 * JSON 请求体构建器
 */
internal class JsonBodyBuilder(private val json: String) : RequestBodyBuilder {
    override fun contentType() = "application/json"
    override fun build() = json.toByteArray(Charsets.UTF_8)
}

/**
 * 表单请求体构建器
 */
internal class FormBodyBuilder(private val form: Map<String, String>) : RequestBodyBuilder {
    override fun contentType() = "application/x-www-form-urlencoded"
    override fun build(): ByteArray {
        val body = form.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
        return body.toByteArray(Charsets.UTF_8)
    }
}

private const val CRLF = "\r\n"

/**
 * Multipart 请求体构建器
 */
internal class MultipartBodyBuilder(
    private val files: Map<String, File>,
    private val params: Map<String, String>,
) : RequestBodyBuilder {
    private val boundary = "Boundary-${UUID.randomUUID()}"

    override fun contentType() = "multipart/form-data; boundary=$boundary"

    override fun build(): ByteArray {
        val parts = mutableListOf<ByteArray>()

        params.forEach { (key, value) ->
            val part = buildString {
                append("--$boundary$CRLF")
                append("Content-Disposition: form-data; name=\"$key\"$CRLF")
                append(CRLF)
                append("$value$CRLF")
            }.toByteArray(Charsets.UTF_8)
            parts.add(part)
        }

        files.forEach { (fieldName, file) ->
            val prefix = buildString {
                append("--$boundary$CRLF")
                append("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"${file.name}\"$CRLF")
                append("Content-Type: ${Files.probeContentType(file.toPath()) ?: "application/octet-stream"}$CRLF")
                append(CRLF)
            }.toByteArray(Charsets.UTF_8)

            parts.add(prefix)
            parts.add(file.readBytes())
            parts.add(CRLF.toByteArray(Charsets.UTF_8))
        }

        parts.add("--$boundary--$CRLF".toByteArray(Charsets.UTF_8))

        val totalSize = parts.sumOf { it.size }
        return ByteArray(totalSize).apply {
            var offset = 0
            parts.forEach {
                System.arraycopy(it, 0, this, offset, it.size)
                offset += it.size
            }
        }
    }
}
