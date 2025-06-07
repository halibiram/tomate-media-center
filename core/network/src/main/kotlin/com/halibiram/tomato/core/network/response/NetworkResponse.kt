package com.halibiram.tomato.core.network.response

import com.halibiram.tomato.core.common.error.TomatoException

// NetworkResponse sealed class, similar to Result but more network specific
sealed class NetworkResponse<out T : Any, out U : Any> {
    /**
     * A request that resulted in a response with a 2xx status code that has a body.
     */
    data class Success<T : Any>(val body: T, val code: Int) : NetworkResponse<T, Nothing>()

    /**
     * A request that resulted in a response with a non-2xx status code that has a body.
     */
    data class ApiError<U : Any>(val body: U, val code: Int) : NetworkResponse<Nothing, U>()

    /**
     * A request that resulted in an error different from an ApiError.
     */
    data class NetworkError(val error: TomatoException.NetworkError) : NetworkResponse<Nothing, Nothing>()

    /**
     * A request that resulted in an error that is not an ApiError or a NetworkError.
     */
    data class UnknownError(val error: TomatoException.UnknownError) : NetworkResponse<Nothing, Nothing>()
}
