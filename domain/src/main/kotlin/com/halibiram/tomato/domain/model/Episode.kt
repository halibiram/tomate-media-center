package com.halibiram.tomato.domain.model

data class Episode(
    val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String, // 'name' in SeriesRepository.kt, standardizing
    val overview: String?,
    val airDate: String?, // Or Date object
    val stillPath: String? = null, // Path for episode image/still
    val rating: Double? = null // Vote average for the episode
)
