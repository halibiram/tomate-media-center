package com.halibiram.tomato.feature.extensions.di

import android.content.Context
import com.halibiram.tomato.domain.repository.ExtensionRepository // For ExtensionEngine
import com.halibiram.tomato.feature.extensions.engine.ExtensionEngine
import com.halibiram.tomato.feature.extensions.engine.ExtensionLoader
// import com.halibiram.tomato.feature.extensions.engine.ExtensionSandbox // Sandbox is not used in this step
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExtensionsModule {

    @Provides
    @Singleton
    fun provideExtensionLoader(@ApplicationContext context: Context): ExtensionLoader {
        // ExtensionLoader is already @Singleton and has @Inject constructor
        // Hilt can provide it automatically. Explicitly providing is also fine.
        return ExtensionLoader(context)
    }

    // ExtensionSandbox provider can be added here when it's fully implemented and needed by Engine
    // @Provides
    // @Singleton
    // fun provideExtensionSandbox(loader: ExtensionLoader, @ApplicationContext context: Context): ExtensionSandbox {
    //     return ExtensionSandbox(context, loader)
    // }

    @Provides
    @Singleton
    fun provideExtensionEngine(
        @ApplicationContext context: Context, // Context might be used by Engine directly or passed to components
        extensionLoader: ExtensionLoader,
        extensionRepository: ExtensionRepository // Engine needs this to observe installed/enabled extensions
        // extensionSandbox: ExtensionSandbox // Add when sandbox is used
    ): ExtensionEngine {
        // ExtensionEngine is already @Singleton and has @Inject constructor
        return ExtensionEngine(context, extensionLoader, extensionRepository /*, extensionSandbox */)
    }

    // ExtensionsRepository is typically an interface, its implementation (e.g., ExtensionRepositoryImpl)
    // would be provided in the data layer's DI module.
    // If ExtensionsRepositoryImpl itself needs specific dependencies for this feature module,
    // those could be provided here, but usually, it's in data layer DI.
    // For now, assuming ExtensionsRepository is provided elsewhere (e.g., data.di.RepositoryModule)
}
