package com.halibiram.tomato.core.network.util

object NetworkConstants {
    // Replace with your actual base URL
    const val BASE_URL = "https://api.example.com/v1/"

    // API Endpoints (examples)
    const val MOVIES_ENDPOINT = "movies"
    const val SERIES_ENDPOINT = "series"
    const val LOGIN_ENDPOINT = "auth/login"
    const val USER_PROFILE_ENDPOINT = "user/profile"

    // Headers
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"

    // Default values
    const val DEFAULT_REQUEST_TIMEOUT_SECONDS = 30L
    const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 30L
    const val DEFAULT_READ_TIMEOUT_SECONDS = 30L

    // API Keys (Store securely, not directly in code for production)
    // Example: const val API_KEY = "your_api_key_here" // Better to load from build config or secure storage
}
