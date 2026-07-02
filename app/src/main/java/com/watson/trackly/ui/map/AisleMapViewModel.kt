package com.watson.trackly.ui.map

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

    // Issue options
    val surveyOptions = listOf(
        SurveyOption("1", "Issue 1"),
        SurveyOption("2", "Issue 2"),
        SurveyOption("3", "Issue 3"),
        SurveyOption("4", "Issue 4")
    )
    
    // Skip reasons
    val skipReasons = listOf(
        SkipReason("1", "Reason 1"),
        SkipReason("2", "Reason 2")
    )
    
    // Barcode to Aisle mapping
    private val barcodeToAisle = mapOf(
        "3131" to "Aisle 1",
        "3333" to "Aisle 2",
        "2929" to "Aisle 3",
        "1515" to "Aisle 4",
        "1010" to "Aisle 5",
        "2626" to "Aisle 6",
        "2727" to "Aisle 7",
        "5151" to "Aisle 8",
        "4444" to "Aisle 9",
        "0707" to "Aisle 10"
    )

    init {
        initializeLocations()
    }

    private fun initializeLocations() {
        // Aisle 10 at top (position 0.0) to Aisle 1 at bottom (position 1.0)
        val locations = listOf(
            AisleLocation("aisle10", "Aisle 10", 0.0f, "0707", LocationStatus.DEFAULT),
            AisleLocation("aisle9", "Aisle 9", 0.111f, "4444", LocationStatus.DEFAULT),
            AisleLocation("aisle8", "Aisle 8", 0.222f, "5151", LocationStatus.DEFAULT),
            AisleLocation("aisle7", "Aisle 7", 0.333f, "2727", LocationStatus.DEFAULT),
            AisleLocation("aisle6", "Aisle 6", 0.444f, "2626", LocationStatus.DEFAULT),
            AisleLocation("aisle5", "Aisle 5", 0.555f, "1010", LocationStatus.DEFAULT),
            AisleLocation("aisle4", "Aisle 4", 0.666f, "1515", LocationStatus.DEFAULT),
            AisleLocation("aisle3", "Aisle 3", 0.777f, "2929", LocationStatus.DEFAULT),
            AisleLocation("aisle2", "Aisle 2", 0.888f, "3333", LocationStatus.DEFAULT),
            AisleLocation("aisle1", "Aisle 1", 1.0f, "3131", LocationStatus.DEFAULT)
        )
        _mapUiState.value = _mapUiState.value.copy(locations = locations)
    }

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
        // Reset all locations to DEFAULT
        val resetLocations = _mapUiState.value.locations.map {
            it.copy(status = LocationStatus.DEFAULT)
        }
        _mapUiState.value = _mapUiState.value.copy(locations = resetLocations)
    }

    fun onLocationScanned(locationId: String) {
        viewModelScope.launch {
            // Update the location status to CURRENT
            val updatedLocations = _mapUiState.value.locations.map { location ->
                if (location.id == locationId) {
                    location.copy(status = LocationStatus.CURRENT)
                } else {
                    location
                }
            }
            _mapUiState.value = _mapUiState.value.copy(
                locations = updatedLocations,
                currentLocationId = locationId,
                showSurveyDialog = true,
                selectedOption = null
            )
        }
    }

    fun onLocationClicked(locationId: String) {
        // Same as scanning - open survey dialog
        onLocationScanned(locationId)
    }

    fun onSurveyOptionSelected(optionId: String) {
        _mapUiState.value = _mapUiState.value.copy(selectedOption = optionId)
    }

    fun onResolveNow() {
        viewModelScope.launch {
            val currentId = _mapUiState.value.currentLocationId ?: return@launch
            
            // Mark the location as COMPLETED (green)
            val updatedLocations = _mapUiState.value.locations.map { location ->
                if (location.id == currentId) {
                    location.copy(status = LocationStatus.COMPLETED)
                } else {
                    location
                }
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
            
            // Mark the location as PENDING (yellow)
            val updatedLocations = _mapUiState.value.locations.map { location ->
                if (location.id == currentId) {
                    location.copy(status = LocationStatus.PENDING)
                } else {
                    location
                }
            }
            
            _mapUiState.value = _mapUiState.value.copy(
                locations = updatedLocations,
                showSurveyDialog = false,
                currentLocationId = null,
                selectedOption = null
            )
        }
    }

    fun onSurveySubmit() {
        // Deprecated - use onResolveNow or onResolveLater instead
        onResolveNow()
    }
    
    fun onBarcodeScanned(barcode: String) {
        // Check if barcode matches any aisle
        val aisleName = barcodeToAisle[barcode]
        if (aisleName != null) {
            // Find the location by name
            val location = _mapUiState.value.locations.find { it.name == aisleName }
            location?.let { scannedLocation ->
                // Check if any aisles were skipped
                val skippedAisles = findSkippedAisles(scannedLocation.id)
                
                if (skippedAisles.isNotEmpty()) {
                    // Show skip dialog
                    _mapUiState.value = _mapUiState.value.copy(
                        showSkipDialog = true,
                        skippedLocations = skippedAisles,
                        pendingLocationId = scannedLocation.id,
                        selectedSkipReason = null
                    )
                } else {
                    // No skipped aisles, proceed normally
                    onLocationScanned(scannedLocation.id)
                }
            }
        }
    }
    
    private fun findSkippedAisles(scannedLocationId: String): List<AisleLocation> {
        val locations = _mapUiState.value.locations
        val scannedLocation = locations.find { it.id == scannedLocationId } ?: return emptyList()
        
        // Extract aisle number from location name (e.g., "Aisle 1" -> 1)
        val scannedAisleNum = scannedLocation.name.replace("Aisle ", "").toIntOrNull() ?: return emptyList()
        
        // Find the highest aisle number that has been completed or pending
        val lastProcessedAisleNum = locations
            .filter { it.status == LocationStatus.COMPLETED || it.status == LocationStatus.PENDING }
            .mapNotNull { it.name.replace("Aisle ", "").toIntOrNull() }
            .maxOrNull()
        
        // If this is the first scan or scanning in order, no skips
        if (lastProcessedAisleNum == null || scannedAisleNum <= lastProcessedAisleNum + 1) {
            return emptyList()
        }
        
        // Return all aisles between last processed and current that are DEFAULT
        return locations.filter { location ->
            val aisleNum = location.name.replace("Aisle ", "").toIntOrNull() ?: return@filter false
            aisleNum > lastProcessedAisleNum && aisleNum < scannedAisleNum && location.status == LocationStatus.DEFAULT
        }.sortedBy { it.name.replace("Aisle ", "").toIntOrNull() ?: 0 }
    }
    
    fun onSkipReasonSelected(reasonId: String) {
        _mapUiState.value = _mapUiState.value.copy(selectedSkipReason = reasonId)
    }
    
    fun confirmSkip() {
        viewModelScope.launch {
            val skippedLocations = _mapUiState.value.skippedLocations
            val pendingLocationId = _mapUiState.value.pendingLocationId ?: return@launch
            
            // Mark skipped locations as SKIPPED
            val updatedLocations = _mapUiState.value.locations.map { location ->
                if (skippedLocations.any { it.id == location.id }) {
                    location.copy(status = LocationStatus.SKIPPED)
                } else {
                    location
                }
            }
            
            _mapUiState.value = _mapUiState.value.copy(
                locations = updatedLocations,
                showSkipDialog = false,
                skippedLocations = emptyList(),
                selectedSkipReason = null
            )
            
            // Now show the survey dialog for the scanned location
            onLocationScanned(pendingLocationId)
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
        // Revert current location back to pending if dialog is dismissed without submit
        val currentId = _mapUiState.value.currentLocationId
        val updatedLocations = _mapUiState.value.locations.map { location ->
            if (location.id == currentId && location.status == LocationStatus.CURRENT) {
                location.copy(status = LocationStatus.PENDING)
            } else {
                location
            }
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
