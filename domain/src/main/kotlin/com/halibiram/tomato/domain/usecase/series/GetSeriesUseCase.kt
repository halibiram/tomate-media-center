package com.halibiram.tomato.domain.usecase.series

import com.halibiram.tomato.domain.model.Series
import com.halibiram.tomato.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSeriesUseCase @Inject constructor(
    private val seriesRepository: SeriesRepository
) {
    operator fun invoke(page: Int): Flow<List<Series>> { // Get popular series
        return seriesRepository.getPopularSeries(page)
    }

    fun getDetails(seriesId: String): Flow<Series?> { // Get series details
        return seriesRepository.getSeriesDetails(seriesId)
    }
}
