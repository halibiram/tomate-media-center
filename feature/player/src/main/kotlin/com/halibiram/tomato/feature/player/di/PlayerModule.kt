package com.halibiram.tomato.feature.player.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent // Or other appropriate component

@Module
@InstallIn(ViewModelComponent::class) // Example: if you were providing dependencies scoped to ViewModels
object PlayerModule {

    // This module can be used to provide dependencies specific to the Player feature
    // that are not already covered by other modules (like core.player.di.PlayerModule).
    // For example, if PlayerViewModel had other dependencies specific to this feature's logic.

    // If PlayerViewModel is @HiltViewModel and its dependencies (PlayerManager, SavedStateHandle)
    // are already provided by Hilt (e.g., PlayerManager from core.player.di.PlayerModule,
    // SavedStateHandle automatically by Hilt), then this module might not need
    // explicit @Provides methods for the ViewModel itself or its direct core dependencies.

    // Placeholder for any feature-specific bindings if needed in the future.
    // @Provides
    // @ViewModelScoped
    // fun provideSomePlayerFeatureSpecificService(): SomePlayerFeatureSpecificService {
    //     return SomePlayerFeatureSpecificServiceImpl()
    // }
}
