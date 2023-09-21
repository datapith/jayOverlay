package io.datapit.jayOverlay

import kotlinx.serialization.Serializable

/**
 * Provides metadata about the Overlay.
 *
 * @property title A human-readable description of the purpose of the overlay.
 * @property version A version identifier for indicating changes to the Overlay document.
 */
@Serializable
data class Info(
    val title: String,
    val version: String
) {
    init {
        require(title.trim().isNotEmpty()) {"title in info is REQUIRED"}
    }
}
