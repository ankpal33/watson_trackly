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
    val name: String,          // e.g. "A1-C2"
    val position: Float,       // legacy – kept for barcode scan ordering
    val barcode: String = "",
    val status: LocationStatus = LocationStatus.DEFAULT,
    val issueLabel: String? = null,
    val skipReason: String? = null,

    // Grid coordinates on the store map
    val aisleCol: Int = 0,     // which aisle column (0-based, left to right)
    val rowIndex: Int = 0,     // row within that column (0-based, bottom = 0)
    val side: ShelfSide = ShelfSide.COMMON,

    // Walk order index – determines skip detection sequence
    val walkOrder: Int = 0
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

data class RoadmapColumn(
    val index: Int,
    val rows: List<RoadmapRow>
)

data class RoadmapRow(
    val rowIndex: Int,
    val locations: List<AisleLocation>
)

// Made with Bob
