package com.halibiram.tomato.domain.usecase.extension

import android.content.Context // For accessing cache/files dir
import android.net.Uri
import com.halibiram.tomato.core.common.result.Result
import com.halibiram.tomato.core.common.result.TomatoException
import com.halibiram.tomato.domain.model.Extension
import com.halibiram.tomato.domain.repository.ExtensionRepository
import com.halibiram.tomato.feature.extensions.engine.ExtensionLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class InstallExtensionUseCase @Inject constructor(
    @ApplicationContext private val context: Context, // Inject context for file operations
    private val extensionRepository: ExtensionRepository,
    private val extensionLoader: ExtensionLoader
) {
    private val TAG = "InstallExtensionUseCase"

    // Helper to copy content URI to a permanent app-specific directory for extensions
    private fun copyUriToAppStorage(contentUriString: String, targetFileName: String): File? {
        return try {
            val uri = Uri.parse(contentUriString)
            val extensionsDir = File(context.filesDir, "installed_extensions")
            if (!extensionsDir.exists()) {
                extensionsDir.mkdirs()
            }
            // Use a unique name, or one derived from manifest if possible, to avoid collisions
            // For now, using targetFileName which should be unique (e.g., based on extension ID)
            val targetFile = File(extensionsDir, targetFileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null
            android.util.Log.d(TAG, "Copied extension to internal storage: ${targetFile.absolutePath}")
            targetFile
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to copy URI to app storage: $contentUriString", e)
            null
        }
    }


    suspend operator fun invoke(
        sourceContentUri: String // This is the content:// URI from the file picker
    ): Result<Extension> {
        if (sourceContentUri.isBlank()) {
            return Result.Error(TomatoException("Source URI cannot be empty."))
        }

        // The ExtensionLoader's loadManifest will handle copying the content URI to a temporary cache file
        // for parsing with PackageManager. We get the manifest first.
        val manifest = extensionLoader.loadManifest(sourceContentUri)
            ?: return Result.Error(TomatoException("Failed to load or parse manifest from $sourceContentUri. The file might be invalid, not an APK, or not a recognized extension type."))

        // Check if extension with this ID is already installed
        val existingExtension = extensionRepository.getExtension(manifest.id)
        if (existingExtension != null) {
            return Result.Error(TomatoException("Extension with ID '${manifest.id}' (${existingExtension.name} v${existingExtension.version}) is already installed."))
        }

        if (manifest.apiVersion > com.halibiram.tomato.feature.extensions.api.CURRENT_HOST_EXTENSION_API_VERSION) {
            return Result.Error(TomatoException(
                "Extension '${manifest.name}' targets API v${manifest.apiVersion}, " +
                "which is newer than the host's API v${com.halibiram.tomato.feature.extensions.api.CURRENT_HOST_EXTENSION_API_VERSION}."
            ))
        }

        // If manifest parsing was successful, copy the original content URI to permanent app storage
        // Use a unique file name, e.g., based on extension ID + version.
        val permanentFileName = "${manifest.id}_v${manifest.version}.apk" // Example naming
        val storedApkFile = copyUriToAppStorage(sourceContentUri, permanentFileName)
        if (storedApkFile == null || !storedApkFile.exists()) {
            return Result.Error(TomatoException("Failed to copy extension to app storage for ${manifest.name}."))
        }
        val storedApkPath = storedApkFile.absolutePath
        android.util.Log.d(TAG, "Extension ${manifest.name} stored at: $storedApkPath")


        val newExtensionDomainObject = Extension(
            id = manifest.id,
            name = manifest.name,
            version = manifest.version,
            author = manifest.author,
            description = manifest.description,
            apiVersion = manifest.apiVersion,
            sourceUrl = storedApkPath,
            source = sourceDescription ?: "From file: ${sourceContentUri.substringAfterLast('/')}",
            packageName = manifest.id,
            iconUrl = null,
            className = manifest.className, // Populate className from manifest
            isEnabled = true
        )

        return try {
            // Pass the path to the internally stored APK to the repository
            extensionRepository.installExtension(newExtensionDomainObject, storedApkPath)
            Result.Success(newExtensionDomainObject)
        } catch (e: Exception) {
            // If saving to DB fails, attempt to delete the copied APK file
            storedApkFile.delete()
            android.util.Log.e(TAG, "Failed to save extension '${manifest.name}' to repository. Deleted copied APK. Error: ${e.message}", e)
            Result.Error(TomatoException("Failed to save extension '${manifest.name}' to repository: ${e.message}", e))
        }
    }
}
