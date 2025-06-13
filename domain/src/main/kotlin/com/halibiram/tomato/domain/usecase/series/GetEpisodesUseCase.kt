package com.halibiram.tomato.domain.usecase.series

import com.halibiram.tomato.domain.model.Episode
import com.halibiram.tomato.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEpisodesUseCase @Inject constructor(
    private val seriesRepository: SeriesRepository
) {
    operator fun invoke(seriesId: String, seasonNumber: Int): Flow<List<Episode>> {
        return seriesRepository.getEpisodesForSeries(seriesId, seasonNumber)
    }
}
