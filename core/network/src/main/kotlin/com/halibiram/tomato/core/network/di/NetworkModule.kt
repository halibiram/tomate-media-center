package com.halibiram.tomato.core.network.di

import android.content.Context
import com.halibiram.tomato.core.network.client.KtorClient // The class that configures HttpClient
import com.halibiram.tomato.data.remote.api.MovieApi
import com.halibiram.tomato.data.remote.api.SearchApi
import com.halibiram.tomato.data.remote.api.SeriesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android // For Android engine
// import io.ktor.client.engine.cio.CIO // Example for CIO engine if not on Android
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClientEngine(@ApplicationContext context: Context): HttpClientEngine {
        return Android.create {
            // Configure Android engine specific options if needed
            // connectTimeout = 15_000
            // socketTimeout = 15_000
            // Example: SSL pinning, proxy settings
        }
        // return CIO.create { ... } // Alternative for non-Android or testing
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true // Common setting
            isLenient = true // If API might send non-strict JSON
            prettyPrint = false // Save bandwidth in production
            encodeDefaults = true // Depending on API requirements
            // explicitNulls = false // Ktor default is true, check if API omits nulls
        }
    }

    @Provides
    @Singleton
    fun provideKtorClient(engine: HttpClientEngine, json: Json): KtorClient {
        // This provides the KtorClient class itself, which is our configurator/factory
        return KtorClient(engine, json)
    }

    @Provides
    @Singleton
    fun provideHttpClient(ktorClient: KtorClient): HttpClient {
        // Build and provide the actual HttpClient instance
        return ktorClient.build()
    }

    // API Service Implementations
    // These are concrete implementations of the API interfaces defined in the data layer.

    @Provides
    @Singleton
    fun provideMovieApi(httpClient: HttpClient): MovieApi {
        return object : MovieApi {
            override suspend fun getPopularMovies(page: Int) =
                httpClient.get("movie/popular") { parameter("page", page) }.body()

            override suspend fun getTrendingMovies(timeWindow: String) =
                httpClient.get("trending/movie/$timeWindow").body()

            override suspend fun getMovieDetails(movieId: String) =
                httpClient.get("movie/$movieId").body()

            override suspend fun getMovieCredits(movieId: String) =
                httpClient.get("movie/$movieId/credits").body()

            override suspend fun getSimilarMovies(movieId: String) =
                httpClient.get("movie/$movieId/similar").body()
        }
    }

    @Provides
    @Singleton
    fun provideSeriesApi(httpClient: HttpClient): SeriesApi {
        return object : SeriesApi {
            override suspend fun getPopularSeries(page: Int) =
                httpClient.get("tv/popular") { parameter("page", page) }.body()

            override suspend fun getTrendingSeries(timeWindow: String) =
                httpClient.get("trending/tv/$timeWindow").body()

            override suspend fun getSeriesDetails(seriesId: String) =
                httpClient.get("tv/$seriesId").body()

            override suspend fun getSeriesCredits(seriesId: String) =
                httpClient.get("tv/$seriesId/credits").body()

            override suspend fun getSeriesSeasonDetails(seriesId: String, seasonNumber: Int) =
                httpClient.get("tv/$seriesId/season/$seasonNumber").body()

            override suspend fun getEpisodeDetails(seriesId: String, seasonNumber: Int, episodeNumber: Int) =
                httpClient.get("tv/$seriesId/season/$seasonNumber/episode/$episodeNumber").body()
        }
    }

    @Provides
    @Singleton
    fun provideSearchApi(httpClient: HttpClient): SearchApi {
        return object : SearchApi {
            override suspend fun searchMulti(query: String, page: Int) =
                httpClient.get("search/multi") {
                    parameter("query", query)
                    parameter("page", page)
                }.body()
        }
    }
}
