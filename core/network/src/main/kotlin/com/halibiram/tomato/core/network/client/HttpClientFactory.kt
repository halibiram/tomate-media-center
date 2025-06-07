package com.halibiram.tomato.core.network.client

import com.halibiram.tomato.core.network.interceptor.AuthInterceptor
import com.halibiram.tomato.core.network.interceptor.LoggingInterceptor
import com.halibiram.tomato.core.network.util.NetworkConstants
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
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

// Factory to create Ktor HttpClient
class HttpClientFactory {

    fun create(): HttpClient {
        return HttpClient(CIO) { // Using CIO engine, can be Android, OkHttp etc.
            expectSuccess = true // Configure HttpClient to expect successful responses

            // Default request configuration
            defaultRequest {
                url(NetworkConstants.BASE_URL) // Base URL for all requests
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                // Add other common headers if needed
            }

            // Content negotiation for JSON serialization/deserialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true // Helpful for API evolution
                })
            }

            // Logging plugin
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        LoggingInterceptor.log(message)
                    }
                }
                level = LogLevel.ALL // Log all levels: HEADERS, BODY, INFO
            }

            // Custom interceptors (concept)
            // Note: Ktor doesn't have "interceptors" exactly like OkHttp.
            // You use plugins or feature phases.
            // For simplicity, we'll imagine AuthInterceptor is integrated here or via a feature.
            // install(AuthInterceptorFeature) { apiKey = "your_api_key" }
            // For now, AuthInterceptor.addAuthHeader can be called manually or within a custom plugin

            // Timeouts (example, configure as needed)
            // engine {
            // requestTimeout = 15_000 // 15 seconds
            // connectTimeout = 10_000 // 10 seconds
            // socketTimeout = 10_000 // 10 seconds
            // }
        }
    }
}

// Placeholder for a custom Auth Interceptor Feature (Ktor Plugin)
// class AuthInterceptorFeature(config: Config) {
// companion object : HttpClientFeature<Config, AuthInterceptorFeature> {
// override val key: AttributeKey<AuthInterceptorFeature> = AttributeKey("AuthInterceptorFeature")
// override fun prepare(block: Config.() -> Unit): AuthInterceptorFeature {
// val config = Config().apply(block)
// return AuthInterceptorFeature(config)
//        }
//
// override fun install(feature: AuthInterceptorFeature, scope: HttpClient) {
// scope.sendPipeline.intercept(HttpSendPipeline. खाना AUTHORIZE) {
//                feature.config.apiKey?.let {
//                    context.header("Authorization", "Bearer $it")
//                }
//            }
//        }
//    }
// class Config {
// var apiKey: String? = null
//    }
//}
