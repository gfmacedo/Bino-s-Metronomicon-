package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoundKit
import com.example.ui.theme.GoldLight
import com.example.ui.theme.MetronomeBorder
import com.example.ui.theme.MetronomeSurface
import com.example.ui.theme.MetronomeSurfaceHigh
import com.example.ui.theme.MetronomeSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MetronomeUiState
import com.example.viewmodel.MetronomiconViewModel

@Composable
fun SoundSelectorSection(
    state: MetronomeUiState,
    viewModel: MetronomiconViewModel,
    modifier: Modifier = Modifier
) {
    var soundMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sound_selector_section"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MetronomeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CLICK SOUND",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Box {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MetronomeSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder),
                    modifier = Modifier
                        .clickable { soundMenuExpanded = true }
                        .testTag("sound_kit_dropdown_trigger")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.soundKit.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select sound kit",
                            tint = GoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = soundMenuExpanded,
                    onDismissRequest = { soundMenuExpanded = false },
                    modifier = Modifier.background(MetronomeSurfaceHigh)
                ) {
                    SoundKit.entries.forEach { kit ->
                        val isSelected = kit == state.soundKit
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = kit.displayName,
                                    color = if (isSelected) GoldLight else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp)) }
                            } else null,
                            onClick = {
                                viewModel.setSoundKit(kit)
                                soundMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
