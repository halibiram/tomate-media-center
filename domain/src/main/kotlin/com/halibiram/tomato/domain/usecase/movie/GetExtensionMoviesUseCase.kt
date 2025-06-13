package com.halibiram.tomato.domain.usecase.movie

import android.util.Log // For logging individual extension errors
import com.halibiram.tomato.core.common.result.Result
import com.halibiram.tomato.core.common.result.TomatoException
import com.halibiram.tomato.core.common.result.UnknownErrorException
import com.halibiram.tomato.core.common.result.ExtensionException // For specific error type
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem
import com.halibiram.tomato.feature.extensions.engine.ExtensionEngine
import javax.inject.Inject

class GetExtensionMoviesUseCase @Inject constructor(
    private val extensionEngine: ExtensionEngine
) {
    private val TAG = "GetExtensionMoviesUseCase"

    /**
     * Fetches popular movies from all enabled MovieProviderExtensions.
     * It flattens successful results. If all extensions fail, it returns the error of the first one.
     * Individual extension failures are logged.
     * @param page The page number for pagination.
     * @return Result containing a list of popular movies from extensions, or an error if all fail.
     */
    suspend fun getPopularMovies(page: Int): Result<List<MovieSourceItem>> {
        return try {
            val resultMap = extensionEngine.getAllPopularMovies(page)
            val successfulMovies = mutableListOf<MovieSourceItem>()
            val encounteredErrors = mutableListOf<Pair<String, TomatoException>>()

            if (resultMap.isEmpty()) { // No active MovieProviderExtensions
                return Result.Success(emptyList())
            }

            resultMap.forEach { (extensionName, result) ->
                when (result) {
                    is Result.Success -> successfulMovies.addAll(result.data)
                    is Result.Error -> {
                        Log.w(TAG, "Error fetching popular movies from extension '$extensionName': ${result.exception.message}")
                        encounteredErrors.add(extensionName to result.exception)
                    }
                    is Result.Loading -> { /* Loading is handled per-extension by engine, not aggregated here */ }
                }
            }

            if (successfulMovies.isNotEmpty()) {
                // If some succeeded, return success with available data.
                // Optionally, report partial failures through a different mechanism if needed by UI.
                if (encounteredErrors.isNotEmpty()) {
                    Log.w(TAG, "${encounteredErrors.size} extension(s) failed to provide popular movies.")
                }
                Result.Success(successfulMovies)
            } else if (encounteredErrors.isNotEmpty()) {
                // All extensions failed, or those that didn't fail returned empty lists.
                // Return the first error encountered, or a generic one.
                val firstError = encounteredErrors.first().second
                Result.Error(ExtensionException(
                    message = "All extensions failed to load popular movies. First error from '${encounteredErrors.first().first}': ${firstError.message}",
                    extensionId = encounteredErrors.first().first, // ID of the first failing extension
                    cause = firstError
                ))
            } else {
                // No errors, but no movies (all extensions returned empty success)
                Result.Success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in getPopularMovies: ${e.message}", e)
            Result.Error(UnknownErrorException("Failed to get popular movies from extensions: ${e.message}", e))
        }
    }

    /**
     * Searches for movies across all enabled MovieProviderExtensions.
     * Similar error handling and result aggregation as getPopularMovies.
     * @param query The search query.
     * @param page The page number for pagination.
     * @return Result containing a list of search results from extensions, or an error if all fail.
     */
    suspend fun searchMovies(query: String, page: Int): Result<List<MovieSourceItem>> {
        return try {
            val resultMap = extensionEngine.searchAllMovies(query, page)
            val successfulMovies = mutableListOf<MovieSourceItem>()
            val encounteredErrors = mutableListOf<Pair<String, TomatoException>>()

            if (resultMap.isEmpty()) { // No active MovieProviderExtensions
                return Result.Success(emptyList())
            }

            resultMap.forEach { (extensionName, result) ->
                when (result) {
                    is Result.Success -> successfulMovies.addAll(result.data)
                    is Result.Error -> {
                        Log.w(TAG, "Error searching movies in extension '$extensionName' for query '$query': ${result.exception.message}")
                        encounteredErrors.add(extensionName to result.exception)
                    }
                    is Result.Loading -> { /* Ignore */ }
                }
            }

            if (successfulMovies.isNotEmpty()) {
                if (encounteredErrors.isNotEmpty()) {
                    Log.w(TAG, "${encounteredErrors.size} extension(s) failed during search for '$query'.")
                }
                Result.Success(successfulMovies)
            } else if (encounteredErrors.isNotEmpty()) {
                val firstError = encounteredErrors.first().second
                Result.Error(ExtensionException(
                    message = "All extensions failed during search for '$query'. First error from '${encounteredErrors.first().first}': ${firstError.message}",
                    extensionId = encounteredErrors.first().first,
                    cause = firstError
                ))
            } else {
                Result.Success(emptyList()) // No results from any extension, but no errors either
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in searchMovies: ${e.message}", e)
            Result.Error(UnknownErrorException("Failed to search movies from extensions for query '$query': ${e.message}", e))
        }
    }
}
