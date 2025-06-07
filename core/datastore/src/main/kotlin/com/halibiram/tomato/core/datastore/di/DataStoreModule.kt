package com.halibiram.tomato.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.halibiram.tomato.core.datastore.preferences.AppPreferences // Assuming you'll create this
import com.halibiram.tomato.core.datastore.preferences.PlayerPreferences // Assuming you'll create this
import com.halibiram.tomato.core.datastore.preferences.UserPreferences // Assuming you'll create this
import com.halibiram.tomato.core.datastore.serializer.createDataStore // Assuming this helper

// Placeholder for Dagger/Hilt module
object DataStoreModule {

    // @Provides
    // @Singleton
    fun provideUserPreferencesDataStore(
        // @ApplicationContext
        context: Context
    ): DataStore<Preferences> {
        // The name "user_preferences.pb" is conventional for Proto DataStore,
        // but for Preferences DataStore, it's just a name for the file.
        // Using .preferences_pb or just .preferences is common.
        return createDataStore(context, UserPreferences.USER_PREFERENCES_NAME)
    }

    // Example: If you had separate DataStores for different features
    // @Provides
    // @Singleton
    // @Named("player_prefs") // Example of qualifying if you have multiple Preferences DataStores
    fun providePlayerPreferencesDataStore(
        // @ApplicationContext
        context: Context
    ): DataStore<Preferences> {
        return createDataStore(context, PlayerPreferences.PLAYER_PREFERENCES_NAME)
    }

    // @Provides
    // @Singleton
    // @Named("app_prefs")
    fun provideAppPreferencesDataStore(
        // @ApplicationContext
        context: Context
    ): DataStore<Preferences> {
        return createDataStore(context, AppPreferences.APP_PREFERENCES_NAME)
    }
}
