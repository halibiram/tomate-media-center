package com.halibiram.tomato.core.common.result

/**
 * Base exception class for domain-specific errors in the Tomato application.
 *
 * @param message A descriptive message for the exception.
 * @param cause The underlying cause of the exception, if any.
 */
open class TomatoException(
    override val message: String,
    override val cause: Throwable? = null,
    val errorCode: String? = null // Optional: Application-specific error code
) : Exception(message, cause)

/**
 * Represents an error related to network operations.
 * This could be due to connectivity issues, server errors, timeouts, etc.
 */
class NetworkException(
    message: String,
    cause: Throwable? = null,
    val httpStatusCode: Int? = null, // Specific HTTP status code if applicable
    errorCode: String? = null
) : TomatoException(message, cause, errorCode)

/**
 * Represents an error related to database operations.
 * This could be due to issues like query failures, data integrity violations, etc.
 */
class DatabaseException(
    message: String,
    cause: Throwable? = null,
    errorCode: String? = null
) : TomatoException(message, cause, errorCode)

/**
 * Represents an error that occurs during data mapping (e.g., DTO to Domain model).
 */
class DataMappingException(
    message: String,
    cause: Throwable? = null,
    errorCode: String? = "DATA_MAPPING_ERROR"
) : TomatoException(message, cause, errorCode)


/**
 * Represents an error when expected data is not found.
 */
class NotFoundException(
    message: String,
    cause: Throwable? = null,
    errorCode: String? = "NOT_FOUND"
) : TomatoException(message, cause, errorCode)

/**
 * Represents an error due to an issue with an extension.
 */
class ExtensionException(
    message: String,
    val extensionId: String? = null,
    cause: Throwable? = null,
    errorCode: String? = "EXTENSION_ERROR"
) : TomatoException(message, cause, errorCode)


/**
 * Represents an unknown or unexpected error that doesn't fit into other categories.
 */
class UnknownErrorException(
    message: String = "An unknown error occurred.",
    cause: Throwable? = null,
    errorCode: String? = "UNKNOWN_ERROR"
) : TomatoException(message, cause, errorCode)

// Add other specific exception types as needed, e.g.:
// class AuthenticationException(message: String, cause: Throwable? = null, errorCode: String? = "AUTH_ERROR") : TomatoException(message, cause, errorCode)
// class PermissionDeniedException(message: String, cause: Throwable? = null, errorCode: String? = "PERMISSION_DENIED") : TomatoException(message, cause, errorCode)
