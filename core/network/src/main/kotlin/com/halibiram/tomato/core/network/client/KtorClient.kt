package com.halibiram.tomato.core.network.client

import io.ktor.client.*

// KtorClient wrapper
interface KtorClient {
    fun getHttpClient(): HttpClient
}
