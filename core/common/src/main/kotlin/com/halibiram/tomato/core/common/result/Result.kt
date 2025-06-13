package com.halibiram.tomato.core.common.result

import com.halibiram.tomato.core.common.error.TomatoException

// Result sealed class
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: TomatoException) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
