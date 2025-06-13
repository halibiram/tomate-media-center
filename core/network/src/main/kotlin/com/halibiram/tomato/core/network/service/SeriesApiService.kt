package com.halibiram.tomato.core.network.service

import com.halibiram.tomato.core.network.dto.EpisodeDto
import com.halibiram.tomato.core.network.dto.SeriesDto
import com.halibiram.tomato.core.network.model.ApiResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject

class SeriesApiService @Inject constructor(
    httpClient: HttpClient
) : BaseApiService(httpClient) {

    // Example: override val baseUrl: String = "https://api.themoviedb.org/3/"

    suspend fun getPopularSeries(page: Int, apiKey: String): ApiResponse<List<SeriesDto>> {
        return safeApiCall {
            url("${super.baseUrl}tv/popular")
            method = HttpMethod.Get
            parameter("api_key", apiKey)
            parameter("page", page)
        }
    }

    suspend fun getSeriesDetails(seriesId: String, apiKey: String): ApiResponse<SeriesDto> {
        return safeApiCall {
            url("${super.baseUrl}tv/$seriesId")
            method = HttpMethod.Get
            parameter("api_key", apiKey)
        }
    }

    suspend fun getEpisodesForSeason(seriesId: String, seasonNumber: Int, apiKey: String): ApiResponse<List<EpisodeDto>> {
         return safeApiCall {
            url("${super.baseUrl}tv/$seriesId/season/$seasonNumber")
            method = HttpMethod.Get
            parameter("api_key", apiKey)
        }
    }

    suspend fun searchSeries(query: String, page: Int, apiKey: String): ApiResponse<List<SeriesDto>> {
         return safeApiCall {
            url("${super.baseUrl}search/tv")
            method = HttpMethod.Get
            parameter("api_key", apiKey)
            parameter("query", query)
            parameter("page", page)
        }
    }
}
