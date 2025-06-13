package com.example.dummysuccess // Matches package name in manifest

import com.halibiram.tomato.feature.extensions.api.ExtensionManifest
import com.halibiram.tomato.feature.extensions.api.MovieProviderExtension
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem
import com.halibiram.tomato.feature.extensions.api.CURRENT_HOST_EXTENSION_API_VERSION

// This class would be in the separate APK.
// For DexClassLoader, a no-argument constructor is the simplest to instantiate.
class SuccessProvider : MovieProviderExtension {

    // These details will be primarily read from the APK's AndroidManifest.xml by the ExtensionLoader.
    // Implementing them here provides a fallback or can be used for direct instantiation if this class
    // were ever part of the main app's classpath (which it won't be when loaded from an external APK).
    // The ExtensionLoader will prioritize manifest values.
    override val id: String get() = "com.example.dummysuccess" // Should match package name declared in its AndroidManifest
    override val name: String get() = "Dummy Success Extension (from APK Code)" // Host app will use manifest value
    override val version: String get() = "1.0.0-apk" // Host app will use manifest value
    override val author: String get() = "Tomato App (APK Code)" // Host app will use manifest value
    override val description: String? get() = "A real APK extension (code) providing dummy success movies."
    override val apiVersion: Int get() = CURRENT_HOST_EXTENSION_API_VERSION // Important for compatibility
    override val className: String get() = "com.example.dummysuccess.SuccessProvider" // Its own fully qualified name

    override suspend fun getPopularMovies(page: Int): List<MovieSourceItem> {
        // Simulate some work/delay
        kotlinx.coroutines.delay(50)
        return listOf(
            MovieSourceItem("pop_s_apk_1", "APK Popular Movie 1 ($name)", "poster_apk_1.jpg", "2023"),
            MovieSourceItem("pop_s_apk_2", "APK Popular Movie 2 ($name)", "poster_apk_2.jpg", "2024")
        )
    }

    override suspend fun searchMovies(query: String, page: Int): List<MovieSourceItem> {
        kotlinx.coroutines.delay(20)
        return if (query.contains("success_apk", ignoreCase = true) || query.contains(name, ignoreCase = true)) {
            listOf(MovieSourceItem("search_s_apk_1", "Found APK Success Movie for '$query'", null, "2023"))
        } else emptyList()
    }
}
