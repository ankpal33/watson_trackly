package com.watson.trackly.ui.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Blender
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.ChildFriendly
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Soap
import androidx.compose.material.icons.outlined.SoupKitchen
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    // Organized grouped layout - easier to manage like JSON
    private val storeLayoutGrouped = mapOf(
        // Aisle 1 (Bottom to Top walk order)
        0 to listOf(
            AisleLocation("A01P1", "1101", walkOrder = 1, category = "Bakery & Eggs", icon = Icons.Outlined.BakeryDining, color = Color(0xFFFB8C00)),
            AisleLocation("A01P2", "1121", walkOrder = 2, category = "Tea, Coffee", icon = Icons.Outlined.Coffee, color = Color(0xFF795548)),
            AisleLocation("A01P3", "1122", walkOrder = 3, category = "Beverages", icon = Icons.Outlined.LocalBar, color = Color(0xFF795548)),
            AisleLocation("A01P4", "1131", walkOrder = 4, category = "Biscuits & Snacks", icon = Icons.Outlined.Cookie, color = Color(0xFFFF7043)),
            AisleLocation("A01P5", "1141", walkOrder = 5, category = "Chocolates", icon = Icons.Outlined.Cake, color = Color(0xFF6D4C41))
        ),

        // Aisle 2 (Top to Bottom walk order)
        1 to listOf(
            AisleLocation("A02P6", "2241", walkOrder = 6, category = "Rice & Staples", icon = Icons.Outlined.LocalDining, color = Color(0xFF8D6E63)),
            AisleLocation("A02P5", "2231", walkOrder = 7, category = "Oils & Ghee", icon = Icons.Outlined.LocalFireDepartment, color = Color(0xFFFFB300)),
            AisleLocation("A02P4", "2232", walkOrder = 8, category = "Spices & Masalas", icon = Icons.Outlined.Blender, color = Color(0xFFE53935)),
            AisleLocation("A02P3", "2221", walkOrder = 9, category = "Instant Foods", icon = Icons.Outlined.SoupKitchen, color = Color(0xFF00ACC1)),
            AisleLocation("A02P2", "2211", walkOrder = 10, category = "Dairy & Frozen", icon = Icons.Outlined.Icecream, color = Color(0xFF1E88E5)),
            AisleLocation("A02P1", "2212", walkOrder = 11, category = "Fruits & Vegetables", icon = Icons.Outlined.Spa, color = Color(0xFF43A047))
        ),

        // Aisle 3 (Bottom to Top walk order)
        2 to listOf(
            AisleLocation("A03P1", "2311", walkOrder = 12, category = "Baby Care", icon = Icons.Outlined.ChildFriendly, color = Color(0xFF8D6E63)),
            AisleLocation("A03P2", "2312", walkOrder = 13, category = "Personal Care", icon = Icons.Outlined.Soap, color = Color(0xFFFFB300)),
            AisleLocation("A03P3", "2313", walkOrder = 14, category = "Health & Hygiene", icon = Icons.Outlined.Medication, color = Color(0xFFE53935)),
            AisleLocation("A03P4", "2314", walkOrder = 15, category = "Home Care", icon = Icons.Outlined.Home, color = Color(0xFF00ACC1)),
            AisleLocation("A03P5", "2315", walkOrder = 16, category = "Plastic & Storage", icon = Icons.Outlined.Kitchen, color = Color(0xFF1E88E5)),
            AisleLocation("A03P6", "2316", walkOrder = 17, category = "Cleaning Tools", icon = Icons.Outlined.CleaningServices, color = Color(0xFF43A047))
        )
    )

    // Derived flat list for scanning and status updates
    private val flattenedLayout = storeLayoutGrouped.flatMap { (aisle, locations) ->
        locations.map { it.copy(aisleCol = aisle) }
    }

    private val barcodeToLocationId = flattenedLayout.associate { it.barcode to it.id }

    init {
        _mapUiState.value = _mapUiState.value.copy(locations = flattenedLayout)
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

    fun setViewMode(mode: RoadmapViewMode) {
        _mapUiState.value = _mapUiState.value.copy(viewMode = mode)
    }

    private fun findSkippedNodes(scannedWalkOrder: Int): List<AisleLocation> {
        val locations = _mapUiState.value.locations
        val lastProcessedOrder = locations
            .filter { it.status == LocationStatus.COMPLETED || it.status == LocationStatus.PENDING }
            .maxOfOrNull { it.walkOrder }
        if (lastProcessedOrder == null || scannedWalkOrder <= lastProcessedOrder + 1) return emptyList()
        return locations.filter { loc ->
            loc.walkOrder in (lastProcessedOrder + 1)..<scannedWalkOrder &&
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
