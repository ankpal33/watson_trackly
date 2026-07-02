package com.watson.trackly.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aholdusa.cleansweep.ui.common.*
import com.watson.trackly.App
import com.watson.trackly.ui.map.LocationStatus
import com.watson.trackly.ui.setting.LocationItem


enum class ScanCardState { INITIAL, SCANNED, IDENTIFY_HAZARD, HAZARD_DESCRIPTION, COMPLETED, PENDING, SKIPPED, PENDING_HAZARD }


/**
 * Reusable primary button component
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: TextUnit = 16.sp
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppMain,              // Enabled background color
            contentColor = Color.White,           // Enabled text color
            disabledContainerColor = AppMain.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Reusable outlined button component
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: TextUnit = 16.sp
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        enabled = enabled,
        border = BorderStroke(
            width = 1.dp,
            color = AppMain
        )
    ) {
        Text(text = text, fontSize = fontSize, fontWeight = FontWeight.Medium, color = AppMain)
    }
}

/**
 * Reusable icon button component
 */
@Composable
fun AppIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AppMain,
    size: Dp = 28.dp
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}

/**
 * Reusable header text component
 */
@Composable
fun HeaderText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppMain
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

/**
 * Reusable title text component
 */
@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppMain
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

/**
 * Reusable body text component
 */
@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

/**
 * Reusable label text component
 */
@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = modifier
    )
}

// Made with Bob

@Composable
fun VerticalLinearMap(
    locations: List<LocationItem>,
    currentCardState: ScanCardState,
    matchedLocation: LocationItem?,
    modifier: Modifier = Modifier,
    cardBottomPadding: Dp,
    listState: LazyListState = rememberLazyListState()
) {
    val itemHeight = 64.dp
    val dotRadius = 12.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = cardBottomPadding),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight / 2),
            state = listState,
        ) {
            itemsIndexed(
                items = locations,
                key = { _, item -> item.barcode }
            ) { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name.uppercase(),
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        textAlign = TextAlign.End
                    )

                    DotWithLine(
                        index = index,
                        total = locations.size,
                        status = item.status,
                        isMatched = item.barcode == matchedLocation?.barcode,
                        currentCardState = currentCardState,
                        dotRadius = dotRadius,
                        barcode = item.barcode
                    )

                    if (item.status != LocationStatus.DEFAULT) {
                        Text(
                            text = item.status.name,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            color = when (item.status) {
                                LocationStatus.COMPLETED -> ApexGreen
                                LocationStatus.PENDING -> Yellow
                                LocationStatus.SKIPPED -> Red
                                else -> Black
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f)) // keeps layout balanced
                    }
                }
            }
        }
    }
}

//Location Map
@Composable
private fun DotWithLine(
    index: Int,
    total: Int,
    status: LocationStatus,
    dotRadius: Dp,
    barcode: String,
    isMatched: Boolean = false,
    currentCardState: ScanCardState? = null
) {
    val lineColor = Color.Black

    Box(
        modifier = Modifier
            .width(dotRadius * 2 + 8.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Connecting vertical line
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (total > 1) {
                val halfWidth = size.width / 2f
                val halfHeight = size.height / 2f
                val startY = if (index == total - 1) halfHeight else 0f
                val endY = if (index == 0) halfHeight else size.height
                drawLine(
                    color = lineColor,
                    start = Offset(halfWidth, startY),
                    end = Offset(halfWidth, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        //dot: green border around a white circle for Scanned, yellow for Pending, red for Skipped
        Box(
            modifier = Modifier
                .size(dotRadius * 2 + 4.dp)
                .semantics { contentDescription = barcode },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radiusPx = dotRadius.toPx()
                val fillColor = when (status) {
                    LocationStatus.SCANNED -> Color.White
                    LocationStatus.COMPLETED -> ApexGreen
                    LocationStatus.SKIPPED -> Red
                    LocationStatus.PENDING -> Yellow
                    else -> Color.Black
                }

                val borderColor = when (status) {
                    LocationStatus.SCANNED -> ApexGreen
                    else -> Color.Transparent
                }
                if (borderColor != Color.Transparent) {
                    drawCircle(
                        color = borderColor,
                        radius = radiusPx + 2.dp.toPx(),
                        center = center
                    )
                }
                drawCircle(
                    color = fillColor,
                    radius = radiusPx,
                    center = center
                )
            }
            //Status Icons (Check, X, Dash): Icons appear only for COMPLETED / SKIPPED / PENDING
            when (status) {
                LocationStatus.COMPLETED -> Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(dotRadius * 1.5f)
                )

                LocationStatus.SKIPPED -> Icon(
                    Icons.Filled.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(dotRadius * 1.5f)
                )

                LocationStatus.PENDING -> Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(dotRadius * 1.5f)
                )

                else -> {}
            }
        }
    }
}