package com.halibiram.tomato.domain.usecase.download

import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.repository.DownloadRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class GetDownloadsUseCaseTest {

    private lateinit var downloadRepository: DownloadRepository
    private lateinit var getDownloadsUseCase: GetDownloadsUseCase

    @BeforeEach
    fun setUp() {
        downloadRepository = mockk()
        getDownloadsUseCase = GetDownloadsUseCase(downloadRepository)
    }

    @Test
    fun `invoke calls repository getDownloads and returns its flow`() = runTest {
        val expectedDownloads = listOf(mockk<Download>())
        every { downloadRepository.getDownloads() } returns flowOf(expectedDownloads)

        val resultFlow = getDownloadsUseCase.invoke()
        val actualDownloads = resultFlow.first()

        coVerify(exactly = 1) { downloadRepository.getDownloads() }
        assertEquals(expectedDownloads, actualDownloads)
    }

    @Test
    fun `getDownloadById calls repository getDownloadFlow and returns its flow`() = runTest {
        val downloadId = "testId"
        val expectedDownload = mockk<Download>()
        every { downloadRepository.getDownloadFlow(downloadId) } returns flowOf(expectedDownload)

        val resultFlow = getDownloadsUseCase.getDownloadById(downloadId)
        val actualDownload = resultFlow.first()

        coVerify(exactly = 1) { downloadRepository.getDownloadFlow(downloadId) }
        assertEquals(expectedDownload, actualDownload)
    }

    @Test
    fun `getActiveDownloads calls repository getActiveDownloadsFlow and returns its flow`() = runTest {
        val expectedActiveDownloads = listOf(mockk<Download>())
        every { downloadRepository.getActiveDownloadsFlow() } returns flowOf(expectedActiveDownloads)

        val resultFlow = getDownloadsUseCase.getActiveDownloads()
        val actualDownloads = resultFlow.first()

        coVerify(exactly = 1) { downloadRepository.getActiveDownloadsFlow() }
        assertEquals(expectedActiveDownloads, actualDownloads)
    }
}
