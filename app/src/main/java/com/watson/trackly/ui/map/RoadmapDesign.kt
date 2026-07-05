package com.watson.trackly.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
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
                        val dy = abs(p1.y - midY)
                        val dx = abs(p1.x - p2.x)
                        val r = 60.dp.toPx().coerceAtMost(dy.coerceAtMost(dx / 2f))

                        val path = Path().apply {
                            moveTo(p1.x, p1.y)
                            
                            // To first curve
                            val c1y = if (midY > p1.y) midY - r else midY + r
                            lineTo(p1.x, c1y)
                            
                            // First curve
                            val c1x = if (p2.x > p1.x) p1.x + r else p1.x - r
                            quadraticBezierTo(p1.x, midY, c1x, midY)
                            
                            // To second curve
                            val c2x = if (p2.x > p1.x) p2.x - r else p2.x + r
                            lineTo(c2x, midY)
                            
                            // Second curve
                            val c2y = if (p2.y > midY) midY + r else midY - r
                            quadraticBezierTo(p2.x, midY, p2.x, c2y)
                            
                            // To end
                            lineTo(p2.x, p2.y)
                        }

                        drawPath(
                            path = path, 
                            color = trackColor, 
                            style = Stroke(
                                width = strokeWidth, 
                                cap = StrokeCap.Round, 
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )

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
                    val startPoint = Offset(firstNode.x, startY)
                    drawLine(
                        color = trackColor, 
                        start = startPoint, 
                        end = firstNode, 
                        strokeWidth = strokeWidth, 
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawCircle(color = trackColor, radius = 4.dp.toPx(), center = startPoint)
                }
                
                val lastNode = positionsMap[sortedWalkOrders.last()]
                if (lastNode != null) {
                    val endY = lastNode.y - 60.dp.toPx()
                    val endPoint = Offset(lastNode.x, endY)
                    drawLine(
                        color = trackColor, 
                        start = lastNode, 
                        end = endPoint, 
                        strokeWidth = strokeWidth, 
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Small ending dot
                    drawCircle(color = trackColor, radius = 4.dp.toPx(), center = endPoint)
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

    Box(modifier = Modifier.width(340.dp).height(800.dp)) {
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
            val textContent = @Composable {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = if (isIconOnRight) 20.dp else 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = location.category.uppercase(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )

                    val statusText = when (location.status) {
                        LocationStatus.COMPLETED -> "Resolved: ${location.issueLabel}"
                        LocationStatus.PENDING -> "Pending: ${location.issueLabel}"
                        LocationStatus.SKIPPED -> "Skipped: ${location.skipReason ?: ""}"
                        else -> null
                    }

                    AnimatedVisibility(
                        visible = statusText != null,
                        enter = fadeIn() + slideInVertically(animationSpec = tween(400)) { it / 2 },
                        exit = fadeOut()
                    ) {
                        if (statusText != null) {
                            Text(
                                text = statusText,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (isIconOnRight) {
                textContent()
                location.icon?.let { IconCircle(it, location.color) }
            } else {
                location.icon?.let { IconCircle(it, location.color) }
                textContent()
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
        LocationStatus.COMPLETED -> Color(0xFF4CAF50)
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
        Box(modifier = Modifier.fillMaxSize().background(if (isFilled) statusColor else Color.Transparent, CircleShape))
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
