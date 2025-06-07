package com.halibiram.tomato.data.remote.api

import com.halibiram.tomato.data.remote.dto.CreditsDto
import com.halibiram.tomato.data.remote.dto.EpisodeDto
import com.halibiram.tomato.data.remote.dto.PaginatedSeriesResponseDto
import com.halibiram.tomato.data.remote.dto.SeriesDto
import com.halibiram.tomato.data.remote.dto.SeasonDto // Full Season DTO, not just list
import com.halibiram.tomato.data.remote.dto.SeasonDetailsWithEpisodesDto // For season details with episodes

interface SeriesApi {
    suspend fun getPopularSeries(page: Int): PaginatedSeriesResponseDto

    suspend fun getTrendingSeries(timeWindow: String): PaginatedSeriesResponseDto // day or week

    suspend fun getSeriesDetails(seriesId: String): SeriesDto

    suspend fun getSeriesCredits(seriesId: String): CreditsDto

    // This should probably return a more detailed Season DTO, potentially including list of episodes
    suspend fun getSeriesSeasonDetails(seriesId: String, seasonNumber: Int): SeasonDetailsWithEpisodesDto

    suspend fun getEpisodeDetails(seriesId: String, seasonNumber: Int, episodeNumber: Int): EpisodeDto
}
