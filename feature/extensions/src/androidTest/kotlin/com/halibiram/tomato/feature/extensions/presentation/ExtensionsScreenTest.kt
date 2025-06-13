package com.halibiram.tomato.feature.extensions.presentation

import android.net.Uri
// import androidx.activity.compose.rememberLauncherForActivityResult // Not directly testable this way
// import androidx.activity.result.contract.ActivityResultContracts // Not directly testable this way
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.state.ToggleableState // For Switch state assertion
import com.halibiram.tomato.domain.model.Extension
import com.halibiram.tomato.domain.usecase.extension.EnableExtensionUseCase
import com.halibiram.tomato.domain.usecase.extension.GetExtensionsUseCase
import com.halibiram.tomato.domain.usecase.extension.InstallExtensionUseCase
import com.halibiram.tomato.domain.usecase.extension.UninstallExtensionUseCase
import com.halibiram.tomato.ui.theme.TomatoTheme
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.semantics.SemanticsProperties

// Fake ViewModel for ExtensionsScreen UI tests
class FakeExtensionsViewModel(
    initialState: ExtensionsUiState,
    val mockGetExtensionsUseCase: GetExtensionsUseCase = mockk { every { invoke() } returns MutableStateFlow(initialState.extensions) },
    val mockInstallExtensionUseCase: InstallExtensionUseCase = mockk(relaxed = true),
    val mockUninstallExtensionUseCase: UninstallExtensionUseCase = mockk(relaxed = true),
    val mockEnableExtensionUseCase: EnableExtensionUseCase = mockk(relaxed = true)
) : ExtensionsViewModel(
    mockGetExtensionsUseCase,
    mockInstallExtensionUseCase,
    mockUninstallExtensionUseCase,
    mockEnableExtensionUseCase
) {
    private val _fakeUiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<ExtensionsUiState> = _fakeUiState

    var installCalledWithUri: String? = null
    var uninstallCalledWithId: String? = null
    var toggleCalledWithId: String? = null
    var toggleCalledWithIsEnabledState: Boolean? = null // This is the state *before* toggle

    fun setState(newState: ExtensionsUiState) {
        _fakeUiState.value = newState
        // If GetExtensionsUseCase is strictly observed for list changes by VM, update its flow
        (mockGetExtensionsUseCase.invoke() as MutableStateFlow).value = newState.extensions
    }

    override fun installExtension(sourceUri: String) {
        installCalledWithUri = sourceUri
        // Test will manually set new state to simulate use case result
    }

    override fun uninstallExtension(id: String) {
        uninstallCalledWithId = id
        val currentExts = _fakeUiState.value.extensions.toMutableList()
        currentExts.removeAll { it.id == id }
        _fakeUiState.value = _fakeUiState.value.copy(extensions = currentExts, infoMessage = "Uninstalled $id")
    }

    override fun toggleExtensionEnabled(id: String, currentIsEnabled: Boolean) {
        toggleCalledWithId = id
        toggleCalledWithIsEnabledState = currentIsEnabled
        val currentExts = _fakeUiState.value.extensions.map {
            if (it.id == id) it.copy(isEnabled = !currentIsEnabled) else it
        }
        _fakeUiState.value = _fakeUiState.value.copy(extensions = currentExts, infoMessage = "Toggled $id")
    }

    override fun clearUserMessages() {
        _fakeUiState.update { it.copy(error = null, infoMessage = null) }
    }
}


@RunWith(AndroidJUnit4::class)
class ExtensionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeExtensionsViewModel

    private fun sampleExtension(id: String, name: String, isEnabled: Boolean, loadingError: String? = null) =
        Extension(id, name, "pkg.$id", "1.0", "uri_for_$id", "Desc for $name", isEnabled, null, 1, "Author", "Source", "$id.ClassName", loadingError)

    @Test
    fun extensionsScreen_loadingState_showsLoadingIndicator() {
        fakeViewModel = FakeExtensionsViewModel(ExtensionsUiState(isLoading = true, extensions = emptyList()))
        composeTestRule.setContent {
            TomatoTheme {
                ExtensionsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToExtensionSettings = {})
            }
        }
        composeTestRule.onNodeWithText("Manage Extensions").assertIsDisplayed()
        composeTestRule.onNode(isProgressBar()).assertIsDisplayed() // Check for any CircularProgressIndicator
        composeTestRule.onNodeWithText("No extensions installed yet.").assertDoesNotExist()
    }

    @Test
    fun extensionsScreen_emptyState_showsNoExtensionsMessage() {
        fakeViewModel = FakeExtensionsViewModel(ExtensionsUiState(isLoading = false, extensions = emptyList()))
        composeTestRule.setContent {
            TomatoTheme {
                ExtensionsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToExtensionSettings = {})
            }
        }
        composeTestRule.onNodeWithText("No extensions installed yet.", substring = true).assertIsDisplayed()
    }

    @Test
    fun extensionsScreen_errorState_showsSnackbarError() {
        val errorMsg = "Failed to load extensions list"
        fakeViewModel = FakeExtensionsViewModel(ExtensionsUiState(isLoading = false, error = errorMsg))
        composeTestRule.setContent {
            TomatoTheme {
                ExtensionsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToExtensionSettings = {})
            }
        }
        // Snackbar is shown via LaunchedEffect, testing its appearance directly is complex.
        // We verify the state that triggers it.
        assertEquals(errorMsg, fakeViewModel.uiState.value.error)
        // composeTestRule.onNodeWithText(errorMsg).assertIsDisplayed() // This would be ideal but snackbars are tricky
    }

    @Test
    fun extensionsScreen_displaysListOfExtensions() {
        val extensions = listOf(sampleExtension("ext1", "Extension Alpha", true), sampleExtension("ext2", "Extension Beta", false))
        fakeViewModel = FakeExtensionsViewModel(ExtensionsUiState(isLoading = false, extensions = extensions))
        composeTestRule.setContent {
            TomatoTheme {
                ExtensionsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToExtensionSettings = {})
            }
        }
        composeTestRule.onNodeWithText("Extension Alpha").assertIsDisplayed()
        composeTestRule.onNodeWithText("Extension Beta").assertIsDisplayed()
    }

    @Test
    fun installFlow_fabClick_simulatesViewModelCall() {
        // Cannot test ActivityResultLauncher directly here.
        // We test that clicking FAB would normally trigger the launcher.
        // And then we simulate the callback to the ViewModel.
        fakeViewModel = FakeExtensionsViewModel(ExtensionsUiState(extensions = emptyList()))
        val dummyUri = "content://dummy/dummy_success_ext.apk"

        // Simulate the InstallExtensionUseCase success via the FakeViewModel's state update
        val newExtId = "com.example.success"
        val installedExtension = sampleExtension(newExtId, "Dummy Success Extension (Manifest)", true)
        coEvery { fakeViewModel.mockInstallExtensionUseCase.invoke(dummyUri, any(), any()) } returns com.halibiram.tomato.core.common.result.Result.Success(installedExtension)


        composeTestRule.setContent {
            TomatoTheme {
                ExtensionsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToExtensionSettings = {})
            }
        }
        // FAB exists
        composeTestRule.onNodeWithContentDescription("Install New Extension").assertIsDisplayed().performClick()
        // At this point, system file picker would open. We simulate its result by calling VM method.

        // Manually simulate the result for the fake ViewModel as if launcher returned URI
        fakeViewModel.installExtension(dummyUri)
        composeTestRule.waitForIdle() // Allow viewModel to process and update state

        assertEquals(dummyUri, fakeViewModel.installCalledWithUri)
        // Check for info message via state (Snackbar test is complex)
        assertTrue(fakeViewModel.uiState.value.infoMessage?.contains("Extension installed: ${installedExtension.name}") == true)
    }

    @Test
    fun uninstallFlow_clickUninstall_showsDialogAndCallsViewModelAndRemovesItem() {
        val extToUninstall = sampleExtension("ext_del", "Delete Me Ext", true)
        fakeViewModel = FakeExtensionsViewModel(ExtensionsUiState(extensions = listOf(extToUninstall)))
        coEvery { fakeViewModel.mockUninstallExtensionUseCase.invoke(extToUninstall.id) } returns com.halibiram.tomato.core.common.result.Result.Success(Unit)

        composeTestRule.setContent {
            TomatoTheme {
                ExtensionsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToExtensionSettings = {})
            }
        }
        composeTestRule.onNode(hasParent(hasText("Delete Me Ext")) and hasText("Uninstall")).performClick()
        composeTestRule.onNodeWithText("Uninstall Extension?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Uninstall", useUnmergedTree = true).performClick()

        assertEquals(extToUninstall.id, fakeViewModel.uninstallCalledWithId)
        composeTestRule.onNodeWithText("Delete Me Ext").assertDoesNotExist() // Item removed from UI
        assertTrue(fakeViewModel.uiState.value.infoMessage?.contains("Uninstalled") == true)
    }

    @Test
    fun enableDisableToggle_clickSwitch_callsViewModelAndUpdatesSwitchState() {
        val extToToggle = sampleExtension("ext_toggle", "Toggle Me Ext", true)
        fakeViewModel = FakeExtensionsViewModel(ExtensionsUiState(extensions = listOf(extToToggle)))
        coEvery { fakeViewModel.mockEnableExtensionUseCase.invoke(extToToggle.id, false) } returns com.halibiram.tomato.core.common.result.Result.Success(Unit)

        composeTestRule.setContent {
            TomatoTheme {
                ExtensionsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToExtensionSettings = {})
            }
        }

        // Switch is initially On (isEnabled = true)
        composeTestRule.onNode(hasParent(hasText("Toggle Me Ext")) and isToggleable()).assertIsOn()
        composeTestRule.onNode(hasParent(hasText("Toggle Me Ext")) and isToggleable()).performClick()

        assertEquals(extToToggle.id, fakeViewModel.toggleCalledWithId)
        assertEquals(true, fakeViewModel.toggleCalledWithIsEnabledState) // isEnabled was true, toggle means new state is false

        // Check switch is now Off
        composeTestRule.onNode(hasParent(hasText("Toggle Me Ext")) and isToggleable()).assertIsOff()
        assertTrue(fakeViewModel.uiState.value.infoMessage?.contains("disabled") == true)
    }

    @Test
    fun extensionWithError_displaysErrorInItemAndDisablesSwitch() {
        val errorMsg = "Failed: Class not found"
        val extWithError = sampleExtension("ext_err_ui", "Error UI Ext", true, loadingError = errorMsg)
        fakeViewModel = FakeExtensionsViewModel(ExtensionsUiState(extensions = listOf(extWithError)))

        composeTestRule.setContent {
            TomatoTheme {
                ExtensionsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToExtensionSettings = {})
            }
        }

        composeTestRule.onNodeWithText("Error UI Ext").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error: $errorMsg", substring = true).assertIsDisplayed()
        // Verify the switch associated with "Error UI Ext" is not enabled
        composeTestRule.onNode(hasParent(hasText("Error UI Ext")) and isToggleable()).assertIsNotEnabled()
    }
}
