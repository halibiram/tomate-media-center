package com.halibiram.tomato.domain.model

data class Episode(
    val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val airDate: String?, // Or a Date/LocalDate type
    val overview: String?,
    val stillPath: String? // Path for episode thumbnail
)
