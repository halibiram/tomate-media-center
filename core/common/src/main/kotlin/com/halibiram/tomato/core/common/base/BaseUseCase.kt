package com.halibiram.tomato.core.common.base

// BaseUseCase
interface BaseUseCase<in P, out R> {
    suspend fun execute(params: P): R
}
