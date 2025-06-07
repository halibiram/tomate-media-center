package com.halibiram.tomato.data.remote.mapper

import com.halibiram.tomato.data.remote.dto.EpisodeDto
import com.halibiram.tomato.domain.model.Episode // Domain model

fun EpisodeDto.toDomain(seriesIdFallback: String? = null): Episode {
    // seriesId might not be part of EpisodeDto if it's nested within a season response.
    // If your API provides series_id directly in EpisodeDto, use that.
    // Otherwise, it might need to be passed down from the parent (Series/Season).
    val sId = seriesIdFallback ?: "UNKNOWN_SERIES_ID" // Placeholder if not available directly

    return Episode(
        id = this.id.toString(),
        seriesId = sId, // This needs careful handling based on API structure
        seasonNumber = this.seasonNumber,
        episodeNumber = this.episodeNumber,
        title = this.name, // DTO uses 'name', Domain uses 'title'
        overview = this.overview,
        airDate = this.airDate,
        stillPath = this.stillPath,
        rating = this.voteAverage
    )
}

fun List<EpisodeDto>.toDomain(seriesIdFallback: String? = null): List<Episode> {
    return this.map { it.toDomain(seriesIdFallback) }
}
