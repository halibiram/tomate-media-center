package com.halibiram.tomato.domain.model

data class User(
    val id: String, // Unique user ID from the authentication system
    val username: String?, // Display name, might be different from email
    val email: String?, // User's email address, often used for login
    val avatarUrl: String?, // URL to the user's profile picture
    val authToken: String? = null, // Current session token, might be stored separately or not part of User model directly
    val isLoggedIn: Boolean = false // Reflects if the user is currently authenticated
)
