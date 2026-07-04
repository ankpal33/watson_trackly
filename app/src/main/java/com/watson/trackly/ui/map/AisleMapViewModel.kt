package com.watson.trackly.ui.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Blender
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Aisle metadata ────────────────────────────────────────────────────────────

data class AisleMeta(
    val aisleNumber: Int,       // 1-based aisle number shown in cards
    val category: String,       // full aisle category name shown as heading
    val icon: ImageVector,
    val color: Color
)

/**
 * Metadata keyed by node id, e.g. "A1C1", "A1L3", "A2R2".
 * aisleNumber follows the 10-aisle spec:
 *   1 Fruits & Vegetables  2 Dairy & Frozen    3 Bakery & Eggs
 *   4 Rice & Staples       5 Oils & Ghee       6 Spices & Masalas
 *   7 Salt, Sugar & Sweeteners  8 Tea, Coffee & Beverages
 *   9 Biscuits & Snacks   10 Chocolates & Confectionery
 */
val nodeMetaMap: Map<String, AisleMeta> = mapOf(
    // Aisle 1 – left column, walk bottom→top
    "A1C1" to AisleMeta(3,  "Bakery & Eggs",                 Icons.Outlined.BakeryDining,        Color(0xFFFB8C00)),
    "A1L2" to AisleMeta(8,  "Tea, Coffee & Beverages",       Icons.Outlined.Coffee,              Color(0xFF795548)),
    "A1R2" to AisleMeta(8,  "Tea, Coffee & Beverages",       Icons.Outlined.Coffee,              Color(0xFF795548)),
    "A1C3" to AisleMeta(9,  "Biscuits & Snacks",             Icons.Outlined.Cookie,              Color(0xFFFF7043)),
    "A1C4" to AisleMeta(10, "Chocolates & Confectionery",    Icons.Outlined.Cake,                Color(0xFF6D4C41)),

    // Aisle 2 – right column, walk top→bottom
    "A2C4" to AisleMeta(4,  "Rice & Staples",                Icons.Outlined.LocalDining,         Color(0xFF8D6E63)),
    "A2L3" to AisleMeta(5,  "Oils & Ghee",                   Icons.Outlined.LocalFireDepartment, Color(0xFFFFB300)),
    "A2R3" to AisleMeta(6,  "Spices & Masalas",              Icons.Outlined.Blender,             Color(0xFFE53935)),
    "A2C2" to AisleMeta(7,  "Salt, Sugar & Sweeteners",      Icons.Outlined.WaterDrop,           Color(0xFF00ACC1)),
    "A2L1" to AisleMeta(2,  "Dairy & Frozen",                Icons.Outlined.Icecream,            Color(0xFF1E88E5)),
    "A2R1" to AisleMeta(1,  "Fruits & Vegetables",           Icons.Outlined.Spa,                 Color(0xFF43A047)),
)

@HiltViewModel
class AisleMapViewModel @Inject constructor() : ViewModel() {

    private val _mapUiState = MutableStateFlow(MapUIState())
    val mapUiState: StateFlow<MapUIState> = _mapUiState.asStateFlow()

    val surveyOptions = listOf(
        SurveyOption("1", "Issue 1"),
        SurveyOption("2", "Issue 2"),
        SurveyOption("3", "Issue 3"),
        SurveyOption("4", "Issue 4")
    )

    val skipReasons = listOf(
        SkipReason("1", "Reason 1"),
        SkipReason("2", "Reason 2")
    )

    /**
     * Barcode → location id.  IDs match nodeMetaMap keys exactly.
     */
    private val barcodeToLocationId = mapOf(
        "1101" to "A1C1",
        "1121" to "A1L2",
        "1122" to "A1R2",
        "1131" to "A1C3",
        "1141" to "A1C4",
        "2241" to "A2C4",
        "2231" to "A2L3",
        "2232" to "A2R3",
        "2221" to "A2C2",
        "2211" to "A2L1",
        "2212" to "A2R1"
    )

    init {
        initializeLocations()
    }

    /**
     * Store layout (matches nodeMetaMap):
     *
     *   Aisle 1 (col 0, walk bottom→top)    Aisle 2 (col 1, walk top→bottom)
     *   row 3:  A1C4 (C)           ───────  A2C4 (C)
     *   row 2:  A1C3 (C)                    A2L3 (L) + A2R3 (R)
     *   row 1:  A1L2 (L) + A1R2 (R)         A2C2 (C)
     *   row 0:  A1C1 (C)                    A2L1 (L) + A2R1 (R)
     *
     * Walk order:
     *   1  A1C1   2  A1L2   3  A1R2   4  A1C3   5  A1C4
     *   6  A2C4   7  A2L3   8  A2R3   9  A2C2  10  A2L1  11  A2R1
     */
    private fun initializeLocations() {
        val locations = listOf(
            // ── Aisle 1, bottom → top ─────────────────────────────
            AisleLocation(id="A1C1", name="A1·C1", position=0.00f, barcode="1101",
                aisleCol=0, rowIndex=0, side=ShelfSide.COMMON, walkOrder=1),
            AisleLocation(id="A1L2", name="A1·L2", position=0.10f, barcode="1121",
                aisleCol=0, rowIndex=1, side=ShelfSide.LEFT,   walkOrder=2),
            AisleLocation(id="A1R2", name="A1·R2", position=0.15f, barcode="1122",
                aisleCol=0, rowIndex=1, side=ShelfSide.RIGHT,  walkOrder=3),
            AisleLocation(id="A1C3", name="A1·C3", position=0.20f, barcode="1131",
                aisleCol=0, rowIndex=2, side=ShelfSide.COMMON, walkOrder=4),
            AisleLocation(id="A1C4", name="A1·C4", position=0.30f, barcode="1141",
                aisleCol=0, rowIndex=3, side=ShelfSide.COMMON, walkOrder=5),
            // ── Aisle 2, top → bottom ─────────────────────────────
            AisleLocation(id="A2C4", name="A2·C4", position=0.40f, barcode="2241",
                aisleCol=1, rowIndex=3, side=ShelfSide.COMMON, walkOrder=6),
            AisleLocation(id="A2L3", name="A2·L3", position=0.50f, barcode="2231",
                aisleCol=1, rowIndex=2, side=ShelfSide.LEFT,   walkOrder=7),
            AisleLocation(id="A2R3", name="A2·R3", position=0.55f, barcode="2232",
                aisleCol=1, rowIndex=2, side=ShelfSide.RIGHT,  walkOrder=8),
            AisleLocation(id="A2C2", name="A2·C2", position=0.65f, barcode="2221",
                aisleCol=1, rowIndex=1, side=ShelfSide.COMMON, walkOrder=9),
            AisleLocation(id="A2L1", name="A2·L1", position=0.75f, barcode="2211",
                aisleCol=1, rowIndex=0, side=ShelfSide.LEFT,   walkOrder=10),
            AisleLocation(id="A2R1", name="A2·R1", position=0.80f, barcode="2212",
                aisleCol=1, rowIndex=0, side=ShelfSide.RIGHT,  walkOrder=11)
        )
        _mapUiState.value = _mapUiState.value.copy(locations = locations)
    }

    // ── Walk control ──────────────────────────────────────────────────────────

    fun startWalk() {
        _mapUiState.value = _mapUiState.value.copy(isWalkInProgress = true)
    }

    fun showEndWalkDialog() {
        _mapUiState.value = _mapUiState.value.copy(showEndWalkDialog = true)
    }

    fun dismissEndWalkDialog() {
        _mapUiState.value = _mapUiState.value.copy(showEndWalkDialog = false)
    }

    fun confirmEndWalk() {
        _mapUiState.value = _mapUiState.value.copy(
            isWalkInProgress = false,
            currentLocationId = null,
            showSurveyDialog = false,
            showEndWalkDialog = false
        )
        val resetLocations = _mapUiState.value.locations.map {
            it.copy(status = LocationStatus.DEFAULT, issueLabel = null, skipReason = null)
        }
        _mapUiState.value = _mapUiState.value.copy(locations = resetLocations)
    }

    fun onLocationScanned(locationId: String) {
        viewModelScope.launch {
            val updatedLocations = _mapUiState.value.locations.map { location ->
                if (location.id == locationId) location.copy(status = LocationStatus.CURRENT)
                else location
            }
            _mapUiState.value = _mapUiState.value.copy(
                locations = updatedLocations,
                currentLocationId = locationId,
                showSurveyDialog = true,
                selectedOption = null
            )
        }
    }

    fun onLocationClicked(locationId: String) = onLocationScanned(locationId)

    fun onSurveyOptionSelected(optionId: String) {
        _mapUiState.value = _mapUiState.value.copy(selectedOption = optionId)
    }

    fun onResolveNow() {
        viewModelScope.launch {
            val currentId = _mapUiState.value.currentLocationId ?: return@launch
            val issueLabel = surveyOptions.find { it.id == _mapUiState.value.selectedOption }?.label
            val updatedLocations = _mapUiState.value.locations.map { loc ->
                if (loc.id == currentId)
                    loc.copy(status = LocationStatus.COMPLETED, issueLabel = issueLabel, skipReason = null)
                else loc
            }
            _mapUiState.value = _mapUiState.value.copy(
                locations = updatedLocations,
                showSurveyDialog = false,
                currentLocationId = null,
                selectedOption = null
            )
        }
    }

    fun onResolveLater() {
        viewModelScope.launch {
            val currentId = _mapUiState.value.currentLocationId ?: return@launch
            val issueLabel = surveyOptions.find { it.id == _mapUiState.value.selectedOption }?.label
            val updatedLocations = _mapUiState.value.locations.map { loc ->
                if (loc.id == currentId)
                    loc.copy(status = LocationStatus.PENDING, issueLabel = issueLabel, skipReason = null)
                else loc
            }
            _mapUiState.value = _mapUiState.value.copy(
                locations = updatedLocations,
                showSurveyDialog = false,
                currentLocationId = null,
                selectedOption = null
            )
        }
    }

    fun onSurveySubmit() = onResolveNow()

    fun onBarcodeScanned(barcode: String) {
        val locationId = barcodeToLocationId[barcode] ?: return
        val location = _mapUiState.value.locations.find { it.id == locationId } ?: return
        val skippedAisles = findSkippedNodes(location.walkOrder)
        if (skippedAisles.isNotEmpty()) {
            _mapUiState.value = _mapUiState.value.copy(
                showSkipDialog = true,
                skippedLocations = skippedAisles,
                pendingLocationId = location.id,
                selectedSkipReason = null
            )
        } else {
            onLocationScanned(location.id)
        }
    }

    private fun findSkippedNodes(scannedWalkOrder: Int): List<AisleLocation> {
        val locations = _mapUiState.value.locations
        val lastProcessedOrder = locations
            .filter { it.status == LocationStatus.COMPLETED || it.status == LocationStatus.PENDING }
            .maxOfOrNull { it.walkOrder }
        if (lastProcessedOrder == null || scannedWalkOrder <= lastProcessedOrder + 1) return emptyList()
        return locations.filter { loc ->
            loc.walkOrder > lastProcessedOrder &&
            loc.walkOrder < scannedWalkOrder &&
            loc.status == LocationStatus.DEFAULT
        }.sortedBy { it.walkOrder }
    }

    fun onSkipReasonSelected(reasonId: String) {
        _mapUiState.value = _mapUiState.value.copy(selectedSkipReason = reasonId)
    }

    fun confirmSkip() {
        viewModelScope.launch {
            val skippedLocations = _mapUiState.value.skippedLocations
            val pendingId = _mapUiState.value.pendingLocationId ?: return@launch
            val reasonLabel = skipReasons.find { it.id == _mapUiState.value.selectedSkipReason }?.label
            val updatedLocations = _mapUiState.value.locations.map { loc ->
                if (skippedLocations.any { it.id == loc.id })
                    loc.copy(status = LocationStatus.SKIPPED, skipReason = reasonLabel, issueLabel = null)
                else loc
            }
            _mapUiState.value = _mapUiState.value.copy(
                locations = updatedLocations,
                showSkipDialog = false,
                skippedLocations = emptyList(),
                selectedSkipReason = null
            )
            onLocationScanned(pendingId)
        }
    }

    fun cancelSkip() {
        _mapUiState.value = _mapUiState.value.copy(
            showSkipDialog = false,
            skippedLocations = emptyList(),
            pendingLocationId = null,
            selectedSkipReason = null
        )
    }

    fun dismissSurveyDialog() {
        val currentId = _mapUiState.value.currentLocationId
        val updatedLocations = _mapUiState.value.locations.map { loc ->
            if (loc.id == currentId && loc.status == LocationStatus.CURRENT)
                loc.copy(status = LocationStatus.PENDING)
            else loc
        }
        _mapUiState.value = _mapUiState.value.copy(
            locations = updatedLocations,
            showSurveyDialog = false,
            currentLocationId = null,
            selectedOption = null
        )
    }
}

// Made with Bob
