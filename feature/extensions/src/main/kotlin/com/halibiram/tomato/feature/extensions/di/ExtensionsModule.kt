package com.halibiram.tomato.feature.extensions.di

// import android.content.Context
// import com.halibiram.tomato.feature.extensions.engine.ExtensionEngine
// import com.halibiram.tomato.feature.extensions.engine.ExtensionLoader
// import com.halibiram.tomato.feature.extensions.engine.ExtensionSandbox
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.android.qualifiers.ApplicationContext
// import dagger.hilt.components.SingletonComponent
// import javax.inject.Singleton

// @Module
// @InstallIn(SingletonComponent::class)
object ExtensionsModule {

    // @Provides
    // @Singleton
    // fun provideExtensionLoader(@ApplicationContext context: Context): ExtensionLoader {
    //     // Manages loading extension code (e.g., from APKs, DEX files)
    //     return ExtensionLoader(context)
    // }

    // @Provides
    // @Singleton
    // fun provideExtensionSandbox(loader: ExtensionLoader): ExtensionSandbox {
    //     // Manages the environment and security for running extension code
    //     // This is a complex component, likely involving class loaders, permissions etc.
    //     return ExtensionSandbox(loader)
    // }

    // @Provides
    // @Singleton
    // fun provideExtensionEngine(
    //     @ApplicationContext context: Context, // For package manager, etc.
    //     sandbox: ExtensionSandbox
    //     // Add other dependencies like a database for storing extension metadata
    // ): ExtensionEngine {
    //     // Core of the extension system: discovers, loads, manages, and executes extensions
    //     return ExtensionEngine(context, sandbox /*, extensionMetadataDao */)
    // }

    // @Provides
    // fun provideExtensionsRepository(extensionEngine: ExtensionEngine /*, extensionDao: ExtensionDao*/): ExtensionsRepository {
    //     return ExtensionsRepositoryImpl(extensionEngine /*, extensionDao */)
    // }
}
