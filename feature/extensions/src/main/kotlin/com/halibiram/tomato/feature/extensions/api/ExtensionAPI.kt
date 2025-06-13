package com.halibiram.tomato.feature.extensions.api

// Basic information about the extension, expected to be provided by the extension's manifest
interface ExtensionManifest {
    val id: String // Unique ID, e.g., package name or a custom unique string
    val name: String
    val version: String // e.g., "1.0.2"
    val author: String
    val description: String?
    val apiVersion: Int // Version of the Tomato Extension API it targets
    val className: String // Fully qualified name of the main extension class implementing an API (e.g., MovieProviderExtension)
    // val iconUrl: String? // Optional: URL to an icon for the extension. Could be a resource name too.
    // val permissionsRequired: List<String>? // Optional: list of permissions extension needs
}

// Example: An extension that provides a list of movies
interface MovieProviderExtension : ExtensionManifest {
    suspend fun getPopularMovies(page: Int): List<MovieSourceItem>
    suspend fun searchMovies(query: String, page: Int): List<MovieSourceItem>
    // suspend fun getMovieDetails(movieId: String): MovieSourceItemDetails?
}

data class MovieSourceItem(
    val id: String,
    val title: String,
    val posterUrl: String?,
    val year: String?,
    val sourceData: Map<String, String> = emptyMap()
)

const val CURRENT_HOST_EXTENSION_API_VERSION = 1
