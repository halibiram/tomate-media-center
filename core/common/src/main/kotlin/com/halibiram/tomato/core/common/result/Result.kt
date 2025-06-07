package com.halibiram.tomato.core.common.result

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: TomatoException) : Result<Nothing>()
    data class Loading<out T>(val partialData: T? = null) : Result<T>() // Allow emitting partial data while loading

    // Helper to check for success easily
    val isSuccess: Boolean get() = this is Success<*>
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading<*>

    fun getOrNull(): T? = if (this is Success<T>) data else null
    fun exceptionOrNull(): TomatoException? = if (this is Error) exception else null
}

// Extension function for convenience
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (TomatoException) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}

inline fun <T> Result<T>.onLoading(action: (T?) -> Unit): Result<T> {
    if (this is Result.Loading) action(partialData)
    return this
}
