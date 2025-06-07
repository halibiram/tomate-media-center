package com.halibiram.tomato.feature.extensions.api

import android.content.Context

/**
 * Defines the contract for extensions.
 * Extensions will implement one or more interfaces derived from this.
 */
interface TomatoExtension {
    /**
     * A unique identifier for the extension (e.g., package name).
     */
    val id: String

    /**
     * Display name of the extension.
     */
    val name: String

    /**
     * Version of the extension.
     */
    val version: String

    /**
     * Version of the ExtensionAPI this extension was built against.
     * Allows the host app to check for compatibility.
     */
    val apiVersion: Int

    /**
     * Called when the extension is first loaded and initialized.
     * @param context The application context, can be used by the extension if needed.
     * @param preferences A dedicated preference store for this extension (optional).
     */
    fun onEnable(context: Context /*, preferences: ExtensionPreferences */) {}

    /**
     * Called when the extension is about to be unloaded or disabled.
     */
    fun onDisable() {}
}

/**
 * Example of a content provider extension.
 * Extensions can implement multiple specialized API interfaces.
 */
interface ContentProviderExtension : TomatoExtension {
    /**
     * Fetches a list of items for a given category.
     * @param categoryId The ID of the category to fetch.
     * @return A list of [ContentItem]s.
     * @throws Exception if fetching fails.
     */
    suspend fun getCategoryItems(categoryId: String): List<ContentItem>

    /**
     * Fetches details for a specific content item.
     * @param itemId The ID of the item to fetch.
     * @return [ContentItemDetails] or null if not found.
     * @throws Exception if fetching fails.
     */
    suspend fun getItemDetails(itemId: String): ContentItemDetails?

    /**
     * Searches for content.
     * @param query The search query.
     * @return A list of [ContentItem]s matching the query.
     * @throws Exception if searching fails.
     */
    suspend fun searchContent(query: String): List<ContentItem>
}

// Data classes for content exchange
data class ContentItem(
    val id: String,
    val title: String,
    val type: ContentType, // e.g., MOVIE, SERIES, EPISODE
    val posterUrl: String?,
    val year: Int? = null
)

data class ContentItemDetails(
    val id: String,
    val title: String,
    val description: String,
    val type: ContentType,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String?, // Or Date object
    val genres: List<String>?,
    val rating: Double?,
    val sources: List<MediaSource> // e.g., different video qualities or servers
)

data class MediaSource(
    val url: String,
    val qualityLabel: String, // e.g., "720p", "1080p"
    val isDirectSource: Boolean = true // True if it's a direct video URL, false if it needs further resolution
)

enum class ContentType {
    MOVIE,
    SERIES,
    EPISODE,
    ANIME, // Example
    OTHER
}

/**
 * Current API version of the host application.
 * Extensions should target this version.
 */
const val CURRENT_EXTENSION_API_VERSION = 1
