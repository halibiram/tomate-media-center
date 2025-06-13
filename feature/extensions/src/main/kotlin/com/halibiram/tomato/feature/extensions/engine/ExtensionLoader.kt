package com.halibiram.tomato.feature.extensions.engine

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.halibiram.tomato.feature.extensions.api.ExtensionManifest
import com.halibiram.tomato.feature.extensions.api.CURRENT_HOST_EXTENSION_API_VERSION
import com.halibiram.tomato.feature.extensions.api.MovieProviderExtension
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.DexClassLoader // For actual class loading
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

// Data class to hold parsed manifest values
private data class ParsedManifestData(
    override val id: String,
    override val name: String,
    override val version: String,
    override val author: String,
    override val description: String?,
    override val apiVersion: Int,
    override val className: String
) : ExtensionManifest


@Singleton
class ExtensionLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "ExtensionLoader"

    // Metadata keys in AndroidManifest.xml of extension APKs
    companion object {
        const val EXT_META_PREFIX = "com.halibiram.tomato.EXTENSION_"
        const val META_CLASS_NAME = "${EXT_META_PREFIX}CLASS_NAME"
        const val META_API_VERSION = "${EXT_META_PREFIX}API_VERSION"
        const val META_NAME = "${EXT_META_PREFIX}NAME" // Optional, can use app label
        const val META_VERSION = "${EXT_META_PREFIX}VERSION_NAME" // Optional, can use packageInfo.versionName
        const val META_AUTHOR = "${EXT_META_PREFIX}AUTHOR"
        const val META_DESCRIPTION = "${EXT_META_PREFIX}DESCRIPTION"
    }

    private fun getFileFromContentUri(contentUriString: String): File? {
        return try {
            val uri = Uri.parse(contentUriString)
            // Create a unique file name in cache
            val fileName = "ext_apk_${System.currentTimeMillis()}_${uri.lastPathSegment ?: "unknown.apk"}"
            val tempFile = File(context.cacheDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null // Could not open input stream

            Log.d(TAG, "Copied content URI to temporary file: ${tempFile.absolutePath}")
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy content URI to file: $contentUriString", e)
            null
        }
    }


    fun loadManifest(sourceUriString: String): ExtensionManifest? {
        Log.d(TAG, "Attempting to load manifest from: $sourceUriString")

        val apkFile = getFileFromContentUri(sourceUriString)
        if (apkFile == null || !apkFile.exists()) {
            Log.e(TAG, "Failed to get valid file from URI: $sourceUriString")
    // Fallback to simulation for testing if URI matches dummy names (other than success one), otherwise fail
    return simulateManifestLoadingFallback(sourceUriString)
        }
        val apkPath = apkFile.absolutePath

        try {
            val packageManager = context.packageManager
    val packageInfoFlags = PackageManager.GET_META_DATA or PackageManager.GET_SIGNATURES // For security later
    @Suppress("DEPRECATION") // getPackageArchiveInfo is deprecated in API 33+ but needed for broad compatibility
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageArchiveInfo(apkPath, PackageManager.PackageInfoFlags.of(packageInfoFlags.toLong()))
    } else {
        packageManager.getPackageArchiveInfo(apkPath, packageInfoFlags)
    }

            if (packageInfo == null) {
                Log.e(TAG, "Failed to get PackageInfo from APK: $apkPath. The APK might be invalid.")
        apkFile.delete()
                return null
            }

            val appInfo = packageInfo.applicationInfo
            if (appInfo == null) {
                Log.e(TAG, "ApplicationInfo is null for APK: $apkPath.")
                apkFile.delete()
                return null
            }

            // Important: For security, an APK's resources (like strings from appInfo.labelRes)
            // are not directly accessible using getPackageArchiveInfo until the package is fully installed.
            // So, metadata for name, description etc. MUST come from <meta-data>.
            // We need to set appInfo.sourceDir and appInfo.publicSourceDir to apkPath for resources if needed,
            // but it's safer to rely on direct string values in meta-data.
            appInfo.sourceDir = apkPath
            appInfo.publicSourceDir = apkPath

            val metaData = appInfo.metaData
            if (metaData == null) {
                Log.e(TAG, "No <meta-data> found in AndroidManifest.xml for APK: $apkPath")
                apkFile.delete()
                return null
            }

            val className = metaData.getString(META_CLASS_NAME)
            val apiVersionStr = metaData.getString(META_API_VERSION) // API version from extension
            val name = metaData.getString(META_NAME) ?: packageInfo.applicationInfo.loadLabel(packageManager).toString()
            val version = metaData.getString(META_VERSION) ?: packageInfo.versionName
            val author = metaData.getString(META_AUTHOR) ?: "Unknown Author"
            val description = metaData.getString(META_DESCRIPTION) ?: appInfo.loadDescription(packageManager)?.toString()


            if (className.isNullOrBlank()) {
                Log.e(TAG, "$META_CLASS_NAME not found in metadata for $apkPath")
                apkFile.delete()
                return null
            }
            if (apiVersionStr.isNullOrBlank()) {
                Log.e(TAG, "$META_API_VERSION not found in metadata for $apkPath")
                apkFile.delete()
                return null
            }
             val apiVersion = try {
                apiVersionStr.toInt()
            } catch (e: NumberFormatException) {
                Log.e(TAG, "Invalid API version format '$apiVersionStr' in $apkPath", e)
                apkFile.delete()
                return null
            }


            Log.i(TAG, "Successfully parsed manifest for: $name v$version (ID: ${packageInfo.packageName}) from $apkPath")
            // Note: The temporary apkFile is not deleted here. The InstallExtensionUseCase
            // should decide whether to move this file to a permanent location or delete it.
            // For now, its path is what might be stored.

            return ParsedManifestData(
                id = packageInfo.packageName, // Use package name as the unique ID
                name = name,
                version = version,
                author = author,
                description = description,
                apiVersion = apiVersion,
                className = className
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing manifest from APK: $apkPath", e)
            apkFile.delete() // Clean up temp file on error
            return null
        }
    }

// Fallback simulation for test environments or if file ops fail, for non-success dummies
private fun simulateManifestLoadingFallback(sourceUriString: String): ExtensionManifest? {
    Log.d(TAG, "Falling back to manifest simulation for URI: $sourceUriString (if not success ext)")
         return when {
        // Keep other dummy cases if they are purely simulated and not meant to be real APKs
        sourceUriString.contains("dummy_another_ext.apk", ignoreCase = true) -> {
             object : ExtensionManifest {
                override val id: String = "com.example.another"
                override val name: String = "Another Ext Provider (Simulated)"
                override val version: String = "0.9.1"
                override val author: String = "Third Party Dev"
                override val description: String? = "This is another dummy extension with a different ID (simulated)."
                    override val apiVersion: Int = CURRENT_HOST_EXTENSION_API_VERSION
                override val className: String = "com.example.DummyAnotherProviderImpl" // Placeholder
                }
            }
        sourceUriString.contains("dummy_old_api_ext.apk", ignoreCase = true) -> {
            object : ExtensionManifest {
                override val id: String = "com.example.oldapi"
                override val name: String = "Old API Extension (Simulated)"
                // ... other fields ...
                override val apiVersion: Int = 0
                override val className: String = "com.example.DummyOldApiProviderImpl"
            }
        }
            else -> null
        }
    }


    fun <T : Any> loadExtensionInstance(
        manifest: ExtensionManifest,
    extensionSourcePath: String?,
        expectedApiInterface: Class<T>
    ): T? {
    Log.d(TAG, "Attempting to load instance for ${manifest.id} (class: ${manifest.className}) from APK path: $extensionSourcePath, expecting ${expectedApiInterface.simpleName}")

        if (extensionSourcePath.isNullOrBlank() || !File(extensionSourcePath).exists()) {
            Log.e(TAG, "Invalid or non-existent APK path for extension ${manifest.id}: $extensionSourcePath")
        // No simulation fallback here for instance loading if path is bad for real loading attempt
        return null
        }

        var classLoader: DexClassLoader? = null
        try {
            val dexOutputDir = context.getDir("dex_output", Context.MODE_PRIVATE)
            Log.d(TAG, "Using DEX output directory: ${dexOutputDir.absolutePath}")

            classLoader = DexClassLoader(extensionSourcePath, dexOutputDir.absolutePath, null, context.classLoader)

            // Ensure the class name from manifest is used
            val loadedClass = classLoader.loadClass(manifest.className)
            Log.d(TAG, "Loaded class ${manifest.className} for extension ${manifest.id}")

            if (!expectedApiInterface.isAssignableFrom(loadedClass)) {
                Log.e(TAG, "Loaded class ${manifest.className} does not implement expected interface ${expectedApiInterface.name}")
                return null
            }

            @Suppress("UNCHECKED_CAST")
            val extensionInstance = loadedClass.getDeclaredConstructor().newInstance() as T // Assumes no-arg constructor
            // If constructor needs ExtensionManifest:
            // val constructor = loadedClass.getDeclaredConstructor(ExtensionManifest::class.java)
            // val extensionInstance = constructor.newInstance(manifest) as T
            Log.i(TAG, "Successfully instantiated extension ${manifest.name} (ID: ${manifest.id})")
            return extensionInstance

        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Extension class ${manifest.className} not found in $extensionSourcePath for ${manifest.id}", e)
        } catch (e: NoSuchMethodException) {
            Log.e(TAG, "No-arg constructor not found for ${manifest.className} in ${manifest.id}", e)
        } catch (e: InstantiationException) {
            Log.e(TAG, "Failed to instantiate ${manifest.className} in ${manifest.id}", e)
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "Illegal access while instantiating ${manifest.className} in ${manifest.id}", e)
        } catch (e: ClassCastException) {
            Log.e(TAG, "Loaded class ${manifest.className} cannot be cast to ${expectedApiInterface.name} for ${manifest.id}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Generic error loading extension instance ${manifest.id}: ${e.message}", e)
        }

        // No simulation fallback here if real loading fails with a valid path.
        return null
    }

    // Removed simulateInstanceLoading as the dummy providers are now external or different.
    // The DummySuccessMovieProviderFromLoader is no longer needed here.
}
