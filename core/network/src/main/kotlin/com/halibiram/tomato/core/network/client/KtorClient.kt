package com.halibiram.tomato.core.network.client

import io.ktor.client.HttpClient

// Wrapper class for Ktor HttpClient
// In a real app, this might be an interface if you want to swap implementations
class KtorClient(val httpClient: HttpClient) {
    // You can add common request methods here if needed
    // e.g., suspend inline fun <reified T> get(url: String): T = httpClient.get(url).body()
}
