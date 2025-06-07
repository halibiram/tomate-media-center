package com.halibiram.tomato.feature.settings.di

// import android.content.Context
// import androidx.datastore.core.DataStore
// import androidx.datastore.preferences.core.Preferences
// import com.halibiram.tomato.core.datastore.di.DataStoreModule // Assuming you have this from core module
// import com.halibiram.tomato.core.datastore.preferences.AppPreferences
// import com.halibiram.tomato.core.datastore.preferences.PlayerPreferences
// import com.halibiram.tomato.core.datastore.preferences.UserPreferences
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.android.qualifiers.ApplicationContext
// import dagger.hilt.components.SingletonComponent
// import javax.inject.Named
// import javax.inject.Singleton

// @Module
// @InstallIn(SingletonComponent::class) // Or ViewModelComponent if repository is scoped to ViewModel
object SettingsModule {

    // Assuming AppPreferences, PlayerPreferences, UserPreferences classes are defined in core.datastore
    // And DataStore instances are provided by DataStoreModule or similar

    // @Provides
    // @Singleton // Or @ViewModelScoped if preferred
    // fun provideSettingsRepository(
    //     appPreferences: AppPreferences, // Injected from core.datastore
    //     playerPreferences: PlayerPreferences, // Injected from core.datastore
    //     userPreferences: UserPreferences // Injected from core.datastore
    // ): SettingsRepository {
    //     return SettingsRepositoryImpl(appPreferences, playerPreferences, userPreferences)
    // }

    // Example of providing specific preference classes if not already available for injection:
    // This assumes DataStore<Preferences> for each pref type is provided by DataStoreModule

    // @Provides
    // @Singleton
    // fun provideAppPreferences(
    //     @Named(DataStoreModule.APP_PREFS_NAME) // Assuming a named qualifier for app DataStore
    //     dataStore: DataStore<Preferences>
    // ): AppPreferences {
    //     return AppPreferences(dataStore)
    // }

    // @Provides
    // @Singleton
    // fun providePlayerPreferences(
    //     @Named(DataStoreModule.PLAYER_PREFS_NAME) // Assuming a named qualifier
    //     dataStore: DataStore<Preferences>
    // ): PlayerPreferences {
    //     return PlayerPreferences(dataStore)
    // }

    // @Provides
    // @Singleton
    // fun provideUserPreferences(
    //     @Named(DataStoreModule.USER_PREFS_NAME) // Assuming a named qualifier
    //     dataStore: DataStore<Preferences>
    // ): UserPreferences {
    //     return UserPreferences(dataStore)
    // }
}
