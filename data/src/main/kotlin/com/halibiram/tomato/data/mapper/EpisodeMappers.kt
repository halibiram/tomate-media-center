package com.halibiram.tomato.data.mapper

import com.halibiram.tomato.core.database.entity.EpisodeEntity
import com.halibiram.tomato.core.network.dto.EpisodeDto
import com.halibiram.tomato.domain.model.Episode

fun EpisodeDto.toDomain(seriesIdParam: String): Episode {
    return Episode(
        id = this.id,
        seriesId = seriesIdParam,
        seasonNumber = this.seasonNumber,
        episodeNumber = this.episodeNumber,
        name = this.name,
        overview = this.overview,
        stillPath = this.stillPath,
        airDate = this.airDate
    )
}

fun EpisodeDto.toEntity(seriesIdParam: String): EpisodeEntity {
    return EpisodeEntity(
        id = this.id,
        seriesId = seriesIdParam,
        seasonNumber = this.seasonNumber,
        episodeNumber = this.episodeNumber,
        name = this.name,
        overview = this.overview,
        stillPath = this.stillPath,
        airDate = this.airDate
    )
}

fun EpisodeEntity.toDomain(): Episode {
    return Episode(
        id = this.id,
        seriesId = this.seriesId,
        seasonNumber = this.seasonNumber,
        episodeNumber = this.episodeNumber,
        name = this.name,
        overview = this.overview,
        stillPath = this.stillPath,
        airDate = this.airDate
    )
}
