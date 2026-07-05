package com.watson.trackly.ui.map

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Stable
data class AisleLocation(
    val id: String,
    val barcode: String = "",
    val aisleCol: Int = 0,
    val walkOrder: Int = 0,
    
    // Metadata
    val category: String = "",
    val icon: ImageVector? = null,
    val color: Color = Color.Gray,

    // Dynamic State
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

data class RoadmapColumn(
    val aisle: Int,
    val locations: List<AisleLocation>
)
