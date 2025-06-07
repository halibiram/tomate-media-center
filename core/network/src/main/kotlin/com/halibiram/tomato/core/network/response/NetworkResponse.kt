package com.halibiram.tomato.core.network.response

import java.io.IOException

/**
 * A sealed class to represent network operation outcomes, providing a structured way
 * to handle success and various error types.
 *
 * @param T The type of the successful data.
 */
sealed class NetworkResponse<out T : Any> {
    /**
     * Represents a successful network response.
     * @property data The data received from the network.
     */
    data class Success<out T : Any>(val data: T) : NetworkResponse<T>()

    /**
     * Represents a failure due to an API-specific error (e.g., validation error, not found).
     * @property code The HTTP status code.
     * @property errorBody The raw error body string, which might be JSON or other format.
     * @property apiErrorResponse Optional pre-parsed API error structure.
     */
    data class ApiError(
        val code: Int,
        val errorBody: String?,
        val apiErrorResponse: ApiErrorResponse? = null // Optional: if you parse your error bodies
    ) : NetworkResponse<Nothing>()

    /**
     * Represents a failure due to a network issue (e.g., no internet connection).
     * @property exception The [IOException] that occurred.
     */
    data class NetworkError(val exception: IOException) : NetworkResponse<Nothing>()

    /**
     * Represents a failure due to an unexpected error during the network call or processing.
     * @property exception The [Throwable] that occurred.
     */
    data class UnknownError(val exception: Throwable) : NetworkResponse<Nothing>()
}

/**
 * Helper function to execute a suspend function that makes a network call and wrap its
 * response in [NetworkResponse].
 *
 * This is a basic example. You'd typically integrate this with Ktor's exception handling.
 */
// suspend fun <T : Any> safeApiCall(apiCall: suspend () -> T): NetworkResponse<T> {
// return try {
// NetworkResponse.Success(apiCall.invoke())
//    } catch (e: HttpRequestTimeoutException) {
// NetworkResponse.NetworkError(IOException("Request timed out", e))
//    } catch (e: ClientRequestException) {
//        // Ktor specific exception for non-2xx responses
// val errorBody = try { e.response.bodyAsText() } catch (_: Exception) { null }
// NetworkResponse.ApiError(code = e.response.status.value, errorBody = errorBody)
//    } catch (e: IOException) {
// NetworkResponse.NetworkError(e)
//    } catch (e: Exception) {
// NetworkResponse.UnknownError(e)
//    }
//}
