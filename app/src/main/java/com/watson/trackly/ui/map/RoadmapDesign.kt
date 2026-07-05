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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun RoadmapDesign(
    locations: List<AisleLocation>,
    onLocationClick: (String) -> Unit = {}
) {
    val horizontalState = rememberScrollState()
    val verticalState = rememberScrollState()

    LaunchedEffect(Unit) {
        horizontalState.scrollTo(0)
        verticalState.scrollTo(verticalState.maxValue)
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

    val nodePositions = remember { mutableStateMapOf<Int, Offset>() }
    var containerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalState)
            .verticalScroll(verticalState)
            .onGloballyPositioned { containerCoordinates = it }
    ) {
        val positionsMap = nodePositions.toMap()
        val sortedWalkOrders = remember(positionsMap) { positionsMap.keys.sorted() }
        val walkOrderToAisle = remember(locations) { locations.associate { it.walkOrder to it.aisleCol } }

        Canvas(modifier = Modifier.matchParentSize()) {
            val trackColor = Color(0xFF004B6E)
            val strokeWidth = 3.dp.toPx()

            if (sortedWalkOrders.size > 1) {
                for (i in 0 until sortedWalkOrders.size - 1) {
                    val p1 = positionsMap[sortedWalkOrders[i]]
                    val p2 = positionsMap[sortedWalkOrders[i + 1]]
                    val aisle1 = walkOrderToAisle[sortedWalkOrders[i]]
                    val aisle2 = walkOrderToAisle[sortedWalkOrders[i + 1]]

                    if (p1 != null && p2 != null) {
                        val midY = (p1.y + p2.y) / 2f
                        val path = Path().apply {
                            moveTo(p1.x, p1.y)
                            lineTo(p1.x, midY)
                            lineTo(p2.x, midY)
                            lineTo(p2.x, p2.y)
                        }

                        drawPath(path = path, color = trackColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

                        if (aisle1 != null && aisle2 != null && aisle1 != aisle2) {
                            val arrowX = (p1.x + p2.x) / 2f
                            val angle = if (p2.x > p1.x) 0f else Math.PI.toFloat()
                            drawArrow(point = Offset(arrowX, midY), angleRad = angle, color = trackColor, strokeWidth = strokeWidth)
                        }
                    }
                }
            }
            
            if (sortedWalkOrders.isNotEmpty()) {
                val firstNode = positionsMap[sortedWalkOrders.first()]
                if (firstNode != null) {
                    val startY = firstNode.y + 60.dp.toPx()
                    drawLine(color = trackColor, start = Offset(firstNode.x, startY), end = firstNode, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawArrow(point = Offset(firstNode.x, (startY + firstNode.y) / 2), angleRad = -1.57f, color = trackColor, strokeWidth = strokeWidth)
                }
                
                val lastNode = positionsMap[sortedWalkOrders.last()]
                if (lastNode != null) {
                    val endY = lastNode.y - 60.dp.toPx()
                    drawLine(color = trackColor, start = lastNode, end = Offset(lastNode.x, endY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }
            }
        }

        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(80.dp)
        ) {
            grouped.forEach { column ->
                RoadmapColumnView(
                    column = column,
                    onLocationClick = onLocationClick,
                    containerCoordinates = containerCoordinates,
                    onCircleGloballyPositioned = { walkOrder, offset -> nodePositions[walkOrder] = offset }
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    point: Offset,
    angleRad: Float,
    color: Color,
    arrowLength: Float = 10.dp.toPx(),
    strokeWidth: Float = 3.dp.toPx()
) {
    val alpha = 0.52f
    val p1 = Offset(point.x - arrowLength * cos(angleRad + alpha), point.y - arrowLength * sin(angleRad + alpha))
    val p2 = Offset(point.x - arrowLength * cos(angleRad - alpha), point.y - arrowLength * sin(angleRad - alpha))
    val path = Path().apply {
        moveTo(point.x, point.y)
        lineTo(p1.x, p1.y)
        moveTo(point.x, point.y)
        lineTo(p2.x, p2.y)
    }
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

@Composable
private fun RoadmapColumnView(
    column: RoadmapColumn,
    onLocationClick: (String) -> Unit,
    containerCoordinates: LayoutCoordinates?,
    onCircleGloballyPositioned: (Int, Offset) -> Unit
) {
    val displayLocations = remember(column.locations) {
        if ((column.aisle + 1) % 2 == 1) column.locations.sortedByDescending { it.walkOrder }
        else column.locations.sortedBy { it.walkOrder }
    }

    Box(modifier = Modifier.width(340.dp).height(900.dp)) {
        Column(
            modifier = Modifier.padding(20.dp).align(Alignment.TopCenter),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            displayLocations.forEachIndexed { index, location ->
                RoadmapNode(
                    location = location,
                    snakeIndex = index,
                    aisleCol = column.aisle,
                    onLocationClick = onLocationClick,
                    containerCoordinates = containerCoordinates,
                    onCircleGloballyPositioned = onCircleGloballyPositioned
                )
                if (index < displayLocations.lastIndex) Spacer(Modifier.height(35.dp))
            }
        }
    }
}

@Composable
private fun StepCard(
    location: AisleLocation,
    onLocationClick: (String) -> Unit,
    isIconOnRight: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(84.dp)
            .shadow(6.dp, RoundedCornerShape(42.dp))
            .clickable { onLocationClick(location.id) },
        shape = RoundedCornerShape(42.dp),
        color = location.color
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isIconOnRight) {
                Column(modifier = Modifier.weight(1f).padding(start = 20.dp, end = 8.dp), verticalArrangement = Arrangement.Center) {
                    Text(text = location.category.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2)
                }
                location.icon?.let { IconCircle(it, location.color) }
            } else {
                location.icon?.let { IconCircle(it, location.color) }
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp, end = 20.dp), verticalArrangement = Arrangement.Center) {
                    Text(text = location.category.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun IconCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Box(modifier = Modifier.size(76.dp).background(Color.White, CircleShape).padding(18.dp), contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun StepCircle(
    location: AisleLocation,
    containerCoordinates: LayoutCoordinates?,
    onGloballyPositioned: (Offset) -> Unit
) {
    val trackColor = Color(0xFF004B6E)
    val statusColor = when (location.status) {
        LocationStatus.COMPLETED -> location.color
        LocationStatus.CURRENT -> location.color
        LocationStatus.PENDING -> Color(0xFFFFAA46)
        LocationStatus.SKIPPED -> Color(0xFFDC301B)
        else -> Color(0xFF94A3B8)
    }
    val isFilled = location.status != LocationStatus.DEFAULT

    Box(
        modifier = Modifier
            .size(28.dp)
            .onGloballyPositioned { coordinates ->
                if (containerCoordinates != null && containerCoordinates.isAttached) {
                    val localCenter = Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
                    onGloballyPositioned(containerCoordinates.localPositionOf(coordinates, localCenter))
                }
            }
            .background(Color.White, CircleShape).border(2.dp, trackColor, CircleShape).padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize().background(if (isFilled) statusColor else Color.Transparent, CircleShape)) {
            if (location.status == LocationStatus.COMPLETED) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun RoadmapNode(
    location: AisleLocation,
    snakeIndex: Int,
    aisleCol: Int,
    onLocationClick: (String) -> Unit,
    containerCoordinates: LayoutCoordinates?,
    onCircleGloballyPositioned: (Int, Offset) -> Unit
) {
    val isEven = (snakeIndex + aisleCol) % 2 == 0
    Row(modifier = Modifier.width(340.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isEven) {
            StepCard(location = location, onLocationClick = onLocationClick, isIconOnRight = true, modifier = Modifier.requiredWidth(220.dp))
            Spacer(Modifier.width(16.dp))
            StepCircle(location = location, containerCoordinates = containerCoordinates, onGloballyPositioned = { offset -> onCircleGloballyPositioned(location.walkOrder, offset) })
        } else {
            StepCircle(location = location, containerCoordinates = containerCoordinates, onGloballyPositioned = { offset -> onCircleGloballyPositioned(location.walkOrder, offset) })
            Spacer(Modifier.width(16.dp))
            StepCard(location = location, onLocationClick = onLocationClick, isIconOnRight = false, modifier = Modifier.requiredWidth(220.dp))
        }
    }
}
