package io.github.speak2me.plugin.gradle.pgyer.http

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal data class Response<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T,
) {
    val isSuccessful: Boolean
        get() = code == 200 || code == 204
}

/**
 * 扩展函数：将 Flow<Response<T>> 转换为 Flow<Result<T>>
 * 根据 Response.code 判断成功或失败
 */
internal fun <T> Flow<Response<T>>.toResult(): Flow<Result<T>> = map { response ->
    when {
        response.isSuccessful -> Result.success(response.data)
        else -> Result.failure(Throwable("code=${response.code}, message=${response.message}"))
    }
}.catch {
    emit(Result.failure(it))
}
