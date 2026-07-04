package com.watson.trackly.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class RoadStep(
    val number: Int,
    val title: String,
    val description: String,
    val color: Color,
    val icon: ImageVector,
    val alignRight: Boolean
)

private val roadmap = listOf(
    RoadStep(
        1,
        "Research",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        Color(0xFFFF7A3D),
        Icons.Outlined.Search,
        true
    ),
    RoadStep(
        2,
        "Planning",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        Color(0xFFFF4FA3),
        Icons.Outlined.Handshake,
        false
    ),
    RoadStep(
        3,
        "Development",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        Color(0xFF9C4DFF),
        Icons.Outlined.WorkOutline,
        true
    ),
    RoadStep(
        4,
        "Testing",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        Color(0xFF4D6BFF),
        Icons.Outlined.BarChart,
        false
    ),
    RoadStep(
        5,
        "Launch",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        Color(0xFF1CC6D6),
        Icons.Outlined.EmojiEvents,
        true
    )
)

@Composable
fun RoadmapDesign() {
    val mapHeight = (roadmap.size * 170).dp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
    ) {

        val scrollState = rememberScrollState()

        LaunchedEffect(Unit) {
            scrollState.scrollTo(scrollState.maxValue)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ){

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .graphicsLayer {
                        scaleY = -1f
                    }
            ) {

                RoadRibbon(
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 70.dp, bottom = 60.dp)
                ) {

                    roadmap.forEachIndexed { index, step ->

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {

                            RoadStepCard(
                                step = step,
                                modifier = if (step.alignRight)
                                    Modifier
                                        .align(Alignment.CenterEnd)
                                        .graphicsLayer { scaleY = -1f }   // Flip text back
                                else
                                    Modifier
                                        .align(Alignment.CenterStart)
                                        .graphicsLayer { scaleY = -1f }
                            )
                        }

                        Spacer(
                            Modifier.height(if (index == roadmap.lastIndex) 90.dp else 60.dp)
                        )
                    }
                }
            }
        }
    }
}