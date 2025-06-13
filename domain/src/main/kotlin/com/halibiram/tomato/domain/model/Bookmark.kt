package com.halibiram.tomato.domain.model

data class Bookmark(
    val mediaId: String,
    val mediaType: MediaType, // Re-using MediaType from Download.kt or define a specific one
    val bookmarkedAt: Long // Timestamp
)
