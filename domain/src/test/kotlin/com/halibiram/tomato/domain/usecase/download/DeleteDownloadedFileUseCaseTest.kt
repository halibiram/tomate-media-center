package com.halibiram.tomato.domain.usecase.download

import android.content.Context
import androidx.work.WorkManager
import com.halibiram.tomato.domain.repository.DownloadRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class DeleteDownloadedFileUseCaseTest {

    private lateinit var context: Context // Mocked, but WorkManager interaction is key
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var workManager: WorkManager
    private lateinit var deleteDownloadedFileUseCase: DeleteDownloadedFileUseCase

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        downloadRepository = mockk()
        workManager = mockk(relaxed = true)

        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager

        deleteDownloadedFileUseCase = DeleteDownloadedFileUseCase(context, downloadRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(WorkManager::class)
    }

    @Test
    fun `invoke cancels work and calls repository removeDownload`() = runTest {
        val downloadId = "task_to_delete"

        every { workManager.cancelUniqueWork(downloadId) } returns mockk() // Mock Operation
        coEvery { downloadRepository.removeDownload(downloadId) } returns true // Assume successful removal

        val result = deleteDownloadedFileUseCase.invoke(downloadId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { workManager.cancelUniqueWork(downloadId) }
        coVerify(exactly = 1) { downloadRepository.removeDownload(downloadId) }
    }

    @Test
    fun `invoke returns failure if repository removeDownload fails`() = runTest {
        val downloadId = "task_delete_fail"

        every { workManager.cancelUniqueWork(downloadId) } returns mockk()
        coEvery { downloadRepository.removeDownload(downloadId) } returns false // Simulate failure

        val result = deleteDownloadedFileUseCase.invoke(downloadId)

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { downloadRepository.removeDownload(downloadId) }
    }

    @Test
    fun `invoke returns failure if repository removeDownload throws exception`() = runTest {
        val downloadId = "task_delete_exception"
        val exception = RuntimeException("DB error")

        every { workManager.cancelUniqueWork(downloadId) } returns mockk()
        coEvery { downloadRepository.removeDownload(downloadId) } throws exception

        val result = deleteDownloadedFileUseCase.invoke(downloadId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}

// Helper extension for Result class if not globally available in tests
inline val <T> Result<T>.isFailure: Boolean
    get() = this is Result.failure<*>

inline fun <T> Result<T>.exceptionOrNull(): Throwable? =
    if (this is Result.failure<*>) (this as Result.failure<*>).exception else null
