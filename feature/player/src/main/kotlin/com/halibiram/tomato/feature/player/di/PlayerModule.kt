package com.halibiram.tomato.feature.player.di

// import android.content.Context
// import com.google.android.exoplayer2.ExoPlayer
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.android.components.ViewModelComponent // or ActivityRetainedComponent for broader scope
// import dagger.hilt.android.qualifiers.ApplicationContext
// import dagger.hilt.android.scopes.ViewModelScoped // or ActivityRetainedScoped

// @Module
// @InstallIn(ViewModelComponent::class) // Scope ExoPlayer and related services to ViewModel lifecycle
object PlayerModule {

    // @Provides
    // @ViewModelScoped // ExoPlayer instance will live as long as the ViewModel
    // fun provideExoPlayer(
    //     @ApplicationContext context: Context
    //     // Add other dependencies like RenderersFactory, TrackSelector, LoadControl if needed
    // ): ExoPlayer {
    //     return ExoPlayer.Builder(context).build()
    // }

    // @Provides
    // fun providePlayerRepository(/* ... */): PlayerRepository {
    //     return PlayerRepositoryImpl(/* ... */)
    // }

    // @Provides
    // fun providePlaybackTrackingService(/* ... */): PlaybackTrackingService {
    //     return PlaybackTrackingServiceImpl(/* ... */)
    // }
}
