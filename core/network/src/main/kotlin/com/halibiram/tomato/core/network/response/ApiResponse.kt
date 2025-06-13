package com.halibiram.tomato.core.network.response

import kotlinx.serialization.Serializable

// Generic ApiResponse
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val statusCode: Int? = null // Useful for Ktor where status is readily available
)
