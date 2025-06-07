package com.halibiram.tomato.domain.model

data class Extension(
    val id: String, // e.g., package name or a unique identifier
    val name: String,
    val version: String,
    val sourceUrl: String, // URL from where it was downloaded/can be updated
    val description: String?,
    val iconUrl: String?,
    val author: String?,
    val type: ExtensionType, // e.g., VIDEO_SOURCE, SUBTITLE_SOURCE, METADATA_PROVIDER
    val permissions: List<String>, // Permissions the extension requires
    val isEnabled: Boolean
)

enum class ExtensionType {
    VIDEO_SOURCE,
    SUBTITLE_SOURCE,
    METADATA_PROVIDER,
    GENERAL_UTILITY
}
