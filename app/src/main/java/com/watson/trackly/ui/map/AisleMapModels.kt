package com.watson.trackly.ui.map

import androidx.compose.runtime.Stable

@Stable
data class AisleLocation(
    val id: String,
    val name: String,
    val position: Float, // 0.0 to 1.0 representing position on the linear map
    val barcode: String = "", // Barcode associated with this location
    val status: LocationStatus = LocationStatus.PENDING
)

enum class LocationStatus {
    DEFAULT,    // Default state (gray dot)
    COMPLETED,  // Scanned and submitted (green dot)
    SKIPPED,    // Skipped (red/orange dot)
    PENDING,    // Not yet scanned (yellow dot)
    SCANNED,    // Scanned and submitted (green dot)
    CURRENT     // Currently selected (highlighted)
}

data class SkipReason(
    val id: String,
    val label: String
)

@Stable
data class SurveyOption(
    val id: String,
    val label: String
)

@Stable
data class MapUIState(
    val locations: List<AisleLocation> = emptyList(),
    val currentLocationId: String? = null,
    val showSurveyDialog: Boolean = false,
    val selectedOption: String? = null,
    val isWalkInProgress: Boolean = false,
    val showEndWalkDialog: Boolean = false,
    val showSkipDialog: Boolean = false,
    val skippedLocations: List<AisleLocation> = emptyList(),
    val selectedSkipReason: String? = null,
    val pendingLocationId: String? = null // The location that was scanned, waiting for skip confirmation
)

// Made with Bob
