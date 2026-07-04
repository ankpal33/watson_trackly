package com.watson.trackly.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoadmapDesign(
    locations: List<AisleLocation>,
    onLocationClick: (String) -> Unit = {}
) {
    val sortedLocations = remember(locations) {
        locations.sortedBy { it.walkOrder }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(top = 48.dp, bottom = 24.dp),
        reverseLayout = true
    ) {
        // Start Dot (at the bottom because of reverseLayout)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    drawCircle(Color(0xFFD1D5DB), 4.dp.toPx())
                }
            }
        }

        itemsIndexed(sortedLocations) { index, location ->
            RoadmapStep(
                location = location,
                index = index,
                isLast = index == sortedLocations.size - 1,
                onLocationClick = onLocationClick
            )
        }

        // End Dot (at the top because of reverseLayout)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    drawCircle(Color(0xFFD1D5DB), 4.dp.toPx())
                }
            }
        }
    }
}

@Composable
private fun RoadmapStep(
    location: AisleLocation,
    index: Int,
    isLast: Boolean,
    onLocationClick: (String) -> Unit
) {
    val meta = nodeMetaMap[location.id] ?: return
    val isRightSide = index % 2 == 0 // Alternates sides

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        // ── Path Segment ──────────────────────────────────────────────────────
        PathSegment(index = index, isLast = isLast)

        // ── Content Row ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isRightSide) Arrangement.End else Arrangement.Start
        ) {
            if (isRightSide) {
                // Card [Left] | Circle [Right] | Label [Far Right]
                StepCard(
                    location = location,
                    meta = meta,
                    onLocationClick = onLocationClick,
                    isIconOnRight = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                StepCircle(location = location, meta = meta)
                Spacer(modifier = Modifier.width(12.dp))
                StepLabel(stepNumber = index + 1, isLeft = false)
            } else {
                // Label [Far Left] | Circle [Left] | Card [Right]
                StepLabel(stepNumber = index + 1, isLeft = true)
                Spacer(modifier = Modifier.width(12.dp))
                StepCircle(location = location, meta = meta)
                Spacer(modifier = Modifier.width(16.dp))
                StepCard(
                    location = location,
                    meta = meta,
                    onLocationClick = onLocationClick,
                    isIconOnRight = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PathSegment(index: Int, isLast: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val isRight = index % 2 == 0
        val targetX = if (isRight) w * 0.72f else w * 0.28f
        val targetY = h / 2

        // Previous connection (from item below)
        val prevX = if (index == 0) w / 2f else (if (isRight) w * 0.28f else w * 0.72f)
        val prevY = h * 1.5f

        val path = Path().apply {
            moveTo(prevX, prevY)
            cubicTo(
                prevX, prevY - h * 0.5f,
                targetX, targetY + h * 0.5f,
                targetX, targetY
            )
        }

        drawPath(
            path = path,
            color = Color(0xFFD1D5DB),
            style = Stroke(
                width = 2.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        )

        // Next connection (to end dot if last)
        if (isLast) {
            val endX = w / 2f
            val endY = -40.dp.toPx()
            val nextPath = Path().apply {
                moveTo(targetX, targetY)
                cubicTo(
                    targetX, targetY - h * 0.5f,
                    endX, endY + 20.dp.toPx(),
                    endX, endY
                )
            }
            drawPath(
                path = nextPath,
                color = Color(0xFFD1D5DB),
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
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
                    Text(
                        text = "Aisle ${meta.aisleNumber} • ${location.name}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
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
                    Text(
                        text = "Aisle ${meta.aisleNumber} • ${location.name}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
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
private fun StepCircle(location: AisleLocation, meta: AisleMeta) {
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
            .background(Color.White, CircleShape)
            .border(2.dp, statusColor, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isFilled) statusColor else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (location.status == LocationStatus.COMPLETED) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun StepLabel(stepNumber: Int, isLeft: Boolean) {
    Column(
        horizontalAlignment = if (isLeft) Alignment.End else Alignment.Start,
        modifier = Modifier.width(60.dp)
    ) {
        Text(
            text = "STEP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp
        )
        Text(
            text = stepNumber.toString().padStart(2, '0'),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF334155),
            lineHeight = 32.sp
        )
    }
}
