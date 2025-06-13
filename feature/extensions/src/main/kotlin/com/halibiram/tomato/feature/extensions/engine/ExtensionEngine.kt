package com.halibiram.tomato.feature.extensions.engine

import android.content.Context
import android.util.Log
import com.halibiram.tomato.domain.model.Extension // Domain model from repository
import com.halibiram.tomato.domain.repository.ExtensionRepository
import com.halibiram.tomato.feature.extensions.api.ExtensionManifest
import com.halibiram.tomato.feature.extensions.api.MovieProviderExtension
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val extensionLoader: ExtensionLoader,
    private val extensionRepository: ExtensionRepository
) {
    private val TAG = "ExtensionEngine"
    private val activeExtensionInstances = mutableMapOf<String, Any>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        Log.d(TAG, "Initializing ExtensionEngine and starting to load enabled extensions.")
        loadEnabledExtensions()
    }

    private fun loadEnabledExtensions() {
        coroutineScope.launch {
            extensionRepository.getExtensions()
                .catch { e -> Log.e(TAG, "Error collecting extensions from repository: ${e.message}", e) }
                .collectLatest { installedExtensions ->
                    Log.d(TAG, "Received ${installedExtensions.size} installed extensions from repository.")
                    val currentlyActiveIds = activeExtensionInstances.keys.toMutableSet()
                    val newActiveInstances = mutableMapOf<String, Any>()

                    installedExtensions.forEach { domainExtension ->
                        if (domainExtension.isEnabled) {
                            if (activeExtensionInstances.containsKey(domainExtension.id)) {
                                // Already active, move it to new map
                                newActiveInstances[domainExtension.id] = activeExtensionInstances[domainExtension.id]!!
                                Log.d(TAG, "Extension ${domainExtension.name} already active.")
                            } else {
                                Log.i(TAG, "Loading enabled extension: ${domainExtension.name} (ID: ${domainExtension.id})")
                                val manifest = domainExtension.toManifest() // Convert domain model to API manifest
                                val apkPath = domainExtension.sourceUrl // This should be the path to the stored APK

                                if (apkPath.isNullOrBlank()) {
                                    Log.e(TAG, "APK path is missing for extension: ${domainExtension.name}. Cannot load instance.")
                                } else {
                                    // Try loading as MovieProviderExtension for now.
                                    // A more robust system would check registered capabilities from manifest.
                                    val instance = extensionLoader.loadExtensionInstance(
                                        manifest,
                                        apkPath, // Pass the path to the APK
                                        MovieProviderExtension::class.java // Example interface
                                    )
                                    if (instance != null) {
                                        newActiveInstances[domainExtension.id] = instance
                                        Log.i(TAG, "Successfully loaded and activated extension: ${domainExtension.name}")
                                        // Clear any previous loading error for this extension
                                        if (domainExtension.loadingError != null) {
                                            extensionRepository.updateExtensionLoadingError(domainExtension.id, null)
                                        }
                                    } else {
                                        val errorMsg = "Failed to load instance for enabled extension: ${domainExtension.name} from path $apkPath"
                                        Log.w(TAG, errorMsg)
                                        extensionRepository.updateExtensionLoadingError(domainExtension.id, errorMsg)
                                        // Optionally, auto-disable if loading persistently fails and user is notified
                                        // extensionRepository.enableExtension(domainExtension.id, false)
                                    }
                                }
                            }
                        }
                        currentlyActiveIds.remove(domainExtension.id)
                    }

                    // Any IDs left in currentlyActiveIds were uninstalled or disabled.
                    // Their onDisable (if applicable) should be called before removing.
                    currentlyActiveIds.forEach { idToUnload ->
                        Log.i(TAG, "Unloading extension no longer enabled or installed: $idToUnload")
                        // TODO: Call onDisable hook on the instance if it exists, via sandbox
                        // val instanceToDisable = activeExtensionInstances[idToUnload]
                        // instanceToDisable?.onDisable() // Conceptual
                    }

                    // Atomically update active instances
                    activeExtensionInstances.clear()
                    activeExtensionInstances.putAll(newActiveInstances)

                    Log.d(TAG, "Currently active extension instances: ${activeExtensionInstances.keys}")
                }
        }
    }

    private fun Extension.toManifest(): ExtensionManifest {
        val domainExt = this
        return object : ExtensionManifest {
            override val id: String = domainExt.id
            override val name: String = domainExt.name
            override val version: String = domainExt.version
            override val author: String = domainExt.author ?: "Unknown"
            override val description: String? = domainExt.description
            override val apiVersion: Int = domainExt.apiVersion
            // className is crucial for DexClassLoader. It should be part of the domain Extension model,
            // originating from the parsed manifest and stored in ExtensionEntity.
            // For now, if it's not in domain.Extension, this will be an issue.
            // Let's assume domain.Extension needs a 'className' field.
            // If domain.Extension doesn't have it, we can't load.
            // className is now part of the Extension domain model
            override val className: String = domainExt.className
        }
    }

    suspend fun getAllPopularMovies(page: Int): Map<String, Result<List<MovieSourceItem>>> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, Result<List<MovieSourceItem>>>()
        Log.d(TAG, "Fetching popular movies from ${activeExtensionInstances.size} active instances.")
        activeExtensionInstances.forEach { (id, instance) ->
            if (instance is MovieProviderExtension) {
                Log.d(TAG, "Calling getPopularMovies on extension: $id (${instance.name})")
                try {
                    val movies = instance.getPopularMovies(page)
                    results[instance.name] = com.halibiram.tomato.core.common.result.Result.Success(movies)
                    Log.d(TAG, "Extension $id (${instance.name}) returned ${movies.size} popular movies.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling getPopularMovies on extension $id (${instance.name}): ${e.message}", e)
                    results[instance.name] = com.halibiram.tomato.core.common.result.Result.Error(
                        com.halibiram.tomato.core.common.result.ExtensionException("Failed to get popular movies from ${instance.name}", extensionId = id, cause = e)
                    )
                }
            }
        }
        Log.d(TAG, "Finished fetching popular movies. Results: ${results.mapValues { entry -> if (entry.value is com.halibiram.tomato.core.common.result.Result.Success<*>) (entry.value as com.halibiram.tomato.core.common.result.Result.Success<List<MovieSourceItem>>).data.size else "Error" }}")
        return@withContext results
    }

    suspend fun searchAllMovies(query: String, page: Int): Map<String, Result<List<MovieSourceItem>>> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, Result<List<MovieSourceItem>>>()
        Log.d(TAG, "Searching movies for query '$query' from ${activeExtensionInstances.size} active instances.")
        activeExtensionInstances.forEach { (id, instance) ->
            if (instance is MovieProviderExtension) {
                 Log.d(TAG, "Calling searchMovies on extension: $id (${instance.name})")
                try {
                    val movies = instance.searchMovies(query, page)
                    results[instance.name] = com.halibiram.tomato.core.common.result.Result.Success(movies)
                    Log.d(TAG, "Extension $id (${instance.name}) returned ${movies.size} movies for query '$query'.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling searchMovies on extension $id (${instance.name}): ${e.message}", e)
                     results[instance.name] = com.halibiram.tomato.core.common.result.Result.Error(
                        com.halibiram.tomato.core.common.result.ExtensionException("Search failed in ${instance.name}", extensionId = id, cause = e)
                    )
                }
            }
        }
         Log.d(TAG, "Finished searching movies. Results: ${results.mapValues { entry -> if (entry.value is com.halibiram.tomato.core.common.result.Result.Success<*>) (entry.value as com.halibiram.tomato.core.common.result.Result.Success<List<MovieSourceItem>>).data.size else "Error" }}")
        return@withContext results
    }

    inline fun <reified T : Any> getLoadedExtensionInstance(extensionId: String): T? {
        val instance = activeExtensionInstances[extensionId]
        return if (instance is T) {
            instance
        } else {
            Log.w(TAG, "Extension instance with ID '$extensionId' not found or not of type ${T::class.java.simpleName}")
            null
        }
    }
}
