package com.halibiram.tomato.data.di

// import com.halibiram.tomato.data.repository.YourRepositoryImpl
// import com.halibiram.tomato.domain.repository.YourRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Example of binding a repository interface to its implementation
    /*
    @Binds
    @Singleton
    abstract fun bindYourRepository(
        yourRepositoryImpl: YourRepositoryImpl
    ): YourRepository
    */

    // Add more repository bindings here
}
