package com.halibiram.tomato.core.network.model

import kotlinx.serialization.Serializable

/**
 * A generic wrapper for API responses.
 *
 * @param T The type of the data in the response.
 * @property data The actual data payload. Nullable if the request might not return data or on error.
 * @property message An optional message, often used for errors or informational messages.
 * @property success Indicates if the request was successful.
 */
@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean // Should be true for successful responses, false for errors
)

/**
 * A more specific error response structure, if your API has one.
 */
@Serializable
data class ApiError(
    val errorCode: String,
    val errorMessage: String,
    val details: String? = null
)

// Helper functions to create ApiResponse instances

fun <T> ApiSuccess(data: T): ApiResponse<T> {
    return ApiResponse(data = data, success = true)
}

fun <T> ApiErrorResponse(message: String, data: T? = null): ApiResponse<T> {
    return ApiResponse(data = data, message = message, success = false)
}

// Example of how to handle Ktor responses and wrap them
// suspend inline fun <reified T> HttpClient.safeRequest(
//    block: HttpRequestBuilder.() -> Unit,
// ): ApiResponse<T> {
//    return try {
//        val response = request { block() }
//        if (response.status.isSuccess()) {
//            ApiSuccess(response.body())
//        } else {
//            // You might want to parse a specific error structure from the body
//            val errorBody: String = response.body()
//            ApiErrorResponse("Request failed with status ${response.status}: $errorBody")
//        }
//    } catch (e: ClientRequestException) {
//        // 4xx errors
//        ApiErrorResponse("Client error: ${e.response.status} - ${e.message}")
//    } catch (e: ServerResponseException) {
//        // 5xx errors
//        ApiErrorResponse("Server error: ${e.response.status} - ${e.message}")
//    } catch (e: Exception) {
//        // Other errors (network, serialization, etc.)
//        ApiErrorResponse("An unexpected error occurred: ${e.message}")
//    }
// }
