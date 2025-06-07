package com.halibiram.tomato.core.network.response

import kotlinx.serialization.Serializable

/**
 * A generic class for API responses, often used with a data field and possibly pagination or metadata.
 * This is a common pattern but might vary based on your specific API structure.
 */
@Serializable
data class ApiResponse<T>(
    val data: T? = null, // The actual payload
    val success: Boolean = true, // Or a status code, e.g., "status": "success"
    val message: String? = null, // Optional message, e.g., for errors or info
    // You might also include pagination details here if your API uses them:
    // val page: Int? = null,
    // val totalResults: Int? = null,
    // val totalPages: Int? = null
)

/**
 * Example of a more specific error response structure if your API has one.
 */
@Serializable
data class ApiErrorResponse(
    val errorCode: String,
    val errorMessage: String,
    val details: List<String>? = null
)
