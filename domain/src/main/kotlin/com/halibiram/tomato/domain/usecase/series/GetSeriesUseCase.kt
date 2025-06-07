package com.halibiram.tomato.domain.usecase.series

import com.halibiram.tomato.domain.repository.Series
import com.halibiram.tomato.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSeriesUseCase @Inject constructor(
    private val seriesRepository: SeriesRepository
) {
    /**
     * Fetches popular series.
     * @param page The page number to retrieve.
     * @return A Flow emitting a list of popular series.
     */
    operator fun invoke(page: Int): Flow<List<Series>> {
        return seriesRepository.getPopularSeries(page)
    }

    // You might add other methods or specific use cases like:
    // fun getTrendingSeries(page: Int): Flow<List<Series>> { ... }
    // fun getSeriesDetails(seriesId: String): Flow<SeriesDetails?> { ... }
}
