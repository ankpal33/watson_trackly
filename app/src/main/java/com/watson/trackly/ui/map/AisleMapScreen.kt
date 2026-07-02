package com.watson.trackly.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aholdusa.cleansweep.ui.common.AppMain
import com.watson.trackly.R
import com.watson.trackly.ui.components.AppIconButton
import com.watson.trackly.ui.components.HeaderText
import com.watson.trackly.ui.components.PrimaryButton
import com.watson.trackly.ui.components.ScanCardState
import com.watson.trackly.ui.components.SecondaryButton
import com.watson.trackly.ui.components.VerticalLinearMap
import com.watson.trackly.ui.setting.LocationItem

@Composable
fun AisleMapScreen(
    vm: AisleMapViewModel = hiltViewModel(),
    appNavHost: NavHostController,
    onScanClick: () -> Unit,
    onLogout: () -> Unit
) {
    val mapState by vm.mapUiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // Header with title and logout button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trackly),
                        contentDescription = "logo",
                        tint = AppMain,
                        modifier = Modifier.size(40.dp)
                    )
                    HeaderText(text = stringResource(R.string.app_name))
                }
                AppIconButton(
                    iconRes = R.drawable.ic_logout,
                    contentDescription = "Logout",
                    onClick = onLogout
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Linear Map (takes 3/4 of screen) - Scrollable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .clip(MaterialTheme.shapes.medium)
                    .border(
                        width = 1.dp,
                        color = AppMain,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                LinearMapView(
                    locations = mapState.locations,
                    onLocationClick = { locationId ->
                        vm.onLocationClicked(locationId)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons Row (below map)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Top
            ) {
                PrimaryButton(
                    text = "Scan Location",
                    onClick = onScanClick,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(5.dp))

                SecondaryButton(
                    text = "End Walk",
                    onClick = { vm.showEndWalkDialog() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Skip Dialog - shown first if aisles were skipped
        if (mapState.showSkipDialog) {
            SkipDialog(
                skippedLocations = mapState.skippedLocations,
                skipReasons = vm.skipReasons,
                selectedReason = mapState.selectedSkipReason,
                onReasonSelected = { vm.onSkipReasonSelected(it) },
                onSkip = { vm.confirmSkip() },
                onCancel = { vm.cancelSkip() }
            )
        }
        
        // Survey Dialog
        if (mapState.showSurveyDialog) {
            val currentLocation = mapState.locations.find { it.id == mapState.currentLocationId }
            currentLocation?.let { location ->
                SurveyDialog(
                    locationName = location.name,
                    options = vm.surveyOptions,
                    selectedOption = mapState.selectedOption,
                    onOptionSelected = { vm.onSurveyOptionSelected(it) },
                    onSubmit = { vm.onSurveySubmit() },
                    onDismiss = { vm.dismissSurveyDialog() },
                    onResolveNow = { vm.onResolveNow() },
                    onResolveLater = { vm.onResolveLater() }
                )
            }
        }
        
        // End Walk Dialog
        if (mapState.showEndWalkDialog) {
            EndWalkDialog(
                locations = mapState.locations,
                onConfirm = {
                    vm.confirmEndWalk()
                    onLogout()
                },
                onDismiss = { vm.dismissEndWalkDialog() }
            )
        }
    }
}

@Composable
fun LinearMapView(
    locations: List<AisleLocation>,
    onLocationClick: (String) -> Unit
) {
    // Convert AisleLocation to LocationItem for VerticalLinearMap
    // Reverse the list so Aisle 10 appears at top and Aisle 1 at bottom
    val locationItems = remember(locations) {
        locations.reversed().map { aisleLocation ->
            LocationItem(
                name = aisleLocation.name,
                barcode = aisleLocation.barcode,
                status = aisleLocation.status
            )
        }
    }
    
    val currentCardState = remember { ScanCardState.INITIAL }
    val matchedLocationItem = locationItems.firstOrNull { it.status == LocationStatus.SCANNED }
    val bottomCardHeight = 0.dp
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        VerticalLinearMap(
            locations = locationItems,
            currentCardState = currentCardState,
            matchedLocation = matchedLocationItem,
            cardBottomPadding = with(LocalDensity.current) { bottomCardHeight },
            listState = listState
        )
    }
}

// Made with Bob
