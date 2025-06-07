package com.halibiram.tomato.core.network.di

import android.content.Context
import com.halibiram.tomato.core.network.BuildConfig // Assuming this exists
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(@ApplicationContext context: Context): HttpClient {
        // It's better to get the debug flag from the application module's BuildConfig
        // For now, we'll assume a BuildConfig exists in core.network or it's handled.
        val isDebug = try {
            BuildConfig.DEBUG
        } catch (e: ClassNotFoundException) {
            // Fallback if core.network.BuildConfig is not found
            // Consider a more robust way to pass this, e.g., from app module's BuildConfig
            // For now, defaulting to false if not found.
            // This can be injected from the app module if necessary.
            false
        }


        return HttpClient(Android) {
            expectSuccess = true

            engine {
                // connectTimeout = 15_000
                // socketTimeout = 15_000
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
                        Timber.tag("HttpClient").d(message)
                    }
                }
                level = if (isDebug) LogLevel.ALL else LogLevel.NONE
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                // Add other default headers if needed
            }
        }
    }
}
