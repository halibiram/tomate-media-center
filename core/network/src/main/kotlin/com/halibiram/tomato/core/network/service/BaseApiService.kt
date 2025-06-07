package com.halibiram.tomato.core.network.service

import com.halibiram.tomato.core.network.model.ApiResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Base class for API services, providing common Ktor HTTP client functionality.
 */
abstract class BaseApiService(protected val httpClient: HttpClient) {

    // Example base URL - specific services should override or configure this
    protected open val baseUrl: String = "https://api.example.com/v1/"

    /**
     * Executes a Ktor HTTP request and wraps the result in an [ApiResponse].
     * This function handles common success and error scenarios.
     *
     * @param T The expected type of the successful response body.
     * @param R The type of the data within the ApiResponse (often same as T, but allows flexibility).
     * @param block A lambda to configure the [HttpRequestBuilder].
     * @return An [ApiResponse] containing the data or an error message.
     */
    protected suspend inline fun <reified T, reified R> safeApiCall(
        crossinline block: HttpRequestBuilder.() -> Unit
    ): ApiResponse<R> {
        return try {
            val response = httpClient.request {
                block()
                // Ensure the URL is correctly formed if it's relative
                // url.takeFrom(baseUrl) // This might be needed depending on how you structure specific service calls
            }

            if (response.status.isSuccess()) {
                val body = response.body<R>()
                ApiResponse(data = body, success = true)
            } else {
                // Attempt to read an error message from the response if possible
                val errorBody = try { response.body<String>() } catch (e: Exception) { null }
                ApiResponse(
                    message = "Request failed: ${response.status.description}. ${errorBody ?: ""}".trim(),
                    success = false
                )
            }
        } catch (e: ClientRequestException) { // 4xx errors
            val errorBody = try { e.response.body<String>() } catch (ex: Exception) { null }
            ApiResponse(
                message = "Client error: ${e.response.status.description}. ${errorBody ?: e.message}".trim(),
                success = false
            )
        } catch (e: ServerResponseException) { // 5xx errors
            val errorBody = try { e.response.body<String>() } catch (ex: Exception) { null }
            ApiResponse(
                message = "Server error: ${e.response.status.description}. ${errorBody ?: e.message}".trim(),
                success = false
            )
        } catch (e: NoTransformationFoundException) {
            ApiResponse(message = "Serialization error: Could not transform response body. ${e.message}", success = false)
        }
        catch (e: Exception) { // Other errors (network, serialization, etc.)
            ApiResponse(message = "An unexpected error occurred: ${e.message}", success = false)
        }
    }

    // Example of a GET request structure
    // suspend inline fun <reified T> get(endpoint: String, queryParams: Map<String, String> = emptyMap()): ApiResponse<T> {
    //    return safeApiCall {
    //        method = HttpMethod.Get
    //        url(baseUrl + endpoint)
    //        queryParams.forEach { (key, value) -> parameter(key, value) }
    //    }
    // }
}
