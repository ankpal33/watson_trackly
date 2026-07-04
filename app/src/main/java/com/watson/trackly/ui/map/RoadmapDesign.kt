package com.watson.trackly.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoadmapDesign(
    locations: List<AisleLocation>,
    onLocationClick: (String) -> Unit = {}
) {

    val roadmapColumns = remember(locations) {
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

    LazyRow(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(80.dp),
        contentPadding = PaddingValues(horizontal = 48.dp)
    ) {

        itemsIndexed(roadmapColumns) { index, column ->

            RoadmapColumnView(
                column = column,
                columnIndex = index,
                onLocationClick = onLocationClick
            )
        }
    }
}

@Composable
private fun RoadmapColumnView(
    column: RoadmapColumn,
    columnIndex: Int,
    onLocationClick: (String) -> Unit
) {

    val displayLocations =
        if (columnIndex % 2 == 0)
            column.locations
        else
            column.locations.reversed()

    Box(
        modifier = Modifier
            .width(340.dp)
            .fillMaxHeight()
    ) {

        VerticalDashedPath()

        Column(
            modifier = Modifier
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            displayLocations.forEach { location ->

                RoadmapNode(
                    location = location,
                    isLeftCard = columnIndex % 2 == 0,
                    onLocationClick = onLocationClick
                )

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun VerticalDashedPath() {

    Canvas(
        Modifier.fillMaxSize()
    ) {

        val x = size.width / 2

        drawLine(
            color = Color(0xFFD1D5DB),
            start = Offset(x,0f),
            end = Offset(x,size.height),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(12f,12f)
            )
        )
    }
}

@Composable
private fun RoadmapRow(
    row: RoadmapProductRow,
    onLocationClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // LEFT AISLE
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            row.leftLocation?.let {
                RoadmapNode(
                    location = it,
                    isLeftCard = true,
                    onLocationClick = onLocationClick
                )
            }
        }

        Spacer(Modifier.width(48.dp))

        // RIGHT AISLE
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            row.rightLocation?.let {
                RoadmapNode(
                    location = it,
                    isLeftCard = false,
                    onLocationClick = onLocationClick
                )
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
private fun RoadmapNode(
    location: AisleLocation,
    isLeftCard: Boolean,
    onLocationClick: (String) -> Unit
) {
    val meta = nodeMetaMap[location.id] ?: return

    Row(
        modifier = Modifier.width(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (isLeftCard) {

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
                meta = meta
            )

        } else {

            StepCircle(
                location = location,
                meta = meta
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