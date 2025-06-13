package com.halibiram.tomato.core.common.error

// ErrorHandler
interface ErrorHandler {
    fun handleError(exception: TomatoException): String
    fun handleThrowable(throwable: Throwable): String
}

class DefaultErrorHandler : ErrorHandler {
    override fun handleError(exception: TomatoException): String {
        return exception.message ?: "An unknown error occurred."
    }

    override fun handleThrowable(throwable: Throwable): String {
        return throwable.message ?: "An unexpected error occurred."
    }
}
