package com.watson.trackly.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import kotlin.math.max
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoadmapDesign(
    locations: List<AisleLocation>,
    onLocationClick: (String) -> Unit = {}
) {
    val focusLocation = remember(locations) {
        locations.lastOrNull {
            it.status == LocationStatus.COMPLETED
        } ?: locations.first()
    }
    val nodePositions = remember {
        mutableStateMapOf<String, Offset>()
    }

    val horizontalState = rememberScrollState()
    val verticalState = rememberScrollState()

    LaunchedEffect(Unit) {
        horizontalState.scrollTo(0)
        verticalState.scrollTo(verticalState.maxValue)
    }

    val snakeOrder = remember(locations) {

        locations
            .groupBy { it.aisleCol }
            .toSortedMap()
            .flatMap { (aisle, items) ->

                val sorted = items.sortedBy { it.walkOrder }

                val isEven = aisle % 2 == 0

                if (isEven)
                    sorted.reversed()   // bottom → top
                else
                    sorted               // top → bottom
            }
    }
    val grouped = remember(locations) {
        locations
            .groupBy { it.aisleCol }
            .toSortedMap()
            .map { (aisle, items) ->
                RoadmapColumn(
                    aisle = aisle,
                    locations = items.sortedBy { it.walkOrder }
                )
            }
    }

    Box(
        Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalState)
            .verticalScroll(verticalState)
    ) {
        // Snake
        SnakePathOverlay(
            nodePositions = nodePositions,
            snakeOrder = snakeOrder,
            modifier = Modifier.matchParentSize()
        )

        // Columns
        Row(
            horizontalArrangement = Arrangement.spacedBy(80.dp)
        ) {
            grouped.forEach { column ->
                RoadmapColumnView(
                    column = column,
                    onLocationClick = onLocationClick,
                    onNodePositioned = {
                        nodePositions[it.id] = it.center
                    }
                )
            }
        }
    }
}

@Composable
private fun SnakePathOverlay(
    nodePositions: Map<String, Offset>,
    snakeOrder: List<AisleLocation>,
    modifier: Modifier
) {
    Canvas(modifier) {

        val points = snakeOrder.mapNotNull { nodePositions[it.id] }

        if (points.size < 2) return@Canvas

        val path = Path().apply {

            moveTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                val p = points[i]
                lineTo(p.x, p.y)
            }
        }

        drawPath(
            path = path,
            color = Color(0xFFD1D5DB),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f)
                )
            )
        )
    }
}

@Composable
private fun RoadmapColumnView(
    column: RoadmapColumn,
    onLocationClick: (String) -> Unit,
    onNodePositioned: (NodePosition) -> Unit
) {

    val isEvenAisle = column.aisle % 2 == 0

    val displayLocations =
        if ((column.aisle + 1) % 2 == 1) {
            column.locations.sortedByDescending { it.walkOrder }
        } else {
            column.locations.sortedBy { it.walkOrder }
        }

    Box(
        modifier = Modifier.width(340.dp).height(900.dp)
    ) {

        Column(
            modifier = Modifier.padding(20.dp).align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            displayLocations.forEachIndexed { index, location ->

                RoadmapNode(
                    location = location,
                    snakeIndex = index,
                    onLocationClick = onLocationClick,
                    onNodePositioned = onNodePositioned
                )

                Spacer(Modifier.height(35.dp))
            }
        }
    }
}
@Composable
private fun StepCard(
    location: AisleLocation,
    meta: AisleMeta,
    onLocationClick: (String) -> Unit,
    isIconOnRight: Boolean,
    modifier: Modifier = Modifier
) {
    val cardColor = meta.color

    Surface(
        modifier = modifier
            .height(84.dp)
            .shadow(6.dp, RoundedCornerShape(42.dp))
            .clickable { onLocationClick(location.id) },
        shape = RoundedCornerShape(42.dp),
        color = cardColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isIconOnRight) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp, end = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = meta.category.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                }
                IconCircle(icon = meta.icon, tint = cardColor)
            } else {
                IconCircle(icon = meta.icon, tint = cardColor)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = meta.category.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun IconCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .background(Color.White, CircleShape)
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun StepCircle(
    location: AisleLocation,
    meta: AisleMeta,
    onPositioned: (NodePosition) -> Unit
) {
    val statusColor = when (location.status) {
        LocationStatus.COMPLETED -> meta.color
        LocationStatus.CURRENT -> meta.color
        LocationStatus.PENDING -> Color(0xFFFFAA46)
        LocationStatus.SKIPPED -> Color(0xFFDC301B)
        else -> Color(0xFF94A3B8)
    }

    val isFilled = location.status != LocationStatus.DEFAULT

    Box(
        modifier = Modifier
            .size(28.dp)
            .onGloballyPositioned { coordinates ->

                val center = coordinates.boundsInRoot().center
                onPositioned(
                    NodePosition(
                        id = location.id,
                        aisle = location.aisleCol,
                        walkOrder = location.walkOrder,
                        center = center
                    )
                )
            }

            .background(Color.White, CircleShape)
            .border(2.dp, statusColor, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFilled) statusColor else Color.Transparent,
                    CircleShape
                )
        ) {
            if (location.status == LocationStatus.COMPLETED) {
                Icon(
                    Icons.Default.Check,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun RoadmapNode(
    location: AisleLocation,
    snakeIndex: Int,
    onLocationClick: (String) -> Unit,
    onNodePositioned: (NodePosition) -> Unit
) {
    val meta = nodeMetaMap[location.id] ?: return
    val isEven = snakeIndex % 2 == 0
    Row(
        modifier = Modifier.width(340.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (isEven) {

            StepCard(
                location = location,
                meta = meta,
                onLocationClick = onLocationClick,
                isIconOnRight = true,
                modifier = Modifier.requiredWidth(220.dp)
            )

            Spacer(Modifier.width(16.dp))

            StepCircle(
                location = location,
                meta = meta,
                onPositioned = onNodePositioned
            )

        } else {

            StepCircle(
                location = location,
                meta = meta,
                onPositioned = onNodePositioned
            )

            Spacer(Modifier.width(16.dp))

            StepCard(
                location = location,
                meta = meta,
                onLocationClick = onLocationClick,
                isIconOnRight = false,
                modifier = Modifier.requiredWidth(220.dp)
            )

        }
    }
}