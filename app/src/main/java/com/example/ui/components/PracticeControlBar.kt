package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MetronomeBorder
import com.example.ui.theme.MetronomeSurface
import com.example.ui.theme.MetronomeSurfaceHigh
import com.example.ui.theme.MetronomeSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MetronomeUiState
import com.example.viewmodel.MetronomiconViewModel

@Composable
fun PracticeControlBar(
    state: MetronomeUiState,
    viewModel: MetronomiconViewModel,
    modifier: Modifier = Modifier
) {
    val isPlaying = state.practiceStatus == PracticeStatus.PLAYING_LOOP ||
            state.practiceStatus == PracticeStatus.INITIAL_COUNT_IN ||
            state.practiceStatus == PracticeStatus.LOOP_COUNT_IN

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("practice_control_bar"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MetronomeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Single Start / Stop Button
            Button(
                onClick = { viewModel.togglePractice() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("master_play_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) CrimsonAccent else GoldPrimary,
                    contentColor = if (isPlaying) Color.White else Color.Black
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop Practice" else "Start Practice",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlaying) "STOP PRACTICE" else "START PRACTICE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Initial Count-In Stepper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MetronomeSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = state.initialCountInEnabled,
                        onCheckedChange = { viewModel.setInitialCountInEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = GoldPrimary.copy(alpha = 0.4f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = MetronomeSurfaceHigh
                        ),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Initial Count-In",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                AnimatedVisibility(visible = state.initialCountInEnabled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MetronomeSurfaceHigh,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { viewModel.adjustInitialCountInBeats(-1) },
                            border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary, modifier = Modifier.size(14.dp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${state.initialCountInBeats}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = GoldLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MetronomeSurfaceHigh,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { viewModel.adjustInitialCountInBeats(+1) },
                            border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary, modifier = Modifier.size(14.dp))
                            }
                        }

                        Text("beats", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(start = 2.dp))
                    }
                }
            }
        }
    }
}
