package com.halibiram.tomato.core.network.interceptor

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header

// AuthInterceptor example for Ktor
object AuthInterceptor {
    fun install(client: io.ktor.client.HttpClient, getToken: suspend () -> String?) {
        client.requestPipeline.intercept(io.ktor.client.request.HttpRequestPipeline.Trước) {
            getToken()?.let { token ->
                context.header("Authorization", "Bearer $token")
            }
            proceed()
        }
    }
}
