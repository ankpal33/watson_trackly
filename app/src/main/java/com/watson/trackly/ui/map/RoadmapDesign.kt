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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Beenhere
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
    onLocationClick: (String) -> Unit = {},
    isSimplified: Boolean = false
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

    val startIconPainter = rememberVectorPainter(Icons.Default.KeyboardDoubleArrowUp)
    val endIconPainter = rememberVectorPainter(Icons.Default.Beenhere)

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
                        val r = (if (isSimplified) 60.dp else 80.dp).toPx().coerceAtMost(dy.coerceAtMost(dx / 2f))

                        val path = Path().apply {
                            moveTo(p1.x, p1.y)
                            val c1y = if (midY > p1.y) midY - r else midY + r
                            lineTo(p1.x, c1y)
                            val c1x = if (p2.x > p1.x) p1.x + r else p1.x - r
                            quadraticBezierTo(p1.x, midY, c1x, midY)
                            val c2x = if (p2.x > p1.x) p2.x - r else p2.x + r
                            lineTo(c2x, midY)
                            val c2y = if (p2.y > midY) midY + r else midY - r
                            quadraticBezierTo(p2.x, midY, p2.x, c2y)
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
                    drawLine(color = trackColor, start = startPoint, end = firstNode, strokeWidth = strokeWidth, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    drawCircle(color = trackColor, radius = 4.dp.toPx(), center = startPoint)

                    with(startIconPainter) {
                        translate(left = startPoint.x - 20.dp.toPx(), top = startPoint.y + 2.dp.toPx()) {
                            draw(size = Size(40.dp.toPx(), 40.dp.toPx()), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(trackColor))
                        }
                    }
                }
                
                val lastNode = positionsMap[sortedWalkOrders.last()]
                if (lastNode != null) {
                    val endY = lastNode.y - 60.dp.toPx()
                    val endPoint = Offset(lastNode.x, endY)
                    drawLine(color = trackColor, start = lastNode, end = endPoint, strokeWidth = strokeWidth, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))

                    // Small ending dot and icon
                    drawCircle(color = trackColor, radius = 4.dp.toPx(), center = endPoint)
                    
                    with(endIconPainter) {
                        translate(left = endPoint.x - 12.dp.toPx(), top = endPoint.y - 32.dp.toPx()) {
                            draw(size = Size(24.dp.toPx(), 24.dp.toPx()), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(trackColor))
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(vertical = 100.dp, horizontal = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isSimplified) 40.dp else 80.dp)
        ) {
            grouped.forEach { column ->
                RoadmapColumnView(
                    column = column,
                    onLocationClick = onLocationClick,
                    containerCoordinates = containerCoordinates,
                    onCircleGloballyPositioned = { walkOrder, offset -> nodePositions[walkOrder] = offset },
                    isSimplified = isSimplified
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
    onCircleGloballyPositioned: (Int, Offset) -> Unit,
    isSimplified: Boolean
) {
    val displayLocations = remember(column.locations) {
        if ((column.aisle + 1) % 2 == 1) column.locations.sortedByDescending { it.walkOrder }
        else column.locations.sortedBy { it.walkOrder }
    }

    Box(modifier = Modifier.width(if (isSimplified) 160.dp else 320.dp).fillMaxHeight()) {
        Column(
            modifier = Modifier.fillMaxHeight(),
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
                    onCircleGloballyPositioned = onCircleGloballyPositioned,
                    isSimplified = isSimplified
                )
                if (index < displayLocations.lastIndex) {
                    Spacer(Modifier.height(if (isSimplified) 32.dp else 60.dp))
                }
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
            .height(72.dp)
            .shadow(4.dp, RoundedCornerShape(36.dp))
            .clickable { onLocationClick(location.id) },
        shape = RoundedCornerShape(36.dp),
        color = location.color
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textContent = @Composable {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = if (isIconOnRight) 16.dp else 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = location.category.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )

                    val statusText = when (location.status) {
                        LocationStatus.COMPLETED -> "Resolved: ${location.issueLabel ?: ""}"
                        LocationStatus.PENDING -> "Pending: ${location.issueLabel ?: ""}"
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
                                fontSize = 8.sp,
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
    Box(modifier = Modifier.size(64.dp).background(Color.White, CircleShape).padding(14.dp), contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun StepCircle(
    location: AisleLocation,
    containerCoordinates: LayoutCoordinates?,
    onGloballyPositioned: (Offset) -> Unit,
    isSimplified: Boolean
) {
    val trackColor = Color(0xFF004B6E)
    val statusColor = when (location.status) {
        LocationStatus.COMPLETED -> Color(0xFF4CAF50)
        LocationStatus.CURRENT -> location.color
        LocationStatus.PENDING -> Color(0xFFFFAA46)
        LocationStatus.SKIPPED -> Color(0xFFDC301B)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                if (containerCoordinates != null && containerCoordinates.isAttached) {
                    val localCenter = Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
                    onGloballyPositioned(containerCoordinates.localPositionOf(coordinates, localCenter))
                }
            }
            .size(if (isSimplified) 64.dp else 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSimplified) {
            if (location.status != LocationStatus.DEFAULT) {
                Box(modifier = Modifier.fillMaxSize().border(3.dp, statusColor, CircleShape))
            }
            Surface(
                modifier = Modifier.size(50.dp).border(2.dp, location.color, CircleShape),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                    location.icon?.let { Icon(imageVector = it, contentDescription = null, tint = location.color, modifier = Modifier.fillMaxSize()) }
                }
            }
        } else {
            val isFilled = location.status != LocationStatus.DEFAULT
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, trackColor, CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().background(if (isFilled) statusColor else Color.Transparent, CircleShape))
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
    onCircleGloballyPositioned: (Int, Offset) -> Unit,
    isSimplified: Boolean
) {
    val isEven = (snakeIndex + aisleCol) % 2 == 0
    Row(modifier = Modifier.width(if (isSimplified) 100.dp else 320.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isSimplified) {
            StepCircle(location = location, containerCoordinates = containerCoordinates, onGloballyPositioned = { offset -> onCircleGloballyPositioned(location.walkOrder, offset) }, isSimplified = true)
        } else if (isEven) {
            StepCard(location = location, onLocationClick = onLocationClick, isIconOnRight = true, modifier = Modifier.requiredWidth(220.dp))
            Spacer(Modifier.width(12.dp))
            StepCircle(location = location, containerCoordinates = containerCoordinates, onGloballyPositioned = { offset -> onCircleGloballyPositioned(location.walkOrder, offset) }, isSimplified = false)
        } else {
            StepCircle(location = location, containerCoordinates = containerCoordinates, onGloballyPositioned = { offset -> onCircleGloballyPositioned(location.walkOrder, offset) }, isSimplified = false)
            Spacer(Modifier.width(12.dp))
            StepCard(location = location, onLocationClick = onLocationClick, isIconOnRight = false, modifier = Modifier.requiredWidth(220.dp))
        }
    }
}
