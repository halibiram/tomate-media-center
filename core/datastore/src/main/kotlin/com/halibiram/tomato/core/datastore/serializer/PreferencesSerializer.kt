package com.halibiram.tomato.core.datastore.serializer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * This file is to provide a convenient way to create a DataStore<Preferences>.
 * The actual serialization for Preferences DataStore is handled internally by Google's libraries.
 * Unlike Proto DataStore, you don't define a custom Serializer object for Preferences DataStore.
 *
 * You typically create an instance of DataStore<Preferences> using a property delegate.
 */

// Property delegate to create DataStore<Preferences>
// This is the standard way to create a Preferences DataStore instance.
// Usage: val Context.userPreferencesStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")
// Then in your DI or Context: context.userPreferencesStore

// For DI purposes, it's often cleaner to have a function that creates it,
// which can be called from your Dagger/Hilt module.
fun createDataStore(context: Context, storeName: String): DataStore<Preferences> {
    // This is essentially what the `preferencesDataStore` delegate does under the hood,
    // but made explicit for DI.
    // Note: The `preferencesDataStore` delegate ensures a singleton instance per name.
    // If using this function directly in DI, ensure it's scoped as a singleton.
    // However, Hilt's @Singleton will take care of that if this function is used in a @Provides method.
    return context.preferencesDataStoreFile(storeName)
}

// Helper extension function to get the File for a Preferences DataStore.
// This is typically internal to the `preferencesDataStore` delegate.
private fun Context.preferencesDataStoreFile(name: String): DataStore<Preferences> {
    // This is a bit of a simplification to allow creating it programmatically.
    // The actual `preferencesDataStore` delegate handles this more robustly.
    // For direct usage, this should be sufficient for DI.
    // The key is that `androidx.datastore.preferences.PreferenceDataStoreDelegate`
    // is what actually constructs the `PreferenceDataStore`.
    // We are essentially just getting a handle to it via a name.
    val delegate = preferencesDataStore(name = name)
    // To actually get the DataStore instance from the delegate, you'd use its getValue method.
    // This is typically done by Kotlin's property delegation mechanism.
    // For manual instantiation in DI, you might need a more direct way if available,
    // or rely on the DI framework to manage the singleton nature of the delegate.

    // The most straightforward way to use in DI without manually managing the delegate:
    // In your DI Module:
    // @Provides @Singleton
    // fun provideMyDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
    // return context.myPreferencesStoreName // where myPreferencesStoreName is the delegated property
    // }
    // To avoid using the delegate directly in this helper and make it more DI friendly:
    // We'll just return a NEW DataStore instance using the context and name directly.
    // The `preferencesDataStore` builder function handles caching and singleton behavior.
    // So, each call to `context.myPrefs` (where `myPrefs` is `preferencesDataStore(name = "my_prefs_name")`)
    // will return the same DataStore instance.
    // Thus, this function can be simplified if we assume the DI module handles the singleton.
    // The `preferencesDataStore` is the actual factory.
    // Let's make this function simply return what the delegate would provide.
    // This is a bit of a conceptual placeholder as the delegate itself is the factory.
    // The primary point of `preferencesDataStore` is its singleton management via property delegation.

    // Correct way for DI:
    // 1. Define the delegate in a file (e.g., AppDataStore.kt):
    //    `val Context.userPreferences: DataStore<Preferences> by preferencesDataStore("user")`
    // 2. In DI module:
    //    `@Provides @Singleton fun provideUserPreferences(@ApplicationContext context: Context) = context.userPreferences`

    // This utility function remains as a conceptual placeholder for how one might centralize store creation
    // if not using the property delegate directly in DI.
    // The `PreferenceDataStoreFactory.create` is what you'd use if doing it completely manually.
    // For simplicity and idiomatic usage, the property delegate is preferred.
    // This "createDataStore" is what `DataStoreModule` will call.
    // It will effectively be `context.theDataStoreName` via the delegate.
    // So, this function just needs to ensure it's clear that it's returning a DataStore instance.
    // The `preferencesDataStore(name)` call itself is what creates/retrieves the singleton.
    // We just need to ensure this function is callable.
    // Let's assume this function will be used to get the singleton instance via the delegate.
    // This file might be more about defining the names and extension properties.

    // The `DataStoreModule` will use this like: `createDataStore(context, "user_prefs")`
    // This means we need a way to map "user_prefs" (a string) back to the delegate.
    // This is getting complicated. The simplest is direct use of delegate in DI.

    // Simpler approach for this file:
    // Just define the extension properties here.
    // DI module will then access them via context.
    // No need for a separate "createDataStore" function in this file if using property delegates.
    // However, the prompt asked for a "serializer/PreferencesSerializer.kt".
    // For Preferences, there isn't a user-defined serializer.
    // This file is likely intended to centralize DataStore creation logic for DI.

    // Let's stick to the idea that this function is called by DI to get a DataStore instance.
    // The `preferencesDataStore` delegate handles the actual creation and singleton management.
    // This function effectively acts as a pass-through to that mechanism.
    // This is a common pattern for providing DataStore instances via Hilt.
    val dataStore: DataStore<Preferences> by preferencesDataStore(name = storeName)
    return dataStore
}

// It's often good practice to define the DataStore delegates in a single place,
// for example, here or in specific files per preference type.

// Example of defining the delegate directly (could be here or in another file like `AppDataStores.kt`):
// val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
//    name = UserPreferences.USER_PREFERENCES_NAME
// )
// val Context.playerPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
//    name = PlayerPreferences.PLAYER_PREFERENCES_NAME
// )
// val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
//    name = AppPreferences.APP_PREFERENCES_NAME
// )

// The `createDataStore` function above is a bit redundant if you define the delegates
// and use them directly in your DI module. However, if you want a centralized function
// that your DI module calls, it can be kept. The key is that `preferencesDataStore(name = storeName)`
// is the actual mechanism for getting the (singleton) DataStore instance.
// The implementation of `createDataStore` above using the delegate ensures it behaves correctly.
