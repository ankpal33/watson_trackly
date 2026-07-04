package com.watson.trackly.ui.map

import androidx.compose.runtime.Stable

/**
 * Side of an aisle shelf.
 * COMMON  = single shelf in the middle (no left/right split)
 * LEFT    = left-side shelf of an aisle
 * RIGHT   = right-side shelf of an aisle
 */
enum class ShelfSide { COMMON, LEFT, RIGHT }

@Stable
data class AisleLocation(
    val id: String,
    val name: String,
    val barcode: String = "",

    val aisleCol: Int = 0,
    val productIndex: Int = 0,      // 0-based within the aisle
    val walkOrder: Int = 0,

    val status: LocationStatus = LocationStatus.DEFAULT,
    val issueLabel: String? = null,
    val skipReason: String? = null
)

enum class LocationStatus {
    DEFAULT,
    COMPLETED,
    SKIPPED,
    PENDING,
    SCANNED,
    CURRENT
}

data class SkipReason(val id: String, val label: String)

@Stable
data class SurveyOption(val id: String, val label: String)

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
    val pendingLocationId: String? = null
)

data class RoadmapProductRow(
    val productIndex: Int,
    val leftLocation: AisleLocation?,
    val rightLocation: AisleLocation?
)

data class RoadmapColumn(
    val aisle: Int,
    val locations: List<AisleLocation>
)

// Made with Bob
