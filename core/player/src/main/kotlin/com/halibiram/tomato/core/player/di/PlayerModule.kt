package com.halibiram.tomato.core.player.di

import android.content.Context
import com.halibiram.tomato.core.player.exoplayer.MediaSourceFactory
import com.halibiram.tomato.core.player.exoplayer.PlayerManager
import com.halibiram.tomato.core.player.exoplayer.TomatoExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    // TomatoExoPlayer, MediaSourceFactory, and PlayerManager are already annotated with @Singleton
    // and their dependencies (like @ApplicationContext) are handled by Hilt via @Inject constructor.
    // Thus, Hilt can provide them automatically without explicit @Provides methods here,
    // as long as their constructors are @Inject annotated.

    // However, explicitly providing them here can also be done for clarity or if they lacked @Inject constructors.
    // Assuming they have @Inject constructors and @Singleton annotations on the classes themselves:

    // If TomatoExoPlayer is @Singleton and has @Inject constructor:
    // No explicit @Provides fun provideTomatoExoPlayer() needed.

    // If MediaSourceFactory is @Singleton and has @Inject constructor:
    // No explicit @Provides fun provideMediaSourceFactory() needed.

    // If PlayerManager is @Singleton and has @Inject constructor:
    // No explicit @Provides fun providePlayerManager() needed.

    // For this exercise, let's keep the explicit providers as requested by the prompt structure,
    // even if direct Hilt injection via @Inject constructor would also work for @Singleton classes.

    @Provides
    @Singleton
    fun provideTomatoExoPlayer(@ApplicationContext context: Context): TomatoExoPlayer {
        return TomatoExoPlayer(context)
    }

    @Provides
    @Singleton
    fun provideMediaSourceFactory(@ApplicationContext context: Context): MediaSourceFactory {
        return MediaSourceFactory(context)
    }

    @Provides
    @Singleton
    fun providePlayerManager(
        tomatoExoPlayer: TomatoExoPlayer,
        mediaSourceFactory: MediaSourceFactory
    ): PlayerManager {
        return PlayerManager(tomatoExoPlayer, mediaSourceFactory)
    }
}
