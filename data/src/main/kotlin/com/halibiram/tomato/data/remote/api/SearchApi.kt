package com.halibiram.tomato.data.remote.api

import com.halibiram.tomato.data.remote.dto.PaginatedSearchResponseDto
// import com.halibiram.tomato.data.remote.dto.SearchResultDto // Individual item type

interface SearchApi {
    // query: search query
    // page: page number for pagination
    suspend fun searchMulti(query: String, page: Int): PaginatedSearchResponseDto
}
