package com.halibiram.tomato.feature.downloads.worker

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.repository.DownloadRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation // Needed if client uses it, though not directly for download stream
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [30])
class DownloadWorkerTest {

    private lateinit var context: Context
    private lateinit var mockDownloadRepository: DownloadRepository
    private lateinit var mockHttpClient: HttpClient // Will use MockEngine

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockDownloadRepository = mockk(relaxed = true) // Relaxed for coJustRun on suspend fun

        // Setup coJustRun for suspend functions in the repository
        coJustRun { mockDownloadRepository.updateDownloadStatus(any(), any()) }
        coJustRun { mockDownloadRepository.updateDownloadState(any(), any(), any(), any(), any(), any(), any()) }
    }

    private fun buildTestWorker(
        inputData: androidx.work.Data,
        mockEngineConfig: MockEngine.Config.() -> Unit = {}
    ): DownloadWorker {
        val mockEngine = MockEngine {
            // Default handler if not specified by test
            addHandler { respond("Default Mock Response", HttpStatusCode.OK) }
            // Apply test-specific config
            mockEngineConfig()
        }
        mockHttpClient = HttpClient(mockEngine) {
            // Add plugins if your worker's client needs them, though for basic download, not much.
            // install(ContentNegotiation) { ... }
        }

        return TestListenableWorkerBuilder<DownloadWorker>(context)
            .setInputData(inputData)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker {
                    return DownloadWorker(
                        appContext,
                        workerParameters,
                        mockDownloadRepository,
                        mockHttpClient
                    )
                }
            })
            .build()
    }

    @Test
    fun `doWork successful download updates repository and returns success`() = runTest {
        val downloadId = "id1"
        val mediaUrl = "http://example.com/file.mp4"
        val mediaTitle = "Test Video"
        val testContent = "this is a test file content".toByteArray()
        val outputFileName = "$downloadId.${mediaUrl.substringAfterLast('.', "mp4")}"


        val worker = buildTestWorker(
            inputData = workDataOf(
                DownloadWorker.KEY_DOWNLOAD_ID to downloadId,
                DownloadWorker.KEY_MEDIA_URL to mediaUrl,
                DownloadWorker.KEY_MEDIA_TITLE to mediaTitle
            )
        ) {
            addHandler { request ->
                assertEquals(mediaUrl, request.url.toString())
                respond(
                    content = ByteReadChannel(testContent),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentLength, testContent.size.toString())
                )
            }
        }

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerifyOrder {
            mockDownloadRepository.updateDownloadStatus(downloadId, DownloadStatus.DOWNLOADING)
            // Initial state update by worker (progress 0, total size known)
            mockDownloadRepository.updateDownloadState(downloadId, 0, DownloadStatus.DOWNLOADING, 0L, testContent.size.toLong(), null, null)
            // Progress updates (at least one for 100%)
            mockDownloadRepository.updateDownloadState(downloadId, 100, DownloadStatus.DOWNLOADING, testContent.size.toLong(), testContent.size.toLong(), null, null)
            // Final completion update
            mockDownloadRepository.updateDownloadState(downloadId, 100, DownloadStatus.COMPLETED, testContent.size.toLong(), testContent.size.toLong(), any<String>(), any<Long>())
        }

        // Verify file was created (Robolectric allows file system interactions)
        val expectedFile = File(context.filesDir, "downloads/$outputFileName")
        assertTrue(expectedFile.exists())
        assertEquals(testContent.size.toLong(), expectedFile.length())
        expectedFile.delete() // Clean up
    }

    @Test
    fun `doWork network error updates repository and returns failure`() = runTest {
        val downloadId = "id_network_error"
        val mediaUrl = "http://example.com/error.mp4"
        val worker = buildTestWorker(
            inputData = workDataOf(DownloadWorker.KEY_DOWNLOAD_ID to downloadId, DownloadWorker.KEY_MEDIA_URL to mediaUrl, DownloadWorker.KEY_MEDIA_TITLE to "Error Test")
        ) {
            addHandler {
                respond("Error", HttpStatusCode.NotFound)
            }
        }

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        assertNotNull(result.outputData.getString("error")) // Check if error message is passed back
        coVerify { mockDownloadRepository.updateDownloadState(downloadId, 0, DownloadStatus.FAILED, 0L, null, null, null) }
    }

    @Test
    fun `doWork general exception updates repository and returns failure`() = runTest {
        val downloadId = "id_general_error"
        val mediaUrl = "http://example.com/exception.mp4"
         val worker = buildTestWorker(
            inputData = workDataOf(DownloadWorker.KEY_DOWNLOAD_ID to downloadId, DownloadWorker.KEY_MEDIA_URL to mediaUrl, DownloadWorker.KEY_MEDIA_TITLE to "Exception Test")
        ) {
            addHandler {
                throw IOException("Simulated connection error") // Simulate an exception during HTTP call
            }
        }

        val result = worker.doWork()
        assertTrue(result is ListenableWorker.Result.Failure)
        coVerify { mockDownloadRepository.updateDownloadState(downloadId, 0, DownloadStatus.FAILED, 0L, null, null, null) }
    }


    @Test
    fun `doWork cancellation updates repository and returns failure`() = runTest {
        val downloadId = "id_cancelled"
        val mediaUrl = "http://example.com/longfile.mp4"
        val testContent = ByteArray(10 * 1024 * 1024) { it.toByte() } // 10MB file

        val worker = buildTestWorker(
            inputData = workDataOf(DownloadWorker.KEY_DOWNLOAD_ID to downloadId, DownloadWorker.KEY_MEDIA_URL to mediaUrl, DownloadWorker.KEY_MEDIA_TITLE to "Cancellation Test")
        ) {
            addHandler {
                // Respond slowly to allow cancellation
                respond(content = ByteReadChannel(testContent), headers = headersOf(HttpHeaders.ContentLength, testContent.size.toString()))
            }
        }

        // Start the worker in a separate coroutine to be able to stop it
        val job = launch { worker.doWork() }
        delay(50) // Let it start and potentially make one progress update

        // Simulate WorkManager stopping the worker
        // worker.stop() // This is not how TestListenableWorkerBuilder worker is stopped externally
        // Instead, we can check if the worker respects its isStopped flag.
        // The current DownloadWorker's main download loop checks `isStopped`.
        // To test cancellation, we'd need to trigger `onStopped()` on the worker,
        // or the worker's `isStopped` property would need to be true.
        // TestListenableWorkerBuilder doesn't directly expose a way to externally stop like WorkManager does.
        // However, if the worker is cancelled via its Future, it should stop.

        val future = worker.future // Get the ListenableFuture
        // Attempt to cancel the future. This should set isStopped flag internally.
        future.cancel(true)

        // Wait for the worker to finish (it should detect cancellation)
        val result = future.get() // Or use CallbackToFutureAdapter to get result with timeout

        assertTrue(result is ListenableWorker.Result.Failure) // Or success depending on how cancellation is handled
        // Verify DB status updated to PAUSED or CANCELLED
        coVerify(atLeast = 1) { mockDownloadRepository.updateDownloadState(eq(downloadId), any(), or(eq(DownloadStatus.PAUSED.name), eq(DownloadStatus.CANCELLED.name)), any(), any(), null, null) }

        job.cancel() // Clean up test coroutine
    }

    // Test for missing input data
    @Test
    fun `doWork with missing downloadId returns failure`() = runTest {
         val worker = buildTestWorker(
            inputData = workDataOf(DownloadWorker.KEY_MEDIA_URL to "url", DownloadWorker.KEY_MEDIA_TITLE to "title")
        )
        val result = worker.doWork()
        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals("Download ID missing", result.outputData.getString("error"))
    }
}
