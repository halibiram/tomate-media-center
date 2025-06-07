package com.halibiram.tomato.feature.player.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
// import com.halibiram.tomato.core.player.PlayerManager // Example

// PlayerViewModel
// @HiltViewModel // Example
class PlayerViewModel /*@Inject constructor(
    private val playerManager: PlayerManager, // From core player module
    private val savedStateHandle: SavedStateHandle
)*/ : ViewModel() {

    // val mediaUrl: String? = savedStateHandle.get("mediaUrl")

    init {
        // mediaUrl?.let { playerManager.playUrl(it) }
    }

    override fun onCleared() {
        super.onCleared()
        // playerManager.releasePlayer()
    }
    // ViewModel logic
}
