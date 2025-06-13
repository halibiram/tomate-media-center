package com.halibiram.tomato.feature.extensions.engine

import android.content.Context
import com.halibiram.tomato.core.common.result.Result
import com.halibiram.tomato.domain.model.Extension
import com.halibiram.tomato.domain.repository.ExtensionRepository
import com.halibiram.tomato.feature.extensions.api.ExtensionManifest
import com.halibiram.tomato.feature.extensions.api.MovieProviderExtension
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

// Assume MainCoroutineExtension is in a shared test utility module
@ExperimentalCoroutinesApi
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : org.junit.jupiter.api.extension.BeforeEachCallback, org.junit.jupiter.api.extension.AfterEachCallback {
    override fun beforeEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.setMain(testDispatcher) // For main-dispatcher tasks in engine if any
        // Engine itself uses Dispatchers.IO, so this primarily helps if engine uses viewModelScope or Main for something.
    }
    override fun afterEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
@ExtendWith(MainCoroutineExtension::class)
class ExtensionEngineTest {

    private lateinit var mockContext: Context // Context for Engine, though might not be used directly if loader handles it
    private lateinit var mockExtensionLoader: ExtensionLoader
    private lateinit var mockExtensionRepository: ExtensionRepository
    private lateinit var extensionEngine: ExtensionEngine
    private lateinit var testDispatcher: TestDispatcher // For advancing time

    private val extensionsFlow = MutableStateFlow<List<Extension>>(emptyList())

    @BeforeEach
    fun setUp() {
        testDispatcher = StandardTestDispatcher() // Using StandardTestDispatcher for control
        // No need to set main dispatcher here if engine always uses Dispatchers.IO for its scope.
        // However, if engine's coroutineScope used Dispatchers.Main, this would be needed.
        // The engine's scope is Dispatchers.IO + SupervisorJob(), so this rule is for tests that might need Main.

        mockContext = mockk(relaxed = true)
        mockExtensionLoader = mockk()
        mockExtensionRepository = mockk()

        every { mockExtensionRepository.getExtensions() } returns extensionsFlow

        // Initialize engine AFTER mocks are set up, as init {} block in engine calls loadEnabledExtensions
        // extensionEngine = ExtensionEngine(mockContext, mockExtensionLoader, mockExtensionRepository)
    }

    @AfterEach
    fun tearDown() {
        // No explicit Dispatchers.resetMain() needed if test dispatcher not set on Main for engine's scope
    }

    private fun createDomainExtension(
        id: String, name: String, version: String = "1.0", isEnabled: Boolean = true,
        className: String = "com.example.$id.MainClass", sourceUrl: String = "/path/to/$id.apk",
        apiVersion: Int = com.halibiram.tomato.feature.extensions.api.CURRENT_HOST_EXTENSION_API_VERSION,
        author: String = "Test Author"
    ) = Extension(id, name, id, version, sourceUrl, "Test Desc", isEnabled, null, apiVersion, author, "Test Source", className)

    private fun createMockMovieProvider(manifest: ExtensionManifest): MovieProviderExtension = mockk {
        every { this@mockk.id } returns manifest.id
        every { this@mockk.name } returns manifest.name
        // Mock other manifest properties if needed by engine directly from instance
        coEvery { getPopularMovies(any()) } returns emptyList() // Default mock behavior
        coEvery { searchMovies(any(), any()) } returns emptyList()
    }

    @Test
    fun `loadEnabledExtensions loads enabled extensions and unloads disabled or removed ones`() = runTest(testDispatcher.scheduler) {
        extensionEngine = ExtensionEngine(mockContext, mockExtensionLoader, mockExtensionRepository) // Init engine here

        val ext1Enabled = createDomainExtension("ext1", "Extension 1", isEnabled = true)
        val mockProvider1 = createMockMovieProvider(ext1Enabled.toManifest())
        coEvery { mockExtensionLoader.loadExtensionInstance(ext1Enabled.toManifest(), ext1Enabled.sourceUrl, MovieProviderExtension::class.java) } returns mockProvider1

        // Initial: ext1 is enabled
        extensionsFlow.value = listOf(ext1Enabled)
        advanceUntilIdle() // Let the engine collect and process

        assertNotNull(extensionEngine.getLoadedExtensionInstance<MovieProviderExtension>("ext1"))
        assertEquals(1, extensionEngine.getAllPopularMovies(1).size) // Assuming it's a movie provider

        // Update: ext1 is disabled, ext2 is enabled
        val ext1Disabled = ext1Enabled.copy(isEnabled = false)
        val ext2Enabled = createDomainExtension("ext2", "Extension 2", isEnabled = true)
        val mockProvider2 = createMockMovieProvider(ext2Enabled.toManifest())
        coEvery { mockExtensionLoader.loadExtensionInstance(ext2Enabled.toManifest(), ext2Enabled.sourceUrl, MovieProviderExtension::class.java) } returns mockProvider2

        extensionsFlow.value = listOf(ext1Disabled, ext2Enabled)
        advanceUntilIdle()

        assertNull(extensionEngine.getLoadedExtensionInstance<MovieProviderExtension>("ext1"), "ext1 should be unloaded as it's disabled")
        assertNotNull(extensionEngine.getLoadedExtensionInstance<MovieProviderExtension>("ext2"), "ext2 should be loaded")
        val popularResults = extensionEngine.getAllPopularMovies(1)
        assertEquals(1, popularResults.size) // Only ext2 should provide results
        assertTrue(popularResults.containsKey(ext2Enabled.name))

        // Update: ext2 is removed (uninstalled)
        extensionsFlow.value = listOf(ext1Disabled) // Only ext1 (disabled) remains
        advanceUntilIdle()

        assertNull(extensionEngine.getLoadedExtensionInstance<MovieProviderExtension>("ext2"), "ext2 should be unloaded as it's removed")
        assertEquals(0, extensionEngine.getAllPopularMovies(1).size)
    }

    @Test
    fun `loadEnabledExtensions handles null instance from loader gracefully`() = runTest(testDispatcher.scheduler) {
        extensionEngine = ExtensionEngine(mockContext, mockExtensionLoader, mockExtensionRepository)

        val ext1 = createDomainExtension("ext1_fail_load", "Extension Fail Load", isEnabled = true)
        // Simulate loader returning null for this extension instance
        coEvery { mockExtensionLoader.loadExtensionInstance(ext1.toManifest(), ext1.sourceUrl, MovieProviderExtension::class.java) } returns null

        extensionsFlow.value = listOf(ext1)
        advanceUntilIdle()

        assertNull(extensionEngine.getLoadedExtensionInstance<MovieProviderExtension>("ext1_fail_load"))
        val popularResults = extensionEngine.getAllPopularMovies(1)
        assertTrue(popularResults.isEmpty())
    }


    @Test
    fun `getAllPopularMovies aggregates results from multiple active MovieProviderExtensions`() = runTest(testDispatcher.scheduler) {
        extensionEngine = ExtensionEngine(mockContext, mockExtensionLoader, mockExtensionRepository)

        val ext1 = createDomainExtension("ext1", "Provider One", isEnabled = true)
        val ext2 = createDomainExtension("ext2", "Provider Two", isEnabled = true)
        val ext3Disabled = createDomainExtension("ext3", "Provider Three", isEnabled = false) // Disabled

        val movies1 = listOf(MovieSourceItem("p1", "Movie 1A", null, "2023"))
        val movies2 = listOf(MovieSourceItem("p2", "Movie 2A", null, "2024"))

        val mockProvider1 = createMockMovieProvider(ext1.toManifest())
        coEvery { mockProvider1.getPopularMovies(1) } returns movies1
        val mockProvider2 = createMockMovieProvider(ext2.toManifest())
        coEvery { mockProvider2.getPopularMovies(1) } returns movies2

        // Simulate loader returning these instances
        coEvery { mockExtensionLoader.loadExtensionInstance(ext1.toManifest(), ext1.sourceUrl, MovieProviderExtension::class.java) } returns mockProvider1
        coEvery { mockExtensionLoader.loadExtensionInstance(ext2.toManifest(), ext2.sourceUrl, MovieProviderExtension::class.java) } returns mockProvider2
        coEvery { mockExtensionLoader.loadExtensionInstance(ext3Disabled.toManifest(), ext3Disabled.sourceUrl, MovieProviderExtension::class.java) } returns null // Or some mock that won't be called

        extensionsFlow.value = listOf(ext1, ext2, ext3Disabled)
        advanceUntilIdle() // Allow engine to load instances

        val results = extensionEngine.getAllPopularMovies(1)

        assertEquals(2, results.size) // Only two enabled and loaded providers
        assertTrue(results.containsKey(ext1.name))
        assertTrue(results.containsKey(ext2.name))
        assertEquals(movies1, (results[ext1.name] as Result.Success).data)
        assertEquals(movies2, (results[ext2.name] as Result.Success).data)
    }

    @Test
    fun `getAllPopularMovies handles errors from individual extensions`() = runTest(testDispatcher.scheduler) {
        extensionEngine = ExtensionEngine(mockContext, mockExtensionLoader, mockExtensionRepository)

        val ext1 = createDomainExtension("ext1", "Good Provider", isEnabled = true)
        val ext2Error = createDomainExtension("ext2_err", "Error Provider", isEnabled = true)

        val movies1 = listOf(MovieSourceItem("p1", "Good Movie", null, "2023"))
        val mockProvider1 = createMockMovieProvider(ext1.toManifest())
        coEvery { mockProvider1.getPopularMovies(1) } returns movies1

        val mockProviderError = createMockMovieProvider(ext2Error.toManifest())
        val exception = RuntimeException("Extension Error")
        coEvery { mockProviderError.getPopularMovies(1) } throws exception

        coEvery { mockExtensionLoader.loadExtensionInstance(ext1.toManifest(), ext1.sourceUrl, MovieProviderExtension::class.java) } returns mockProvider1
        coEvery { mockExtensionLoader.loadExtensionInstance(ext2Error.toManifest(), ext2Error.sourceUrl, MovieProviderExtension::class.java) } returns mockProviderError

        extensionsFlow.value = listOf(ext1, ext2Error)
        advanceUntilIdle()

        val results = extensionEngine.getAllPopularMovies(1)

        assertEquals(2, results.size)
        assertTrue(results[ext1.name] is Result.Success)
        assertEquals(movies1, (results[ext1.name] as Result.Success).data)
        assertTrue(results[ext2Error.name] is Result.Error)
        assertEquals(exception, (results[ext2Error.name] as Result.Error).exception.cause)
    }

    // Helper to convert Extension domain model to ExtensionManifest for mocking loader calls
    private fun Extension.toManifest(): ExtensionManifest = object : ExtensionManifest {
        override val id: String = this@toManifest.id
        override val name: String = this@toManifest.name
        override val version: String = this@toManifest.version
        override val author: String = this@toManifest.author ?: "N/A"
        override val description: String? = this@toManifest.description
        override val apiVersion: Int = this@toManifest.apiVersion
        override val className: String = this@toManifest.className
    }

    // Similar tests for searchAllMovies can be added here
}
