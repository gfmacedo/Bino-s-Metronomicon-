package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Subdivision
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MetronomeBorder
import com.example.ui.theme.MetronomeSurface
import com.example.ui.theme.MetronomeSurfaceHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MetronomeUiState
import com.example.viewmodel.MetronomiconViewModel

@Composable
fun LoopEditorSection(
    state: MetronomeUiState,
    viewModel: MetronomiconViewModel,
    modifier: Modifier = Modifier
) {
    val isEditing = state.editingLoopId != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("loop_editor_section"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MetronomeSurface),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isEditing) 1.5.dp else 1.dp,
            color = if (isEditing) GoldPrimary else MetronomeBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "EDIT LOOP" else "LOOP SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldLight,
                    letterSpacing = 1.sp
                )

                if (isEditing) {
                    OutlinedButton(
                        onClick = { viewModel.cancelEditing() },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Cancel", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // BPM Row with Stepper, Slider and Tap Tempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tempo:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${state.editorBpm} BPM",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = GoldLight
                )
            }

            Slider(
                value = state.editorBpm.toFloat(),
                onValueChange = { viewModel.setEditorBpm(it.toInt()) },
                valueRange = 40f..300f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bpm_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = GoldPrimary,
                    activeTrackColor = GoldDark,
                    inactiveTrackColor = MetronomeSurfaceHigh
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CompactAdjustButton("-5") { viewModel.adjustEditorBpm(-5) }
                    CompactAdjustButton("-1") { viewModel.adjustEditorBpm(-1) }
                    CompactAdjustButton("+1") { viewModel.adjustEditorBpm(+1) }
                    CompactAdjustButton("+5") { viewModel.adjustEditorBpm(+5) }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MetronomeSurfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder),
                    modifier = Modifier
                        .clickable { viewModel.tapTempo() }
                        .testTag("tap_tempo_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = GoldLight, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("TAP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Beats Per Bar: +/- Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Beats per Bar:",
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                StepperControl(
                    valueText = "${state.editorBeatsPerBar}",
                    onDecrement = { viewModel.adjustEditorBeatsPerBar(-1) },
                    onIncrement = { viewModel.adjustEditorBeatsPerBar(+1) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subdivisions buttons: shrunken down and ordered 4th, 8th, 16th, 3, 6
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Subdivision:",
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Ordered explicitly: 4th, 8th, 16th, 3, 6
                    val orderedSubdivisions = listOf(
                        Subdivision.QUARTER,
                        Subdivision.EIGHTH,
                        Subdivision.SIXTEENTH,
                        Subdivision.TRIPLET,
                        Subdivision.SEXTUPLET
                    )

                    orderedSubdivisions.forEach { sub ->
                        val isSelected = state.editorSubdivision == sub
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) GoldPrimary else MetronomeSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GoldLight else MetronomeBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .clickable { viewModel.setEditorSubdivision(sub) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = sub.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loop Length (Beats): +/- Selection, default 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Loop Length (Beats):",
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                StepperControl(
                    valueText = "${state.editorTotalBeats}",
                    onDecrement = { viewModel.adjustEditorTotalBeats(-1) },
                    onIncrement = { viewModel.adjustEditorTotalBeats(+1) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loop Count-In: Toggle + Stepper (+ / -), default 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = state.editorHasCountIn,
                        onCheckedChange = { viewModel.setEditorHasCountIn(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = GoldPrimary.copy(alpha = 0.4f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = MetronomeSurfaceHigh
                        ),
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Loop Count-In:",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }

                AnimatedVisibility(visible = state.editorHasCountIn) {
                    StepperControl(
                        valueText = "${state.editorCountInBeats}",
                        onDecrement = { viewModel.adjustEditorCountInBeats(-1) },
                        onIncrement = { viewModel.adjustEditorCountInBeats(+1) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Loop Button
            Button(
                onClick = { viewModel.commitEditorLoop() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("add_loop_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEditing) "SAVE CHANGES" else "ADD LOOP TO PRACTICE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun CompactAdjustButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MetronomeSurfaceHigh,
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(26.dp)
            .width(34.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun StepperControl(
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MetronomeSurfaceHigh,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onDecrement),
            border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary, modifier = Modifier.size(14.dp))
            }
        }

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = valueText,
                fontSize = 12.sp,
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
                .clickable(onClick = onIncrement),
            border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary, modifier = Modifier.size(14.dp))
            }
        }
    }
}
