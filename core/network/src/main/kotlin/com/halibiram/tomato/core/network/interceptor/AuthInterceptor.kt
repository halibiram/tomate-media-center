package com.halibiram.tomato.core.network.interceptor

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header

// Placeholder for Auth Interceptor logic.
// In Ktor, this is typically handled by a custom plugin/feature or by modifying requests directly.
object AuthInterceptor {

    // This is a simplified way to think about it.
    // A more robust solution would involve a Ktor Feature (Plugin).
    // Or, this function could be called from a centralized request-making utility.
    fun addAuthHeader(requestBuilder: HttpRequestBuilder, token: String?) {
        token?.let {
            if (it.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $it")
            }
        }
        // Alternatively, could fetch the token from a secure storage here if not passed directly.
    }

    // Example API Key auth (if that's the model)
    fun addApiKeyHeader(requestBuilder: HttpRequestBuilder, apiKey: String?) {
        apiKey?.let {
            if (it.isNotBlank()) {
                requestBuilder.header("X-Api-Key", it)
            }
        }
    }
}
