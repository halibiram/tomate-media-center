package com.halibiram.tomato.core.network.di

import com.halibiram.tomato.core.network.service.MovieApiService
import com.halibiram.tomato.core.network.service.SeriesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_MS = 15_000L

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("KtorHttpClient").d(message)
                    }
                }
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MS
                connectTimeoutMillis = TIMEOUT_MS
                socketTimeoutMillis = TIMEOUT_MS
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
                modifyRequest { request ->
                    request.headers.append("X-Retry-Count", (request.attributes.getOrNull(HttpRequestRetry.RetryAttributeKey) ?: 0).toString())
                }
            }
            install(UserAgent) {
                agent = "TomatoApp/1.0 KtorClient"
            }
        }
    }

    @Provides
    @Singleton
    fun provideMovieApiService(httpClient: HttpClient): MovieApiService {
        return MovieApiService(httpClient)
    }

    @Provides
    @Singleton
    fun provideSeriesApiService(httpClient: HttpClient): SeriesApiService {
        return SeriesApiService(httpClient)
    }
}
