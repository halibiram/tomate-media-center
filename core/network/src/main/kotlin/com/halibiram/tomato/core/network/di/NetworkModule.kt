package com.halibiram.tomato.core.network.di

import com.halibiram.tomato.core.network.client.HttpClientFactory
import com.halibiram.tomato.core.network.client.KtorClient
import io.ktor.client.HttpClient

// Placeholder for Dagger/Hilt module
object NetworkModule {

    // @Provides
    // @Singleton
    fun provideHttpClientFactory(): HttpClientFactory {
        return HttpClientFactory()
    }

    // @Provides
    // @Singleton
    fun provideKtorClient(factory: HttpClientFactory): KtorClient {
        return KtorClient(factory.create())
    }

    // @Provides
    // @Singleton
    fun provideBaseHttpClient(ktorClient: KtorClient): HttpClient {
        return ktorClient.httpClient
    }
}
