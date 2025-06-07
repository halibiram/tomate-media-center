package com.halibiram.tomato.domain.model

data class User(
    val id: String,
    val username: String?,
    val email: String?,
    val avatarUrl: String?,
    val isLoggedIn: Boolean,
    val preferences: UserPreferences? // Optional detailed preferences object
)

data class UserPreferences(
    val theme: String, // "light", "dark", "system"
    val preferredSubtitleLanguage: String?,
    // Add other user-specific preferences
)
