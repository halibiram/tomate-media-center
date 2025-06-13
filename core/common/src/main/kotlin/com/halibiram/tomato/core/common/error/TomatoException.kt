package com.halibiram.tomato.core.common.error

// TomatoException
sealed class TomatoException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    data class NetworkError(override val message: String?, override val cause: Throwable? = null) : TomatoException(message, cause)
    data class DatabaseError(override val message: String?, override val cause: Throwable? = null) : TomatoException(message, cause)
    data class UnknownError(override val message: String?, override val cause: Throwable? = null) : TomatoException(message, cause)
    data class AuthenticationError(override val message: String?, override val cause: Throwable? = null) : TomatoException(message, cause)
    data class FeatureError(override val message: String?, override val cause: Throwable? = null) : TomatoException(message, cause)
}
