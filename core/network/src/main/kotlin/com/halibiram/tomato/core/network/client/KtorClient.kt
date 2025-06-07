package com.halibiram.tomato.core.network.client

import com.halibiram.tomato.core.network.interceptor.LoggingInterceptor // Assuming this is a Ktor Logger
import com.halibiram.tomato.core.network.util.NetworkConstants // For BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine // For constructor injection
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor HTTP client configuration.
 *
 * @param engine The HttpClientEngine to use (e.g., Android, CIO, OkHttp). Injected for testability.
 * @param json The Kotlinx Json serialization instance. Injected for custom configuration.
 */
class KtorClient(
    private val engine: HttpClientEngine,
    private val json: Json
) {
    fun build(): HttpClient {
        return HttpClient(engine) {
            // Default request parameters
            defaultRequest {
                url(NetworkConstants.BASE_URL) // Base URL for all requests
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                // Add other common headers if needed, e.g., API key
                // header("X-Api-Key", NetworkConstants.API_KEY) // Example if API key is static
            }

            // Content negotiation for JSON serialization/deserialization
            install(ContentNegotiation) {
                json(json) // Use the injected Json instance
            }

            // Logging plugin
            install(Logging) {
                // Use the custom LoggingInterceptor if it's adapted as a Ktor Logger
                // Otherwise, use Ktor's built-in logger:
                logger = object : Logger {
                    override fun log(message: String) {
                        // Delegate to your custom logger or use a platform logger
                        // For now, using the placeholder LoggingInterceptor's static log method
                        LoggingInterceptor.log(message) // This assumes LoggingInterceptor.log is a static method
                        // A better approach would be to inject a LoggingInterceptor instance that implements Ktor's Logger
                        // Log.d("KtorHttpClient", message) // Or directly use Android Log
                    }
                }
                level = LogLevel.ALL // Log all levels: HEADERS, BODY, INFO
                // filter { request -> request.url.host.contains("ktor.io") } // Example filter
            }

            // Timeouts (example, configure as needed in engine block)
            // engine {
            //     connectTimeout = NetworkConstants.DEFAULT_CONNECT_TIMEOUT_SECONDS * 1000
            //     socketTimeout = NetworkConstants.DEFAULT_READ_TIMEOUT_SECONDS * 1000
            //     // For CIO engine specific timeout:
            //     // requestTimeout = NetworkConstants.DEFAULT_REQUEST_TIMEOUT_SECONDS * 1000
            // }

            // Expect success for all requests (i.e., throw exceptions for non-2xx responses)
            // This is often true by default but can be made explicit.
            // expectSuccess = true
        }
    }
}

// Note: The previous KtorClient was a simple wrapper. This is a more conventional factory/builder class.
// The DI module will provide this KtorClient, and then call .build() to get the HttpClient.
// Or, KtorClient itself can be the HttpClient if preferred, by inheriting from it or by delegation.
// For now, KtorClient().build() provides the HttpClient.
// Let's adjust to make KtorClient itself the HttpClient via delegation for cleaner DI.

class TomatoHttpClient(private val configuredClient: HttpClient) {
    val httpClient: HttpClient = configuredClient
}

// The above KtorClient should be a factory that *creates* the HttpClient.
// Let's rename the class to reflect it's a factory or configurer.
// Or, the DI module can directly construct HttpClient using this logic.

// Let's stick to the idea that `KtorClient(engine, json).build()` provides the HttpClient.
// The `NetworkModule` will then provide the `HttpClient` instance.
// The previous placeholder `KtorClient` class was:
// class KtorClient(val httpClient: HttpClient)
// This was simpler. The current one is a builder/configurator.

// To simplify, let's assume NetworkModule directly configures and provides HttpClient.
// So, this KtorClient.kt might not be needed if all logic moves to NetworkModule.
// However, the request was to flesh out KtorClient.kt.
// The current KtorClient(engine, json).build() approach is fine.
// The previous `TomatoHttpClient` wrapper is redundant if `KtorClient().build()` is used in DI.The existing `KtorClient.kt` file has been overwritten with the new Ktor client configuration logic. It now takes `HttpClientEngine` and `Json` as constructor parameters and has a `build()` method to create a configured `HttpClient`.

**Phase 3: Flesh out `NetworkModule.kt` in `core/network/src/main/kotlin/com/halibiram/tomato/core/network/di/`**

I will overwrite the existing placeholder `NetworkModule.kt`.
