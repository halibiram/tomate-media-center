package com.halibiram.tomato.domain.usecase.series

import com.halibiram.tomato.domain.repository.Episode
import com.halibiram.tomato.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEpisodesUseCase @Inject constructor(
    private val seriesRepository: SeriesRepository
) {
    /**
     * Fetches episodes for a specific season of a series.
     * @param seriesId The ID of the series.
     * @param seasonNumber The season number.
     * @return A Flow emitting a list of episodes.
     */
    operator fun invoke(seriesId: String, seasonNumber: Int): Flow<List<Episode>> {
        return seriesRepository.getEpisodesForSeason(seriesId, seasonNumber)
    }
}
