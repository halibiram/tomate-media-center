package com.halibiram.tomato.feature.downloads.di

// import android.content.Context
// import androidx.work.WorkManager
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.android.qualifiers.ApplicationContext
// import dagger.hilt.components.SingletonComponent
// import javax.inject.Singleton

// @Module
// @InstallIn(SingletonComponent::class)
object DownloadsModule {

    // @Provides
    // @Singleton
    // fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
    //     return WorkManager.getInstance(context)
    // }

    // @Provides
    // fun provideDownloadsRepository(
    //     downloadDao: com.halibiram.tomato.core.database.dao.DownloadDao,
    //     // Provide other dependencies like a service for fetching download URLs if needed
    // ): DownloadsRepository {
    //     return DownloadsRepositoryImpl(downloadDao /* ... */)
    // }

    // @Provides
    // fun provideDownloadService(
    //     workManager: WorkManager,
    //     // httpClient: io.ktor.client.HttpClient // For direct downloads if not using WorkManager for everything
    // ): DownloadService {
    //     return DownloadServiceImpl(workManager /* ... */)
    // }
}
