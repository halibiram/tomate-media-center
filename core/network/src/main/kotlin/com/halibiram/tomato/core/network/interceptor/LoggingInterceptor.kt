package com.halibiram.tomato.core.network.interceptor

import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.statement.HttpResponse
import io.ktor.util.AttributeKey

// Custom Logging Interceptor example (though Ktor has a built-in one)
object TomatoLoggingInterceptor {
    val key = AttributeKey<Unit>("TomatoLoggingInterceptor")

    fun install(client: io.ktor.client.HttpClient) {
        client.sendPipeline.intercept(HttpSendPipeline.Monitoring) {
            // Log request
            // Log response
        }
    }
}

class CustomLogger : Logger {
    override fun log(message: String) {
        println("KtorLogger: $message") // Replace with your actual logger
    }
}
