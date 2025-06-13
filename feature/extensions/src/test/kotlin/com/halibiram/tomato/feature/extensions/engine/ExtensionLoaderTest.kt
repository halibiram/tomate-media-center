package com.halibiram.tomato.feature.extensions.engine

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.halibiram.tomato.feature.extensions.api.ExtensionManifest
import com.halibiram.tomato.feature.extensions.api.MovieProviderExtension
import dalvik.system.DexClassLoader
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [30]) // Using Robolectric
class ExtensionLoaderTest {

    private lateinit var context: Context
    private lateinit var mockPackageManager: PackageManager
    private lateinit var extensionLoader: ExtensionLoader

    // Dummy class that could be in a test APK/JAR for DexClassLoader test
    class TestExtensionImpl : MovieProviderExtension {
        override val id: String = "test.impl.id"
        override val name: String = "Test Impl Name"
        override val version: String = "1.0"
        override val author: String = "Test Author"
        override val description: String? = "Test Description"
        override val apiVersion: Int = 1
        override val className: String = TestExtensionImpl::class.java.name
        override suspend fun getPopularMovies(page: Int) = emptyList<com.halibiram.tomato.feature.extensions.api.MovieSourceItem>()
        override suspend fun searchMovies(query: String, page: Int) = emptyList<com.halibiram.tomato.feature.extensions.api.MovieSourceItem>()
    }


    @Before
    fun setUp() {
        context = spyk(ApplicationProvider.getApplicationContext()) // Spy to allow partial mocking if needed
        mockPackageManager = mockk(relaxed = true)
        every { context.packageManager } returns mockPackageManager

        extensionLoader = ExtensionLoader(context)

        // Mock content resolver for getFileFromContentUri
        val mockContentResolver = mockk<android.content.ContentResolver>()
        every { context.contentResolver } returns mockContentResolver
        val mockInputStream = mockk<InputStream>(relaxed = true) // Mock an InputStream
        every { mockContentResolver.openInputStream(any()) } returns mockInputStream

        // Mock FileOutputStream and copyTo to simulate successful file copy
        // This is a bit deep but needed to make getFileFromContentUri work in test
        mockkConstructor(FileOutputStream::class)
        every { any<FileOutputStream>().use<Unit>(any()) } answers {
            // Simulate the block being called, which means copyTo would be called
            // The actual file writing isn't important for this test's focus on manifest parsing
            // but ensuring the File object is returned.
            firstArg<FileOutputStream>().write("testdata".toByteArray()) // Write some dummy data
        }
        every { mockInputStream.copyTo(any()) } returns 1L // Simulate bytes copied
    }

    @After
    fun tearDown() {
        unmockkAll() // Clear all mocks
        // Clean up any created cache files (though Robolectric usually handles this for cacheDir)
        File(context.cacheDir, "temp_ext_test.apk").delete()
    }

    private fun createMockPackageInfo(packageName: String, className: String, appName: String, version: String, apiVersion: String, author: String, desc: String?): PackageInfo {
        val packageInfo = PackageInfo()
        packageInfo.packageName = packageName
        packageInfo.versionName = version
        packageInfo.applicationInfo = ApplicationInfo()
        packageInfo.applicationInfo.packageName = packageName
        packageInfo.applicationInfo.metaData = Bundle().apply {
            putString(ExtensionLoader.META_CLASS_NAME, className)
            putString(ExtensionLoader.META_NAME, appName)
            // putString(ExtensionLoader.META_VERSION, version) // Version from packageInfo.versionName is used if META_VERSION is not set
            putString(ExtensionLoader.META_API_VERSION, apiVersion)
            putString(ExtensionLoader.META_AUTHOR, author)
            desc?.let { putString(ExtensionLoader.META_DESCRIPTION, it) }
        }
        return packageInfo
    }

    @Test
    fun `loadManifest successfully parses valid APK metadata`() {
        val apkFileName = "test_extension.apk"
        val contentUri = "content://com.example.provider/$apkFileName"
        val tempFile = File(context.cacheDir, "ext_apk_12345_$apkFileName") // Simulate file name used by getFileFromContentUri

        // Ensure getFileFromContentUri returns this specific file for the test
        every { context.contentResolver.openInputStream(Uri.parse(contentUri)) } answers {
            tempFile.writeBytes("dummy apk data".toByteArray()) // Create a dummy file for getPackageArchiveInfo
            tempFile.inputStream()
        }


        val expectedPackageName = "com.example.testext"
        val expectedClassName = "com.example.testext.TestExtensionMain"
        val expectedAppName = "Test Extension Name"
        val expectedVersion = "1.0.1"
        val expectedApiVersion = "1"
        val expectedAuthor = "Test Author"
        val expectedDesc = "A test extension."

        val mockPI = createMockPackageInfo(expectedPackageName, expectedClassName, expectedAppName, expectedVersion, expectedApiVersion, expectedAuthor, expectedDesc)

        // Mock getPackageArchiveInfo
        val flags = PackageManager.GET_META_DATA or PackageManager.GET_SIGNATURES
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            every { mockPackageManager.getPackageArchiveInfo(tempFile.absolutePath, PackageManager.PackageInfoFlags.of(flags.toLong())) } returns mockPI
        } else {
            @Suppress("DEPRECATION")
            every { mockPackageManager.getPackageArchiveInfo(tempFile.absolutePath, flags) } returns mockPI
        }


        val manifest = extensionLoader.loadManifest(contentUri)

        assertNotNull("Manifest should not be null for valid APK", manifest)
        assertEquals(expectedPackageName, manifest!!.id)
        assertEquals(expectedClassName, manifest.className)
        assertEquals(expectedAppName, manifest.name)
        assertEquals(expectedVersion, manifest.version)
        assertEquals(expectedApiVersion.toInt(), manifest.apiVersion)
        assertEquals(expectedAuthor, manifest.author)
        assertEquals(expectedDesc, manifest.description)

        tempFile.delete() // Clean up created dummy file
    }

    @Test
    fun `loadManifest returns null if getPackageArchiveInfo returns null`() {
        val contentUri = "content://com.example.provider/invalid.apk"
        val tempFile = File(context.cacheDir, "ext_apk_invalid.apk")
        every { context.contentResolver.openInputStream(Uri.parse(contentUri)) } answers {
            tempFile.writeBytes("dummy apk data".toByteArray())
            tempFile.inputStream()
        }

        val flags = PackageManager.GET_META_DATA or PackageManager.GET_SIGNATURES
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            every { mockPackageManager.getPackageArchiveInfo(tempFile.absolutePath, PackageManager.PackageInfoFlags.of(flags.toLong())) } returns null
        } else {
            @Suppress("DEPRECATION")
            every { mockPackageManager.getPackageArchiveInfo(tempFile.absolutePath, flags) } returns null
        }

        val manifest = extensionLoader.loadManifest(contentUri)
        assertNull("Manifest should be null if PackageInfo is null", manifest)
        tempFile.delete()
    }

    @Test
    fun `loadManifest returns null if metadata is missing required fields`() {
        val contentUri = "content://com.example.provider/missing_meta.apk"
        val tempFile = File(context.cacheDir, "ext_apk_missing_meta.apk")
         every { context.contentResolver.openInputStream(Uri.parse(contentUri)) } answers {
            tempFile.writeBytes("dummy apk data".toByteArray())
            tempFile.inputStream()
        }

        val packageInfo = PackageInfo().apply {
            packageName = "com.example.missing"
            applicationInfo = ApplicationInfo().apply {
                packageName = "com.example.missing"
                metaData = Bundle() // Empty metadata
            }
        }
        val flags = PackageManager.GET_META_DATA or PackageManager.GET_SIGNATURES
         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            every { mockPackageManager.getPackageArchiveInfo(tempFile.absolutePath, PackageManager.PackageInfoFlags.of(flags.toLong())) } returns packageInfo
        } else {
            @Suppress("DEPRECATION")
            every { mockPackageManager.getPackageArchiveInfo(tempFile.absolutePath, flags) } returns packageInfo
        }

        val manifest = extensionLoader.loadManifest(contentUri)
        assertNull("Manifest should be null if required metadata (like class name or API version) is missing", manifest)
        tempFile.delete()
    }

    // DexClassLoader tests are complex without a real DEX file.
    // This test will mock DexClassLoader interactions.
    @Test
    fun `loadExtensionInstance attempts to load class using DexClassLoader`() {
        val manifest = mockk<ExtensionManifest> {
            every { id } returns "com.example.dex"
            every { className } returns "com.example.dex.DexEntry"
        }
        val apkPath = "/path/to/fake.apk" // Path to the APK file

        // Mock DexClassLoader
        mockkConstructor(DexClassLoader::class)
        val mockLoadedClass = mockk<Class<*>>()
        val mockInstance = mockk<MovieProviderExtension>()

        every { anyConstructed<DexClassLoader>().loadClass(manifest.className) } returns mockLoadedClass
        every { mockLoadedClass.asSubclass(MovieProviderExtension::class.java) } returns MovieProviderExtension::class.java.cast(mockLoadedClass)
        every { mockLoadedClass.getDeclaredConstructor().newInstance() } returns mockInstance
        every { MovieProviderExtension::class.java.isAssignableFrom(mockLoadedClass) } returns true


        val instance = extensionLoader.loadExtensionInstance(manifest, apkPath, MovieProviderExtension::class.java)

        assertNotNull(instance)
        assertEquals(mockInstance, instance)
        verify { anyConstructed<DexClassLoader>().loadClass(manifest.className) }
    }

    @Test
    fun `loadExtensionInstance returns null on ClassNotFoundException`() {
        val manifest = mockk<ExtensionManifest> {
            every { id } returns "com.example.notfound"
            every { className } returns "com.example.notfound.MissingClass"
        }
        val apkPath = "/path/to/another_fake.apk"

        mockkConstructor(DexClassLoader::class)
        every { anyConstructed<DexClassLoader>().loadClass(manifest.className) } throws ClassNotFoundException()

        val instance = extensionLoader.loadExtensionInstance(manifest, apkPath, MovieProviderExtension::class.java)
        assertNull(instance)
    }
}
