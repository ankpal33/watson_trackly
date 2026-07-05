package com.watson.trackly.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aholdusa.cleansweep.ui.common.AppMain
import com.watson.trackly.R
import com.watson.trackly.ui.components.AppIconButton
import com.watson.trackly.ui.components.HeaderText
import com.watson.trackly.ui.components.PrimaryButton
import com.watson.trackly.ui.components.SecondaryButton
import com.watson.trackly.ui.components.TabButton

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
                .systemBarsPadding()
                .padding(10.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            // Header with logo and logout button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_logo),
                    contentDescription = "Trackly Logo",
                    modifier = Modifier.height(32.dp),
                    contentScale = ContentScale.Fit
                )
                
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = AppMain,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // View Mode Toggle Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    text = "Category List",
                    isSelected = mapState.viewMode == RoadmapViewMode.LIST,
                    onClick = { vm.setViewMode(RoadmapViewMode.LIST) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = if (mapState.viewMode == RoadmapViewMode.MAP) "Scan View" else "Map View",
                    isSelected = (mapState.viewMode == RoadmapViewMode.MAP || mapState.viewMode == RoadmapViewMode.SCAN),
                    onClick = {
                        if (mapState.viewMode == RoadmapViewMode.MAP) {
                            vm.setViewMode(RoadmapViewMode.SCAN)
                        } else {
                            vm.setViewMode(RoadmapViewMode.MAP)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content Area (takes most of the screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .border(
                        width = 1.dp,
                        color = AppMain,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                when (mapState.viewMode) {
                    RoadmapViewMode.LIST -> {
                        CategoryListView(locations = mapState.locations)
                    }
                    RoadmapViewMode.SCAN -> {
                        RoadmapDesign(
                            locations = mapState.locations,
                            onLocationClick = { locationId ->
                                vm.onLocationClicked(locationId)
                            },
                            isSimplified = false
                        )
                    }
                    RoadmapViewMode.MAP -> {
                        RoadmapDesign(
                            locations = mapState.locations,
                            onLocationClick = { locationId ->
                                vm.onLocationClicked(locationId)
                            },
                            isSimplified = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons Row (below content)
            Row(
                modifier = Modifier.fillMaxWidth(),
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

        // Dialogs
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

        if (mapState.showSurveyDialog) {
            val currentLocation = mapState.locations.find { it.id == mapState.currentLocationId }
            currentLocation?.let { location ->
                SurveyDialog(
                    locationName = location.category,
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
fun CategoryListView(locations: List<AisleLocation>) {
    val grouped = remember(locations) {
        locations.groupBy { it.aisleCol }.toSortedMap()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(16.dp)
    ) {
        grouped.forEach { (aisle, items) ->
            item {
                Text(
                    text = "Aisle ${aisle + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppMain,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(items) { location ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = location.color.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            location.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    tint = location.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = location.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (location.status == LocationStatus.COMPLETED) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
