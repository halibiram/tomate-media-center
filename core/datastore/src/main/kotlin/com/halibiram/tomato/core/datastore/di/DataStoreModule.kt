package com.halibiram.tomato.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.halibiram.tomato.core.datastore.preferences.AppPreferences
import com.halibiram.tomato.core.datastore.preferences.PlayerPreferences
import com.halibiram.tomato.core.datastore.preferences.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val TOMATO_PREFERENCES_STORE_NAME = "tomato_preferences"
// If you were migrating from SharedPreferences, you'd specify its name here:
// private const val USER_SHARED_PREFERENCES_NAME = "your_shared_prefs_name_if_migrating"

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            // Example of migrating from SharedPreferences:
            // migrations = listOf(SharedPreferencesMigration(appContext, USER_SHARED_PREFERENCES_NAME)),
            migrations = emptyList(), // No migrations for now
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()), // Define a scope for DataStore operations
            produceFile = { appContext.preferencesDataStoreFile(TOMATO_PREFERENCES_STORE_NAME) }
        )
    }

    @Singleton
    @Provides
    fun provideUserPreferences(dataStore: DataStore<Preferences>): UserPreferences {
        return UserPreferences(dataStore)
    }

    @Singleton
    @Provides
    fun providePlayerPreferences(dataStore: DataStore<Preferences>): PlayerPreferences {
        return PlayerPreferences(dataStore)
    }

    @Singleton
    @Provides
    fun provideAppPreferences(dataStore: DataStore<Preferences>): AppPreferences {
        return AppPreferences(dataStore)
    }
}

// Note: The previous DataStoreModule used `createDataStore(context, name)` which was a bit convoluted.
// The `PreferenceDataStoreFactory.create` is the standard way to create a single DataStore<Preferences> instance
// that will be shared by all preference classes (UserPreferences, PlayerPreferences, AppPreferences).
// Each of these classes will then use their specific keys to read/write from this single DataStore instance.
// This avoids needing named DataStore<Preferences> providers for each type.
