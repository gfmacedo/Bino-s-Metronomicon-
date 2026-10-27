package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PracticeStatus
import com.example.ui.theme.MetronomeBorder
import com.example.ui.theme.MetronomeSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmOrangeLight
import com.example.viewmodel.MetronomeUiState

// Mustard yellow color palette for flashing pulse indicators (kept as is)
val MustardActive = Color(0xFFE5A823)
val MustardGlow = Color(0xFFFFD43F)
val MustardDim = Color(0xFF282313)
val MustardBorder = Color(0xFF5A4918)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisualPulseIndicator(
    state: MetronomeUiState,
    modifier: Modifier = Modifier
) {
    val isPlaying = state.practiceStatus == PracticeStatus.PLAYING_LOOP ||
            state.practiceStatus == PracticeStatus.INITIAL_COUNT_IN ||
            state.practiceStatus == PracticeStatus.LOOP_COUNT_IN

    val currentLoop = state.practiceLoops.getOrNull(state.currentLoopIndex)

    // Flash animation trigger on each beat or tick
    val flashAnim = remember { Animatable(0f) }
    LaunchedEffect(state.visualFlashKey) {
        if (state.visualFlashKey > 0L) {
            flashAnim.snapTo(1f)
            flashAnim.animateTo(0f, tween(180, easing = FastOutSlowInEasing))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_pulse_indicator"),
        shape = RoundedCornerShape(14.dp),
        color = MetronomeSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status and Current Loop info line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) MustardGlow else TextSecondary.copy(alpha = 0.4f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (state.practiceStatus) {
                            PracticeStatus.INITIAL_COUNT_IN -> "COUNT-IN (${state.countInRemaining})"
                            PracticeStatus.LOOP_COUNT_IN -> "LOOP COUNT-IN (${state.countInRemaining})"
                            PracticeStatus.PLAYING_LOOP -> "PRACTICE (${state.currentLoopIndex + 1}/${state.practiceLoops.size})"
                            PracticeStatus.PAUSED -> "PAUSED"
                            PracticeStatus.STOPPED -> "READY"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isCountInActive) MustardGlow else if (isPlaying) TextPrimary else TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                if (currentLoop != null && isPlaying && !state.isCountInActive) {
                    Text(
                        text = "Beat ${state.currentBeatInLoop + 1}/${currentLoop.totalBeats} • ${currentLoop.subdivision.title}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarmOrangeLight,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pulse dots: ALWAYS reflects the number dictated by the loop length (or count-in beats)
            if (state.isCountInActive) {
                // Count-In Dots matching total count-in beats
                val countBeats = state.totalCountInBeats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (b in 0 until countBeats) {
                        val isFlashed = isPlaying && state.activeDotIndex == b
                        val dotColor by animateColorAsState(
                            targetValue = if (isFlashed) MustardGlow else MustardDim,
                            animationSpec = tween(120),
                            label = "countDot"
                        )
                        Box(
                            modifier = Modifier
                                .size(if (isFlashed) 16.dp else 12.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    width = if (isFlashed) 1.5.dp else 1.dp,
                                    color = if (isFlashed) Color.White else MustardBorder,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            } else {
                // Loop Dots: Exactly matches loop length (totalBeats)
                val loopTotalBeats = currentLoop?.totalBeats ?: state.editorTotalBeats
                val activeBeat = if (isPlaying && currentLoop != null) state.currentBeatInLoop else -1
                val subTicks = currentLoop?.subdivision?.ticksPerBeat ?: state.editorSubdivision.ticksPerBeat

                // Adapt dot sizing if loop has many beats
                val dotSize = when {
                    loopTotalBeats <= 8 -> 14.dp
                    loopTotalBeats <= 16 -> 11.dp
                    else -> 9.dp
                }
                val activeDotSize = when {
                    loopTotalBeats <= 8 -> 18.dp
                    loopTotalBeats <= 16 -> 15.dp
                    else -> 12.dp
                }
                val dotSpacing = if (loopTotalBeats <= 8) 10.dp else 6.dp

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dotSpacing, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = 16
                ) {
                    for (b in 0 until loopTotalBeats) {
                        val isCurrentBeat = isPlaying && activeBeat == b

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            // Main Beat Dot
                            val beatColor by animateColorAsState(
                                targetValue = if (isCurrentBeat) MustardGlow else MustardDim,
                                animationSpec = tween(120),
                                label = "beatDot_$b"
                            )

                            Box(
                                modifier = Modifier
                                    .size(if (isCurrentBeat) activeDotSize else dotSize)
                                    .clip(CircleShape)
                                    .background(beatColor)
                                    .border(
                                        width = if (isCurrentBeat) 1.5.dp else 1.dp,
                                        color = if (isCurrentBeat) Color.White else MustardBorder,
                                        shape = CircleShape
                                    )
                            )

                            // Subdivision mini-dots below the active beat
                            if (subTicks > 1) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    for (sub in 0 until subTicks) {
                                        val isSubActive = isCurrentBeat && state.currentSubdivisionTick == sub
                                        Box(
                                            modifier = Modifier
                                                .size(3.5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSubActive) MustardGlow else MustardBorder)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
