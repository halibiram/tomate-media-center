package com.halibiram.tomato.core.network.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// HttpClientFactory
object HttpClientFactory {
    fun create(): HttpClient {
        return HttpClient(CIO) { // Or Android, OkHttp, etc.
            engine {
                // CIO specific settings
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.HEADERS
            }
            // Add other plugins like Auth, defaultRequest, etc.
        }
    }
}

class DefaultKtorClient : KtorClient {
    override fun getHttpClient(): HttpClient = HttpClientFactory.create()
}
