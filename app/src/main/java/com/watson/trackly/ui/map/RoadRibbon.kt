package com.watson.trackly.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun RoadRibbon(
    modifier: Modifier = Modifier
) {

    Canvas(modifier = modifier.fillMaxSize()) {

        val w = size.width
        val h = size.height

        val left = w * 0.18f
        val right = w * 0.82f

        val startX = w * 0.55f

        val y0 = 40f
        val y1 = h * 0.18f
        val y2 = h * 0.34f
        val y3 = h * 0.50f
        val y4 = h * 0.66f
        val y5 = h * 0.82f
        val y6 = h * 0.96f

        val path = Path().apply {

            // top circle stem
            moveTo(startX, y0)
            lineTo(startX, y1 - 70f)

            // first curve
            cubicTo(
                startX,
                y1,
                right,
                y1,
                right,
                y1 + 60f
            )

            lineTo(right, y2 - 60f)

            // second curve
            cubicTo(
                right,
                y2,
                left,
                y2,
                left,
                y2 + 60f
            )

            lineTo(left, y3 - 60f)

            // third curve
            cubicTo(
                left,
                y3,
                right,
                y3,
                right,
                y3 + 60f
            )

            lineTo(right, y4 - 60f)

            // fourth curve
            cubicTo(
                right,
                y4,
                left,
                y4,
                left,
                y4 + 60f
            )

            lineTo(left, y5 - 60f)

            // fifth curve
            cubicTo(
                left,
                y5,
                startX,
                y5,
                startX,
                y6
            )
        }

        // Shadow
        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.10f),
            style = Stroke(
                width = 42.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

// White ribbon
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(
                width = 42.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

// Border
        drawPath(
            path = path,
            color = Color(0xFFE2E2E2),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Bottom arrow
        drawLine(
            color = Color(0xFF555555),
            start = Offset(startX, y0 - 35f),
            end = Offset(startX, y0),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Top circle
        drawCircle(
            color = Color.White,
            radius = 14.dp.toPx(),
            center = Offset(startX, y0)
        )

        drawCircle(
            color = Color(0xFF555555),
            radius = 14.dp.toPx(),
            center = Offset(startX, y0),
            style = Stroke(3.dp.toPx())
        )
    }
}